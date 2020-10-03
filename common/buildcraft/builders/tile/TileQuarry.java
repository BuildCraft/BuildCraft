/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.chunkload.ChunkLoaderManager;
import buildcraft.lib.chunkload.IChunkLoadingTile;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.inventory.AutomaticProvidingTransactor;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.AxisOrder;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.BoxIterator;
import buildcraft.lib.misc.data.EnumAxisOrder;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.world.WorldEventListenerAdapter;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersConfig;
import buildcraft.builders.BCBuildersEventDist;
import buildcraft.builders.client.render.AdvDebuggerQuarry;
import buildcraft.core.BCCoreConfig;
import buildcraft.core.marker.VolumeCache;
import buildcraft.core.marker.VolumeConnection;
import buildcraft.core.marker.VolumeSubCache;
import buildcraft.core.tile.TileMarkerVolume;

public class TileQuarry extends TileBC_Neptune implements ITickable, IDebuggable, IChunkLoadingTile {
    public static final boolean DEBUG_QUARRY = BCDebugging.shouldDebugLog("builders.quarry");
    private static final long MAX_POWER_PER_TICK = 512 * MjAPI.MJ;
    private static final ResourceLocation ADVANCEMENT_COMPLETE
        = new ResourceLocation("buildcraftbuilders:diggy_diggy_hole");

    private final MjBattery battery = new MjBattery(24000 * MjAPI.MJ);
    public final Box frameBox = new Box();
    private final Box miningBox = new Box();
    private BoxIterator boxIterator;
    public final List<BlockPos> framePoses = new ArrayList<>();
    private int frameBoxPosesCount = 0;
    private final LinkedList<BlockPos> toCheck = new LinkedList<>();
    private final Set<BlockPos> firstCheckedPoses = new HashSet<>();
    private boolean firstChecked = false;
    private final Set<BlockPos> frameBreakBlockPoses = new TreeSet<>(
        BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(p -> getPos().distanceSq(p)))
    );
    private final Set<BlockPos> framePlaceFramePoses = new HashSet<>();
    public Task currentTask = null;
    public Vec3d drillPos;
    public Vec3d clientDrillPos;
    public Vec3d prevClientDrillPos;
    private long debugPowerRate = 0;
    private double blockPercentSoFar;
    private double moveDistanceSoFar;

    private List<AxisAlignedBB> collisionBoxes = ImmutableList.of();
    private Vec3d collisionDrillPos;

    private final IWorldEventListener worldEventListener = new WorldEventListenerAdapter() {
        @Override
        public void notifyBlockUpdate(
            World w, BlockPos updatePos, IBlockState oldState, IBlockState newState, int flags
        ) {
            w.profiler.startSection("bc_quarry_listener");
            if (frameBox.isInitialized() && miningBox.isInitialized()) {
                if (frameBox.contains(updatePos)) {
                    check(updatePos);
                } else if (miningBox.contains(updatePos) && boxIterator != null) {
                    if (boxIterator.hasVisited(updatePos)) {
                        if (!canMoveThrough(updatePos) && canMoveDownTo(updatePos)) {
                            boxIterator.moveTo(updatePos);
                        }
                    }
                }
            }
            w.profiler.endSection();
        }
    };

    public TileQuarry() {
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
        caps.addCapabilityInstance(
            CapUtil.CAP_ITEM_TRANSACTOR, AutomaticProvidingTransactor.INSTANCE, EnumPipePart.VALUES
        );
    }

    @Nonnull
    private BoxIterator createBoxIterator() {
        long x = getPos().getX();
        long y = getPos().getY();
        long z = getPos().getZ();
        long seed = ((x & 0xFFFF) << 0) | ((y & 0xFFFF) << 16) | ((z & 0xFFFF << 32));

        Random rand = new Random(seed);
        EnumAxisOrder axisOrder = rand.nextBoolean() ? EnumAxisOrder.XZY : EnumAxisOrder.ZXY;
        AxisOrder.Inversion inv = AxisOrder.Inversion.getFor(rand.nextBoolean(), rand.nextBoolean(), false);
        return new BoxIterator(miningBox, AxisOrder.getFor(axisOrder, inv), true);
    }

    /** Gets the current positions where frame blocks should be placed, in order.
     * <p>
     * Assumes that {@link #frameBox} is correct for the current position. Does not take into account the current facing
     * of the quarry, as that is assumed to be involved in the {@link #frameBox} itself.
     *
     * @return An ordered list of the positions that the frame should be placed in. The list is in placement order.
     * @throws IllegalStateException if something went wrong during iteration, or the current {@link #frameBox} was
     *             incorrect compared to {@link #getPos()} */
    private List<BlockPos> getFramePositions() {
        // visitedSet and framePositions are considered the same
        // - both should contain the same elements
        // - neither should contain duplicate elements
        // - visitedSet is used as an optimisation, as set.contains is likely to be faster than list.contains
        Set<BlockPos> visitedSet = new HashSet<>();
        List<BlockPos> framePositions = new ArrayList<>();

        List<BlockPos> openSet = new ArrayList<>();
        List<BlockPos> nextOpenSet = new ArrayList<>();

        // Assume that frameBox is right next to the quarries position
        // If it's not then its not that big of a problem, as the iteration will
        // not add any of the frame positions. However we will end up with a list
        // containing no elements, which isn't ideal.
        openSet.add(getPos());

        // Hold on to the array of orders, as we shuffle it on each iteration
        EnumFacing[] order = EnumFacing.values();
        // Also hold on to it as a list, so that we don't have to re-create it all the time
        List<EnumFacing> orderAsList = Arrays.asList(order);

        // This is technically higher than the number of iterations needed as
        // most of the iterations will add more than one edge block.
        int maxIterationCount = frameBox.getBlocksOnEdgeCount();
        int iterationCount = 0;
        do {
            for (BlockPos p : openSet) {
                Collections.shuffle(orderAsList);
                for (EnumFacing face : order) {
                    BlockPos next = p.offset(face);
                    // Each iteration we add the *next* positions, rather than the current position
                    // Then we can just add the quarries position once (which isn't part of the frame)
                    if (frameBox.isOnEdge(next) && visitedSet.add(next)) {
                        nextOpenSet.add(next);
                        framePositions.add(next);
                    }
                }
            }
            // Clear openSet and swap it with nextOpenSet
            // Chances are that the arrays will only ever get bigger
            // So its useful to avoid lots of allocation every iteration.
            openSet.clear();
            List<BlockPos> t = openSet;
            openSet = nextOpenSet;
            nextOpenSet = t;

            // Shuffle the open set each time, to avoid the
            // (odd) order that it frames are normally built in.
            Collections.shuffle(openSet);

            // Sanity Check: Ensure that openSet isn't huge
            // the (theoretical) maximum size would be if all
            // 8 corners were visited in the same iteration,
            // and somehow were the first 8 added.
            if (openSet.size() > 8 * 3) {
                String msg = "OpenSet got too big!";
                msg += "\n  Position = " + pos;
                msg += "\n  Frame Box = " + frameBox;
                msg += "\n  Iteration Count = " + iterationCount;
                msg += "\n  OpenSet = " + openSet.stream().map(Object::toString).collect(
                    Collectors.joining("\n  ", "[", "]")
                );
                throw new IllegalStateException(msg);
            }

            // Ensure that we aren't going infinitely
            iterationCount++;
            if (iterationCount >= maxIterationCount) {
                // We definitely failed. As maxIterationCount is an over-estimate
                String msg = "Failed to generate a correct list of frame positions! Was the frame box wrong?";
                msg += "\n  Position = " + pos;
                msg += "\n  Frame Box = " + frameBox;
                msg += "\n  Iteration Count = " + iterationCount;
                msg += "\n  OpenSet = " + openSet.stream().map(Object::toString).collect(
                    Collectors.joining("\n  ", "[", "]")
                );
                throw new IllegalStateException(msg);
            }
        } while (!openSet.isEmpty());

        if (framePositions.isEmpty()) {
            // We failed. Perhaps frameBox wasn't actually right next to the position of the quarry?
            String msg = "Failed to generate a correct list of frame positions! Was the frame box wrong?";
            msg += "\n  Position = " + pos;
            msg += "\n  Frame Box = " + frameBox;
            throw new IllegalStateException(msg);
        }

        return framePositions;
    }

    private boolean shouldBeFrame(BlockPos p) {
        return frameBox.isOnEdge(p);
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (placer.world.isRemote) {
            return;
        }
        EnumFacing facing = world.getBlockState(pos).getValue(BlockBCBase_Neptune.PROP_FACING);
        BlockPos areaPos = pos.offset(facing.getOpposite());
        TileEntity tile = world.getTileEntity(areaPos);
        BlockPos min = null, max = null;
        if (tile instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) tile;
            min = provider.min();
            max = provider.max();
            int dx = max.getX() - min.getX();
            int dz = max.getZ() - min.getZ();
            if (dx < 3 || dz < 3) {
                min = null;
                max = null;
            } else {
                provider.removeFromWorld();
            }
        }
        // noinspection ConstantConditions
        if (min == null || max == null) {
            min = null;
            max = null;
            VolumeSubCache cache = VolumeCache.INSTANCE.getSubCache(getWorld());
            for (BlockPos markerPos : cache.getAllMarkers()) {
                TileMarkerVolume marker = (TileMarkerVolume) cache.getMarker(markerPos);
                if (marker == null) {
                    continue;
                }
                VolumeConnection connection = marker.getCurrentConnection();
                if (connection == null) {
                    continue;
                }
                Box volBox = connection.getBox();
                Box box2 = new Box();
                box2.initialize(volBox);
                if (!box2.isInitialized()) {
                    continue;
                }
                if (pos.getY() != box2.min().getY()) {
                    continue;
                }
                if (box2.contains(pos)) {
                    continue;
                }
                if (!box2.contains(areaPos)) {
                    continue;
                }
                if (box2.size().getX() < 3 || box2.size().getZ() < 3) {
                    continue;
                }
                box2.expand(1);
                box2.setMin(box2.min().up());
                if (box2.isOnEdge(pos)) {
                    min = volBox.min();
                    max = volBox.max();
                    marker.removeFromWorld();
                    break;
                }
            }
        }
        if (min == null || max == null) {
            miningBox.reset();
            frameBox.reset();
            switch (facing.getOpposite()) {
                case DOWN:
                case UP:
                default:
                case EAST: // +X
                    min = pos.add(1, 0, -5);
                    max = pos.add(11, 4, 5);
                    break;
                case WEST: // -X
                    min = pos.add(-11, 0, -5);
                    max = pos.add(-1, 4, 5);
                    break;
                case SOUTH: // +Z
                    min = pos.add(-5, 0, 1);
                    max = pos.add(5, 4, 11);
                    break;
                case NORTH: // -Z
                    min = pos.add(-5, 0, -11);
                    max = pos.add(5, 4, -1);
                    break;
            }
        }
        if (max.getY() - min.getY() < BCBuildersConfig.quarryFrameMinHeight) {
            max = new BlockPos(max.getX(), min.getY() + BCBuildersConfig.quarryFrameMinHeight, max.getZ());
        }
        if (world.isOutsideBuildHeight(max)) {
            int dist = max.getY() - min.getY();
            min = min.down(dist);
            max = max.down(dist);
        }
        frameBox.reset();
        frameBox.setMin(min);
        frameBox.setMax(max);
        miningBox.reset();
        int minY = max.getY() - 1 - BCCoreConfig.miningMaxDepth;
        if (world.isOutsideBuildHeight(new BlockPos(min.getX(), minY, min.getZ()))) {
            // TODO: Ask cubic chunks's world for the actual minimum height, rather than just assume 0!
            minY = 0;
        }
        miningBox.setMin(new BlockPos(min.getX() + 1, minY, min.getZ() + 1));
        miningBox.setMax(new BlockPos(max.getX() - 1, max.getY() - 1, max.getZ() - 1));
        updatePoses();
    }

    private boolean canMine(BlockPos blockPos) {
        if (world.getBlockState(blockPos).getBlockHardness(world, blockPos) < 0) {
            return false;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(world, blockPos);
        return fluid == null || fluid.getViscosity() <= 1000;
    }

    private boolean canMoveThrough(BlockPos blockPos) {
        if (world.isAirBlock(blockPos)) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(world, blockPos);
        return fluid != null && fluid.getViscosity() <= 1000;
    }

    private boolean canMoveDownTo(BlockPos blockPos) {
        for (int y = miningBox.max().getY(); y > blockPos.getY(); y--) {
            if (!canMoveThrough(VecUtil.replaceValue(blockPos, Axis.Y, y))) {
                return false;
            }
        }
        return true;
    }

    private boolean canIgnoreInFrameBox(BlockPos blockPos) {
        return !world.isAirBlock(blockPos) && BlockUtil.getFluidWithFlowing(world, blockPos) == null;
    }

    private void check(BlockPos blockPos) {
        frameBreakBlockPoses.remove(blockPos);
        framePlaceFramePoses.remove(blockPos);
        if (shouldBeFrame(blockPos)) {
            if (world.getBlockState(blockPos).getBlock() != BCBuildersBlocks.frame) {
                if (canIgnoreInFrameBox(blockPos)) {
                    frameBreakBlockPoses.add(blockPos);
                } else {
                    framePlaceFramePoses.add(blockPos);
                }
            }
        } else {
            if (canIgnoreInFrameBox(blockPos)) {
                frameBreakBlockPoses.add(blockPos);
            }
        }
        if (!firstChecked) {
            firstCheckedPoses.add(blockPos);
            if (firstCheckedPoses.size() >= frameBoxPosesCount) {
                firstChecked = true;
            }
        }
    }

    @Override
    public void onLoad() {
        if (!world.isRemote) {
            updatePoses();
        }
    }

    @Override
    public void validate() {
        super.validate();
        BCBuildersEventDist.INSTANCE.validateQuarry(this);
        if (!world.isRemote) {
            world.addEventListener(worldEventListener);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        BCBuildersEventDist.INSTANCE.invalidateQuarry(this);
        if (!world.isRemote) {
            world.removeEventListener(worldEventListener);
            ChunkLoaderManager.releaseChunksFor(this);
        }
    }

    @Nullable
    @Override
    public LoadType getLoadType() {
        return LoadType.HARD;
    }

    @Nullable
    @Override
    public Set<ChunkPos> getChunksToLoad() {
        if (!miningBox.isInitialized()) {
            return null;
        }
        Set<ChunkPos> chunkPoses = new HashSet<>();
        ChunkPos minChunkPos = new ChunkPos(frameBox.min());
        ChunkPos maxChunkPos = new ChunkPos(frameBox.max());
        for (int x = minChunkPos.x; x <= maxChunkPos.x; x++) {
            for (int z = minChunkPos.z; z <= maxChunkPos.z; z++) {
                chunkPoses.add(new ChunkPos(x, z));
            }
        }
        return chunkPoses;
    }

    private void updatePoses() {
        framePoses.clear();
        frameBoxPosesCount = 0;
        toCheck.clear();
        firstCheckedPoses.clear();
        firstChecked = false;
        frameBreakBlockPoses.clear();
        framePlaceFramePoses.clear();
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == BCBuildersBlocks.quarry && frameBox.isInitialized()) {
            List<BlockPos> blocksInArea = frameBox.getBlocksInArea();
            blocksInArea.sort(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(pos::distanceSq)));
            frameBoxPosesCount = blocksInArea.size();
            toCheck.addAll(blocksInArea);
            framePoses.addAll(getFramePositions());
            ChunkLoaderManager.loadChunksForTile(this);
        }
    }

    @Override
    public void update() {
        if (drillPos == null) {
            collisionBoxes = ImmutableList.of();
            collisionDrillPos = null;
        }

        if (world.isRemote) {
            prevClientDrillPos = clientDrillPos;
            clientDrillPos = drillPos;
            if (currentTask != null) {
                currentTask.clientTick();
            }
            return;
        }

        if (!frameBox.isInitialized() || !miningBox.isInitialized()) {
            return;
        }

        if (!toCheck.isEmpty()) {
            for (int i = 0; i < (firstChecked ? 10 : 500); i++) {
                BlockPos blockPos = toCheck.pollFirst();
                check(blockPos);
                toCheck.addLast(blockPos);
            }
        }

        if (!firstChecked) {
            return;
        }

        long max;
        if (battery.getStored() > battery.getCapacity() / 2) {
            max = MAX_POWER_PER_TICK;
        } else {
            long roundedUp = battery.getStored() + MjAPI.MJ / 2;
            if (roundedUp > Long.MAX_VALUE / MAX_POWER_PER_TICK) {
                // The multiplication would overflow, so we'll have to use BigInteger for this bit
                max = BigInteger.valueOf(roundedUp).multiply(BigInteger.valueOf(MAX_POWER_PER_TICK))
                    .divide(BigInteger.valueOf(battery.getCapacity() / 2)).longValue();
            } else {
                max = MAX_POWER_PER_TICK * roundedUp / (battery.getCapacity() / 2);
            }
            max = MathUtil.clamp(max, 0, MAX_POWER_PER_TICK);
        }
        debugPowerRate = max;
        blockPercentSoFar = 0;
        moveDistanceSoFar = 0;

        int maxTasks = Math.max(1, (int) (max * BCBuildersConfig.quarryMaxTasksPerTick / MAX_POWER_PER_TICK));
        boolean sendUpdate = false;
        power_loop: for (int i = 0; i < maxTasks; i++) {

            if (currentTask != null) {

                long needed = currentTask.getRequiredPowerThisTick();
                long added;
                final int mult = BCBuildersConfig.quarryTaskPowerDivisor;
                if (mult > 0) {
                    long nNeeded = needed * (mult + i) / mult;
                    long leftover = (needed * (mult + i)) % mult;
                    long power = battery.extractPower(0, Math.min(max, nNeeded));
                    max -= power;
                    added = power * mult / (mult + i);
                    if (leftover > 0) {
                        added++;
                    }
                } else {
                    added = battery.extractPower(0, Math.min(max, needed));
                    max -= added;
                }
                if (currentTask.addPower(added)) {
                    currentTask = null;
                } else {
                    sendUpdate = true;
                    break;
                }
            }

            if (!frameBreakBlockPoses.isEmpty()) {
                BlockPos blockPos = frameBreakBlockPoses.iterator().next();
                if (canMine(blockPos)) {
                    drillPos = null;
                    currentTask = new TaskBreakBlock(blockPos);
                    sendUpdate = true;
                }
                check(blockPos);
                continue power_loop;
            }

            if (!framePlaceFramePoses.isEmpty()) {
                for (BlockPos blockPos : framePoses) {
                    if (!framePlaceFramePoses.contains(blockPos)) {
                        continue;
                    }
                    check(blockPos);
                    if (!framePlaceFramePoses.contains(blockPos)) {
                        continue;
                    }
                    drillPos = null;
                    currentTask = new TaskAddFrame(blockPos);
                    sendUpdate = true;
                    continue power_loop;
                }
            }

            if (boxIterator == null || drillPos == null) {
                boxIterator = createBoxIterator();
                while (
                    canMoveThrough(boxIterator.getCurrent()) || !canMine(boxIterator.getCurrent())
                    || !canMoveDownTo(boxIterator.getCurrent())
                ) {
                    if (boxIterator.advance() == null) {
                        break;
                    }
                }
                drillPos = new Vec3d(miningBox.closestInsideTo(pos));
            }

            if (boxIterator != null && boxIterator.hasNext()) {
                while (
                    canMoveThrough(boxIterator.getCurrent()) || !canMine(boxIterator.getCurrent())
                    || !canMoveDownTo(boxIterator.getCurrent())
                ) {
                    if (boxIterator.advance() == null) {
                        break;
                    }
                }

                if (boxIterator.hasNext()) {
                    boolean found = false;

                    if (drillPos.squareDistanceTo(new Vec3d(boxIterator.getCurrent())) >= 1) {
                        currentTask = new TaskMoveDrill(drillPos, new Vec3d(boxIterator.getCurrent()));
                        found = true;
                    } else if (canMine(boxIterator.getCurrent())) {
                        currentTask = new TaskBreakBlock(boxIterator.getCurrent());
                        found = true;
                    }

                    if (found) {
                        sendUpdate = true;
                    } else {
                        AxisAlignedBB box = miningBox.getBoundingBox();
                        if (box.maxX - box.minX == 63 && box.maxZ - box.minZ == 63) {
                            AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_COMPLETE);
                        }
                    }
                }
            }
        }
        debugPowerRate -= max;
        if (sendUpdate) {
            sendNetworkUpdate(NET_RENDER_DATA);
        }
    }

    public List<AxisAlignedBB> getCollisionBoxes() {
        if (drillPos != null && drillPos != collisionDrillPos && frameBox.isInitialized()) {
            Vec3d max = VecUtil.convertCenter(frameBox.max());
            Vec3d min = VecUtil.replaceValue(VecUtil.convertCenter(frameBox.min()), Axis.Y, max.y);
            collisionBoxes = ImmutableList.of(
                BoundingBoxUtil.makeFrom(
                    VecUtil.replaceValue(min, Axis.X, drillPos.x + 0.5), VecUtil.replaceValue(
                        max, Axis.X, drillPos.x + 0.5
                    ), 0.25
                ), BoundingBoxUtil.makeFrom(
                    VecUtil.replaceValue(min, Axis.Z, drillPos.z + 0.5), VecUtil.replaceValue(
                        max, Axis.Z, drillPos.z + 0.5
                    ), 0.25
                ), BoundingBoxUtil.makeFrom(
                    drillPos.addVector(0.5, 0, 0.5), VecUtil.replaceValue(drillPos, Axis.Y, max.y).addVector(
                        0.5, 0, 0.5
                    ), 0.25
                )
            );
            collisionDrillPos = drillPos;
        }
        return collisionBoxes;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("box", miningBox.writeToNBT());
        nbt.setTag("frame", frameBox.writeToNBT());
        if (boxIterator != null) {
            nbt.setTag("boxIterator", boxIterator.writeToNbt());
        }
        nbt.setTag("battery", battery.serializeNBT());
        if (currentTask != null) {
            nbt.setByte(
                "currentTaskId", (byte) Arrays.stream(EnumTaskType.values()).filter(
                    type -> type.clazz == currentTask.getClass()
                ).findFirst().orElseThrow(IllegalStateException::new).ordinal()
            );
            nbt.setTag("currentTaskData", currentTask.serializeNBT());
        }
        if (drillPos != null) {
            nbt.setTag("drillPos", NBTUtilBC.writeVec3d(drillPos));
        }
        nbt.setBoolean("firstChecked", firstChecked);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        miningBox.initialize(nbt.getCompoundTag("box"));
        frameBox.initialize(nbt.getCompoundTag("frame"));
        boxIterator = BoxIterator.readFromNbt(nbt.getCompoundTag("boxIterator"));
        battery.deserializeNBT(nbt.getCompoundTag("battery"));
        if (nbt.hasKey("currentTask")) {
            currentTask = EnumTaskType.values()[(int) nbt.getByte("currentTaskId")].supplier.apply(this);
            currentTask.readFromNBT(nbt.getCompoundTag("currentTaskData"));
        } else {
            currentTask = null;
        }
        drillPos = NBTUtilBC.readVec3d(nbt.getTag("drillPos"));
        firstChecked = nbt.getBoolean("firstChecked");
        if (drillPos != null && drillPos.squareDistanceTo(new Vec3d(getPos())) > 1024 * 1024) {
            drillPos = null;
        }

        // Validation
        boolean isValid = false;
        if (frameBox.isInitialized() && miningBox.isInitialized()) {
            isValid = true;
            EnumFacing validFace = null;
            for (EnumFacing face : EnumFacing.values()) {
                if (face.getAxis() == Axis.Y) continue;
                // We can't read the blockstate yet so instead we'll have to try all possible faces
                if (frameBox.isOnEdge(getPos().offset(face))) {
                    validFace = face;
                    break;
                }
            }
            if (validFace == null) {
                isValid = false;
            } else {

                int fx0 = frameBox.min().getX();
                int fy0 = frameBox.min().getY();
                int fz0 = frameBox.min().getZ();

                int fx1 = frameBox.max().getX();
                int fy1 = frameBox.max().getY();
                int fz1 = frameBox.max().getZ();

                int mx0 = miningBox.min().getX();
                int my0 = miningBox.min().getY();
                int mz0 = miningBox.min().getZ();

                int mx1 = miningBox.max().getX();
                int my1 = miningBox.max().getY();
                int mz1 = miningBox.max().getZ();

                isValid = true //
                    && fx0 + 1 == mx0//
                    && fx1 - 1 == mx1//
                    && fz0 + 1 == mz0//
                    && fz1 - 1 == mz1//
                    && fy0 >= my0//
                    && fy1 - 1 == my1//
                ;
            }
        }
        if (!isValid) {
            frameBox.reset();
            miningBox.reset();
            drillPos = null;
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                frameBox.writeData(buffer);
                miningBox.writeData(buffer);
                buffer.writeBoolean(drillPos != null);
                if (drillPos != null) {
                    MessageUtil.writeVec3d(buffer, drillPos);
                }
                buffer.writeBoolean(currentTask != null);
                if (currentTask != null) {
                    buffer.writeByte(
                        (byte) Arrays.stream(EnumTaskType.values()).filter(type -> type.clazz == currentTask.getClass())
                            .findFirst().orElseThrow(IllegalStateException::new).ordinal()
                    );
                    for (int i = 0; i < 2; i++) {
                        currentTask.toBytes(buffer);
                    }
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                frameBox.readData(buffer);
                miningBox.readData(buffer);
                if (buffer.readBoolean()) {
                    drillPos = MessageUtil.readVec3d(buffer);
                } else {
                    drillPos = null;
                }
                if (buffer.readBoolean()) {
                    int taskId = buffer.readByte();
                    Task task = EnumTaskType.values()[taskId].supplier.apply(this);
                    task.fromBytes(buffer);
                    if (currentTask == null || !currentTask.equals(task)) {
                        currentTask = task;
                        Task tempTask = EnumTaskType.values()[taskId].supplier.apply(this);
                        tempTask.fromBytes(buffer);
                    } else {
                        currentTask.fromBytes(buffer);
                    }
                } else {
                    currentTask = null;
                }
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("battery = " + battery.getDebugString());
        left.add("rate = " + LocaleUtil.localizeMjFlow(debugPowerRate));
        left.add("frameBox");
        left.add(" - min = " + frameBox.min());
        left.add(" - max = " + frameBox.max());
        left.add("miningBox:");
        left.add(" - min = " + miningBox.min());
        left.add(" - max = " + miningBox.max());

        left.add("firstCheckedPoses = " + firstCheckedPoses.size());
        left.add("frameBoxPosesCount = " + frameBoxPosesCount);
        left.add("firstChecked = " + firstChecked);

        BoxIterator iter = boxIterator;
        left.add("current = " + (iter == null ? "null" : iter.getCurrent()));

        Task task = currentTask;
        if (task != null) {
            left.add("task:");
            left.add(" - class = " + task.getClass().getName());
            left.add(" - power = " + LocaleUtil.localizeMj(task.power));
            left.add(" - target = " + LocaleUtil.localizeMj(task.getTarget()));
        } else {
            left.add("task = null");
        }
        left.add("drill = " + drillPos);
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(pos, miningBox);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public DetachedRenderer.IDetachedRenderer getDebugRenderer() {
        return new AdvDebuggerQuarry(this);
    }

    private enum EnumTaskType {
        BREAK_BLOCK(TaskBreakBlock.class, quarry -> quarry.new TaskBreakBlock()),
        ADD_FRAME(TaskAddFrame.class, quarry -> quarry.new TaskAddFrame()),
        MOVE_DRILL(TaskMoveDrill.class, quarry -> quarry.new TaskMoveDrill());

        public final Class<? extends Task> clazz;
        public final Function<TileQuarry, Task> supplier;

        EnumTaskType(Class<? extends Task> clazz, Function<TileQuarry, Task> supplier) {
            this.clazz = clazz;
            this.supplier = supplier;
        }
    }

    private abstract class Task {
        public long power;
        public long clientPower;
        public long prevClientPower;

        NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setLong("power", power);
            return nbt;
        }

        void readFromNBT(NBTTagCompound nbt) {
            power = nbt.getLong("power");
            if (power < 0) {
                power = 0;
            }
        }

        void toBytes(PacketBufferBC buffer) {
            buffer.writeLong(power);
        }

        void fromBytes(PacketBufferBC buffer) {
            power = buffer.readLong();
        }

        void clientTick() {
            prevClientPower = clientPower;
            clientPower = power;
        }

        public abstract long getTarget();

        public long getRequiredPowerThisTick() {
            return Math.max(0, getTarget() - power);
        }

        /** @param target TODO
         * @return {@code true} if this task has been completed, or cancelled. */
        protected abstract boolean onReceivePower(long added, long target);

        protected abstract boolean finish(long added, long target);

        /** @return {@code true} if this task has been completed, or cancelled. */
        final boolean addPower(long microJoules) {
            power += microJoules;
            long target = getTarget();
            if (power >= target) {
                if (!finish(microJoules, target)) {
                    battery.addPower(Math.min(power, battery.getCapacity() - battery.getStored()), false);
                }
                return true;
            } else {
                return onReceivePower(microJoules, target);
            }
        }
    }

    public class TaskBreakBlock extends Task {
        public BlockPos breakPos = BlockPos.ORIGIN;

        TaskBreakBlock() {}

        TaskBreakBlock(BlockPos pos) {
            this.breakPos = pos;
        }

        @Override
        NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = super.serializeNBT();
            nbt.setTag("breakPos", NBTUtilBC.writeBlockPos(breakPos));
            return nbt;
        }

        @Override
        void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            breakPos = NBTUtilBC.readBlockPos(nbt.getTag("breakPos"));
            if (breakPos == null) {
                // We failed to read, abort
                currentTask = null;
            }
        }

        @Override
        void toBytes(PacketBufferBC buffer) {
            super.toBytes(buffer);
            buffer.writeBlockPos(breakPos);
        }

        @Override
        void fromBytes(PacketBufferBC buffer) {
            super.fromBytes(buffer);
            breakPos = buffer.readBlockPos();
        }

        @Override
        public long getTarget() {
            return BlockUtil.computeBlockBreakPower(world, breakPos);
        }

        @Override
        public long getRequiredPowerThisTick() {
            long target = getTarget();
            long req = Math.max(0, target - power);
            double rate = BCBuildersConfig.quarryMaxBlockMineRate;
            if (rate < 0.1) {
                return req;
            }
            rate /= 20; // seconds -> ticks
            rate -= blockPercentSoFar;
            if (rate <= 0) {
                return 0;
            }
            return Math.min(req, (long) (target * rate));
        }

        @Override
        protected boolean onReceivePower(long added, long target) {
            blockPercentSoFar += added / (double) target;
            if (!world.isAirBlock(breakPos)) {
                world.sendBlockBreakProgress(breakPos.hashCode(), breakPos, (int) (power * 9 / getTarget()));
                return false;
            } else {
                return true;
            }
        }

        @Override
        protected boolean finish(long added, long target) {
            blockPercentSoFar += added / (double) target;
            if (!canMine(breakPos)) {
                return true;
            }
            world.sendBlockBreakProgress(breakPos.hashCode(), breakPos, -1);
            Optional<List<ItemStack>> stacks = BlockUtil.breakBlockAndGetDrops(
                (WorldServer) world, breakPos, new ItemStack(Items.DIAMOND_PICKAXE), getOwner(), true
            );
            if (stacks.isPresent()) {
                // The drill pos will be null if we are making the frame: this is when we want to destroy the block, not
                // drop its contents
                if (drillPos != null) {
                    stacks.get().forEach(stack -> InventoryUtil.addToBestAcceptor(world, pos, null, stack));
                }
            }
            check(breakPos);
            return stacks.isPresent();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null) return false;
            if (getClass() != o.getClass()) {
                return false;
            }
            return breakPos.equals(((TaskBreakBlock) o).breakPos);
        }
    }

    public class TaskAddFrame extends Task {
        public BlockPos framePos = BlockPos.ORIGIN;

        TaskAddFrame() {}

        TaskAddFrame(BlockPos framePos) {
            this.framePos = framePos;
        }

        @Override
        NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = super.serializeNBT();
            nbt.setTag("framePos", NBTUtilBC.writeBlockPos(framePos));
            return nbt;
        }

        @Override
        void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            framePos = NBTUtilBC.readBlockPos(nbt.getTag("framePos"));
            if (framePos == null) {
                // We failed to read, abort
                currentTask = null;
            }
        }

        @Override
        void toBytes(PacketBufferBC buffer) {
            super.toBytes(buffer);
            buffer.writeBlockPos(framePos);
        }

        @Override
        void fromBytes(PacketBufferBC buffer) {
            super.fromBytes(buffer);
            framePos = buffer.readBlockPos();
        }

        @Override
        public long getTarget() {
            return 24 * MjAPI.MJ;
        }

        @Override
        protected boolean onReceivePower(long added, long target) {
            return canIgnoreInFrameBox(framePos);
        }

        @Override
        protected boolean finish(long added, long target) {
            if (canIgnoreInFrameBox(framePos)) {
                return false;
            }
            world.setBlockState(framePos, BCBuildersBlocks.frame.getDefaultState());
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null) return false;
            if (getClass() != o.getClass()) {
                return false;
            }
            return framePos.equals(((TaskAddFrame) o).framePos);
        }
    }

    private class TaskMoveDrill extends Task {
        public Vec3d from = Vec3d.ZERO;
        public Vec3d to = Vec3d.ZERO;

        TaskMoveDrill() {}

        TaskMoveDrill(Vec3d from, Vec3d to) {
            this.from = from;
            this.to = to;
        }

        @Override
        NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = super.serializeNBT();
            nbt.setTag("from", NBTUtilBC.writeVec3d(from));
            nbt.setTag("to", NBTUtilBC.writeVec3d(to));
            return nbt;
        }

        @Override
        void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            from = NBTUtilBC.readVec3d(nbt.getTag("from"));
            to = NBTUtilBC.readVec3d(nbt.getTag("to"));
            if (from == null || to == null) {
                // We failed to read. Abort.
                currentTask = null;
            }
        }

        @Override
        void toBytes(PacketBufferBC buffer) {
            super.toBytes(buffer);
            MessageUtil.writeVec3d(buffer, from);
            MessageUtil.writeVec3d(buffer, to);
        }

        @Override
        void fromBytes(PacketBufferBC buffer) {
            super.fromBytes(buffer);
            from = MessageUtil.readVec3d(buffer);
            to = MessageUtil.readVec3d(buffer);
        }

        @Override
        public long getTarget() {
            return (long) (from.distanceTo(to) * 20 * MjAPI.MJ);
        }

        @Override
        public long getRequiredPowerThisTick() {
            long req = Math.max(0, getTarget() - power);

            double max = BCBuildersConfig.quarryMaxFrameMoveSpeed;
            if (max < 0.1) {
                return req;
            }
            max /= 20;
            max -= moveDistanceSoFar;
            if (max <= 0) {
                return 0;
            }
            return Math.min(req, (long) (max * 20 * MjAPI.MJ));
        }

        @Override
        protected boolean onReceivePower(long added, long target) {
            moveDistanceSoFar += added / (double) MjAPI.MJ;
            // Vec3d oldDrillPos = drillPos;
            drillPos = from.scale(1 - power / (double) target).add(to.scale(power / (double) target));
            // moveEntities(oldDrillPos);
            return false;
        }

        @Override
        protected boolean finish(long added, long target) {
            moveDistanceSoFar += added / (double) MjAPI.MJ;
            // Vec3d oldDrillPos = drillPos;
            drillPos = to;
            // moveEntities(oldDrillPos);
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null) return false;
            if (getClass() != o.getClass()) {
                return false;
            }
            TaskMoveDrill other = (TaskMoveDrill) o;
            return from.equals(other.from) && to.equals(other.to);
        }
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.chunkload.ChunkLoaderManager;
import buildcraft.lib.chunkload.IChunkLoadingTile;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.AxisOrder;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.BoxIterator;
import buildcraft.lib.misc.data.EnumAxisOrder;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

// Forge→Fabric migration notes (R.Chen):
//   ITickable.update()              → tick() driven by {@link #ticker()} (BlockEntityTicker)
//   BlockState                     → BlockState
//   LivingEntity                → LivingEntity
//   Direction / Direction.Axis    → Direction / Direction.Axis
//   Identifier                → Identifier
//   NbtCompound                  → NbtCompound; readFromNBT/writeToNBT → readNbt/writeNbt (void)
//   ServerWorld                     → ServerWorld
//   world.isClient                  → world.isClient
//   world.isAirBlock                → world.isAir
//   world.isOutsideBuildHeight      → world.isOutOfHeightLimit
//   world.sendBlockBreakProgress    → world.setBlockBreakingInfo
//   validate()/invalidate()         → cancelRemoval()/markRemoved()
//   ChunkLoaderManager              → migrated to ServerWorld.setChunkForced (see lib.chunkload)
//   MessageUtil.read/writeVec3d     → local read/writeVec3d helpers (PacketBufferBC doubles)
//
//   STUBS (deferred — not in libLeaf yet):
//   - IWorldEventListener / WorldEventListenerAdapter: dropped; the reactive frame/mining re-check is
//     replaced by the existing polling loop over `toCheck`/`boxIterator` in tick().
//   - core.marker.VolumeCache / TileMarkerVolume: the marker-volume frame selection is stubbed; the quarry
//     falls back to its default frame box. Restored when core.marker.volume + core.tile land.
//   - CapUtil.CAP_ITEM_TRANSACTOR + AutomaticProvidingTransactor: item-output capability deferred to the
//     Transfer-API item pass; only the MJ receiver capability is exposed.
//   - BCBuildersBlocks.frame / .quarry: block identity checks + frame placement stubbed (lib.block deferred).
//   - BCBuildersConfig.* / BCCoreConfig.miningMaxDepth: inlined as constants (config layer deferred).
//   - BCBuildersEventDist: quarry validate/invalidate bookkeeping dropped.
//   - AdvDebuggerQuarry / render bounding box: client debug render stubbed.
//   - BlockUtil fluid viscosity / break-power / drops: BlockUtil stubs (see lib.misc.BlockUtil).
public class TileQuarry extends TileBC_Neptune implements IDebuggable, IChunkLoadingTile {
    public static final boolean DEBUG_QUARRY = BCDebugging.shouldDebugLog("builders.quarry");
    private static final long MAX_POWER_PER_TICK = 512 * MjAPI.MJ;
    private static final Identifier ADVANCEMENT_COMPLETE
        = new Identifier("buildcraftbuilders:diggy_diggy_hole");

    // STUB(R.Chen): inlined BCBuildersConfig / BCCoreConfig values (config layer not yet in libLeaf).
    private static final int QUARRY_FRAME_MIN_HEIGHT = 4;
    private static final int QUARRY_MAX_TASKS_PER_TICK = 4;
    private static final int QUARRY_TASK_POWER_DIVISOR = 2;
    private static final double QUARRY_MAX_FRAME_MOVE_SPEED = 0;
    private static final double QUARRY_MAX_BLOCK_MINE_RATE = 0;
    private static final int MINING_MAX_DEPTH = 512;

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
        BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(p -> getPos().getSquaredDistance(p)))
    );
    private final Set<BlockPos> framePlaceFramePoses = new HashSet<>();
    public Task currentTask = null;
    public Vec3d drillPos;
    public Vec3d clientDrillPos;
    public Vec3d prevClientDrillPos;
    private long debugPowerRate = 0;
    private double blockPercentSoFar;
    private double moveDistanceSoFar;
    private boolean firstServerTick = true;

    public TileQuarry(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
        // STUB(R.Chen): caps.addCapabilityInstance(CAP_ITEM_TRANSACTOR, AutomaticProvidingTransactor.INSTANCE,
        //               EnumPipePart.VALUES) — Forge item-output capability; deferred to the Transfer-API
        //               item pass (replace with ItemStorage.SIDED registration in BCBuildersInitializer).
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
        Set<BlockPos> visitedSet = new HashSet<>();
        List<BlockPos> framePositions = new ArrayList<>();

        List<BlockPos> openSet = new ArrayList<>();
        List<BlockPos> nextOpenSet = new ArrayList<>();

        openSet.add(getPos());

        Direction[] order = Direction.values();
        List<Direction> orderAsList = Arrays.asList(order);

        int maxIterationCount = frameBox.getBlocksOnEdgeCount();
        int iterationCount = 0;
        do {
            for (BlockPos p : openSet) {
                Collections.shuffle(orderAsList);
                for (Direction face : order) {
                    BlockPos next = p.offset(face);
                    if (frameBox.isOnEdge(next) && visitedSet.add(next)) {
                        nextOpenSet.add(next);
                        framePositions.add(next);
                    }
                }
            }
            openSet.clear();
            List<BlockPos> t = openSet;
            openSet = nextOpenSet;
            nextOpenSet = t;

            Collections.shuffle(openSet);

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

            iterationCount++;
            if (iterationCount >= maxIterationCount) {
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
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (placer.getWorld().isClient) {
            return;
        }
        Direction facing = world.getBlockState(pos).get(BlockBCBase_Neptune.PROP_FACING);
        BlockPos areaPos = pos.offset(facing.getOpposite());
        BlockEntity tile = world.getBlockEntity(areaPos);
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
        // STUB(R.Chen): core.marker.VolumeCache / TileMarkerVolume frame-volume selection deferred until
        // core.marker.volume + core.tile.TileMarkerVolume are migrated. The quarry falls through to its
        // default frame box below when no IAreaProvider (land-mark) volume is present.
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
        if (max.getY() - min.getY() < QUARRY_FRAME_MIN_HEIGHT) {
            max = new BlockPos(max.getX(), min.getY() + QUARRY_FRAME_MIN_HEIGHT, max.getZ());
        }
        if (world.isOutOfHeightLimit(max)) {
            int dist = max.getY() - min.getY();
            min = min.down(dist);
            max = max.down(dist);
        }
        frameBox.reset();
        frameBox.setMin(min);
        frameBox.setMax(max);
        miningBox.reset();
        int minY = max.getY() - 1 - MINING_MAX_DEPTH;
        if (world.isOutOfHeightLimit(new BlockPos(min.getX(), minY, min.getZ()))) {
            // TODO: Ask the world for the actual minimum height, rather than just assume 0!
            minY = 0;
        }
        miningBox.setMin(new BlockPos(min.getX() + 1, minY, min.getZ() + 1));
        miningBox.setMax(new BlockPos(max.getX() - 1, max.getY() - 1, max.getZ() - 1));
        updatePoses();
    }

    private boolean canMine(BlockPos blockPos) {
        if (world.getBlockState(blockPos).getHardness(world, blockPos) < 0) {
            return false;
        }
        // STUB(R.Chen): Forge fluid viscosity probing deferred; getFluidWithFlowing returns null for now.
        return BlockUtil.getFluidWithFlowing(world, blockPos) == null;
    }

    private boolean canMoveThrough(BlockPos blockPos) {
        if (world.isAir(blockPos)) {
            return true;
        }
        // STUB(R.Chen): fluid passability (viscosity <= 1000) deferred; non-air treated as solid.
        return false;
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
        return !world.isAir(blockPos) && BlockUtil.getFluidWithFlowing(world, blockPos) == null;
    }

    private void check(BlockPos blockPos) {
        frameBreakBlockPoses.remove(blockPos);
        framePlaceFramePoses.remove(blockPos);
        if (shouldBeFrame(blockPos)) {
            // STUB(R.Chen): BCBuildersBlocks.frame identity check deferred (lib.block not in libLeaf).
            // Treated as "not yet a frame block", so the position is queued for the frame-building logic.
            if (canIgnoreInFrameBox(blockPos)) {
                frameBreakBlockPoses.add(blockPos);
            } else {
                framePlaceFramePoses.add(blockPos);
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
        if (!world.isClient) {
            updatePoses();
        }
    }

    @Override
    public void cancelRemoval() {
        super.cancelRemoval();
        // STUB(R.Chen): BCBuildersEventDist.INSTANCE.validateQuarry(this) + world.addEventListener
        // (IWorldEventListener) dropped; reactive re-checks are handled by the polling loop in tick().
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        // STUB(R.Chen): BCBuildersEventDist.INSTANCE.invalidateQuarry(this) + world.removeEventListener dropped.
        if (world != null && !world.isClient) {
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
        // STUB(R.Chen): state.getBlock() == BCBuildersBlocks.quarry identity check deferred (lib.block not in
        // libLeaf). Since this BE only exists where the quarry block is placed, the block check is assumed true.
        if (frameBox.isInitialized()) {
            List<BlockPos> blocksInArea = frameBox.getBlocksInArea();
            blocksInArea.sort(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(pos::getSquaredDistance)));
            frameBoxPosesCount = blocksInArea.size();
            toCheck.addAll(blocksInArea);
            framePoses.addAll(getFramePositions());
            ChunkLoaderManager.loadChunksForTile(this);
        }
    }

    // ITickable.update() → BlockEntityTicker. The owning Block must return this ticker from getTicker(...).
    public static <T extends TileQuarry> BlockEntityTicker<T> ticker() {
        return (world, pos, state, be) -> be.tick();
    }

    public void tick() {
        if (drillPos == null) {
            // STUB(R.Chen): collision boxes (Box) deferred; entity-collision rendering not migrated.
        }

        if (world.isClient) {
            prevClientDrillPos = clientDrillPos;
            clientDrillPos = drillPos;
            if (currentTask != null) {
                currentTask.clientTick();
            }
            return;
        }

        if (firstServerTick) {
            firstServerTick = false;
            updatePoses();
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

        int maxTasks = Math.max(1, (int) (max * QUARRY_MAX_TASKS_PER_TICK / MAX_POWER_PER_TICK));
        boolean sendUpdate = false;
        power_loop: for (int i = 0; i < maxTasks; i++) {

            if (currentTask != null) {

                long needed = currentTask.getRequiredPowerThisTick();
                long added;
                final int mult = QUARRY_TASK_POWER_DIVISOR;
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
                drillPos = Vec3d.of(miningBox.closestInsideTo(pos));
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

                    if (drillPos.squaredDistanceTo(Vec3d.of(boxIterator.getCurrent())) >= 1) {
                        currentTask = new TaskMoveDrill(drillPos, Vec3d.of(boxIterator.getCurrent()));
                        found = true;
                    } else if (canMine(boxIterator.getCurrent())) {
                        currentTask = new TaskBreakBlock(boxIterator.getCurrent());
                        found = true;
                    }

                    if (found) {
                        sendUpdate = true;
                    } else {
                        net.minecraft.util.math.Box box = miningBox.getBoundingBox();
                        if (box.maxX - box.minX == 63 && box.maxZ - box.minZ == 63) {
                            if (getOwner() != null) {
                                AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_COMPLETE);
                            }
                        }
                    }
                }
            }
        }
        debugPowerRate -= max;
        if (sendUpdate) {
            sendNetworkUpdate(NET_RENDER_DATA);
        }
        markChunkDirty();
    }

    /** @return The (currently empty) entity-collision boxes for the drill arm.
     *  STUB(R.Chen): collision boxes (Forge Box / BoundingBoxUtil) deferred to the client/physics
     *  render pass; returns an empty list so entities pass through the drill until then. */
    public List<net.minecraft.util.math.Box> getCollisionBoxes() {
        return Collections.emptyList();
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("box", miningBox.writeToNBT());
        nbt.put("frame", frameBox.writeToNBT());
        if (boxIterator != null) {
            nbt.put("boxIterator", boxIterator.writeToNbt());
        }
        nbt.put("battery", battery.writeToNbt());
        if (currentTask != null) {
            nbt.putByte(
                "currentTaskId", (byte) Arrays.stream(EnumTaskType.values()).filter(
                    type -> type.clazz == currentTask.getClass()
                ).findFirst().orElseThrow(IllegalStateException::new).ordinal()
            );
            nbt.put("currentTaskData", currentTask.createNbt());
        }
        if (drillPos != null) {
            nbt.put("drillPos", NBTUtilBC.writeVec3d(drillPos));
        }
        nbt.putBoolean("firstChecked", firstChecked);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        miningBox.initialize(nbt.getCompound("box"));
        frameBox.initialize(nbt.getCompound("frame"));
        boxIterator = BoxIterator.readFromNbt(nbt.getCompound("boxIterator"));
        battery.readFromNbt(nbt.getCompound("battery"));
        if (nbt.contains("currentTask")) {
            currentTask = EnumTaskType.values()[(int) nbt.getByte("currentTaskId")].supplier.apply(this);
            currentTask.readFromNBT(nbt.getCompound("currentTaskData"));
        } else {
            currentTask = null;
        }
        drillPos = NBTUtilBC.readVec3d(nbt.get("drillPos"));
        firstChecked = nbt.getBoolean("firstChecked");
        if (drillPos != null && drillPos.squaredDistanceTo(Vec3d.of(getPos())) > 1024 * 1024) {
            drillPos = null;
        }

        // Validation
        boolean isValid = false;
        if (frameBox.isInitialized() && miningBox.isInitialized()) {
            isValid = true;
            Direction validFace = null;
            for (Direction face : Direction.values()) {
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
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        if (side == NetSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                frameBox.writeData(buffer);
                miningBox.writeData(buffer);
                buffer.writeBoolean(drillPos != null);
                if (drillPos != null) {
                    writeVec3d(buffer, drillPos);
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
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                frameBox.readData(buffer);
                miningBox.readData(buffer);
                if (buffer.readBoolean()) {
                    drillPos = readVec3d(buffer);
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

    // STUB(R.Chen): MessageUtil.read/writeVec3d not yet migrated; inline double-triple (de)serialization.
    private static void writeVec3d(PacketBufferBC buffer, Vec3d vec) {
        buffer.writeDouble(vec.x);
        buffer.writeDouble(vec.y);
        buffer.writeDouble(vec.z);
    }

    private static Vec3d readVec3d(PacketBufferBC buffer) {
        return new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
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

    @Environment(EnvType.CLIENT)
    @Override
    public DetachedRenderer.IDetachedRenderer getDebugRenderer() {
        // STUB(R.Chen): AdvDebuggerQuarry (client debug overlay) deferred to the client render pass.
        return null;
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

        NbtCompound serializeNBT() {
            NbtCompound nbt = new NbtCompound();
            nbt.putLong("power", power);
            return nbt;
        }

        void readFromNBT(NbtCompound nbt) {
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
        NbtCompound serializeNBT() {
            NbtCompound nbt = super.createNbt();
            nbt.put("breakPos", NBTUtilBC.writeBlockPos(breakPos));
            return nbt;
        }

        @Override
        void readFromNBT(NbtCompound nbt) {
            super.readFromNBT(nbt);
            breakPos = NBTUtilBC.readBlockPos(nbt.get("breakPos"));
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
            double rate = QUARRY_MAX_BLOCK_MINE_RATE;
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
            if (!world.isAir(breakPos)) {
                world.setBlockBreakingInfo(breakPos.hashCode(), breakPos, (int) (power * 9 / getTarget()));
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
            world.setBlockBreakingInfo(breakPos.hashCode(), breakPos, -1);
            Optional<List<ItemStack>> stacks = BlockUtil.breakBlockAndGetDrops(
                (ServerWorld) world, breakPos, new ItemStack(Items.DIAMOND_PICKAXE), getOwner(), true
            );
            if (stacks.isPresent()) {
                // The drill pos will be null if we are making the frame: this is when we want to destroy the block, not
                // drop its contents
                if (drillPos != null) {
                    // STUB(R.Chen): InventoryUtil.addToBestAcceptor (pipe/inventory output) not yet migrated;
                    // drops fall to the world instead of being pushed into adjacent acceptors.
                    stacks.get().forEach(stack -> InventoryUtil.drop(world, pos, stack));
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
        NbtCompound serializeNBT() {
            NbtCompound nbt = super.createNbt();
            nbt.put("framePos", NBTUtilBC.writeBlockPos(framePos));
            return nbt;
        }

        @Override
        void readFromNBT(NbtCompound nbt) {
            super.readFromNBT(nbt);
            framePos = NBTUtilBC.readBlockPos(nbt.get("framePos"));
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
            // STUB(R.Chen): world.setBlockState(framePos, BCBuildersBlocks.frame.getDefaultState()) deferred —
            // BCBuildersBlocks.frame not in libLeaf (lib.block). Frame placement restored once the block lands.
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
        NbtCompound serializeNBT() {
            NbtCompound nbt = super.createNbt();
            nbt.put("from", NBTUtilBC.writeVec3d(from));
            nbt.put("to", NBTUtilBC.writeVec3d(to));
            return nbt;
        }

        @Override
        void readFromNBT(NbtCompound nbt) {
            super.readFromNBT(nbt);
            from = NBTUtilBC.readVec3d(nbt.get("from"));
            to = NBTUtilBC.readVec3d(nbt.get("to"));
            if (from == null || to == null) {
                // We failed to read. Abort.
                currentTask = null;
            }
        }

        @Override
        void toBytes(PacketBufferBC buffer) {
            super.toBytes(buffer);
            writeVec3d(buffer, from);
            writeVec3d(buffer, to);
        }

        @Override
        void fromBytes(PacketBufferBC buffer) {
            super.fromBytes(buffer);
            from = readVec3d(buffer);
            to = readVec3d(buffer);
        }

        @Override
        public long getTarget() {
            return (long) (from.distanceTo(to) * 20 * MjAPI.MJ);
        }

        @Override
        public long getRequiredPowerThisTick() {
            long req = Math.max(0, getTarget() - power);

            double max = QUARRY_MAX_FRAME_MOVE_SPEED;
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
            drillPos = from.multiply(1 - power / (double) target).add(to.multiply(power / (double) target));
            return false;
        }

        @Override
        protected boolean finish(long added, long target) {
            moveDistanceSoFar += added / (double) MjAPI.MJ;
            drillPos = to;
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

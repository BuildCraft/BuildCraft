/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.inventory.AutomaticProvidingTransactor;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.AverageInt;
import buildcraft.lib.misc.data.AxisOrder;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.BoxIterator;
import buildcraft.lib.misc.data.EnumAxisOrder;
import buildcraft.lib.mj.MjBatteryReciver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersEventDist;

public class TileQuarry extends TileBC_Neptune implements ITickable, IDebuggable {
    private final MjBattery battery = new MjBattery(1600L * MjAPI.MJ);
    public final Box frameBox = new Box();
    private final Box miningBox = new Box();
    private BoxIterator boxIterator;
    public Task currentTask = null;
    public Vec3d drillPos;
    public Vec3d clientDrillPos;
    public Vec3d prevClientDrillPos;
    /**
     * Recent power input, in MJ (not micro)
     */
    private final AverageInt recentPowerAverage = new AverageInt(200);

    public TileQuarry() {
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReciver(battery) {
            @Override
            public long receivePower(long microJoules, boolean simulate) {
                long excess = super.receivePower(microJoules, simulate);
                if (!simulate) {
                    recentPowerAverage.push((int) ((microJoules - excess) / MjAPI.MJ));
                }
                return excess;
            }
        }));
        caps.addCapabilityInstance(CapUtil.CAP_ITEM_TRANSACTOR, AutomaticProvidingTransactor.INSTANCE, EnumPipePart.VALUES);
    }

    public List<BlockPos> getFramePositions(IBlockState state) {
        List<BlockPos> framePositions = new ArrayList<>();

        BlockPos min = frameBox.min();
        BlockPos max = frameBox.max();

        for (int x = min.getX(); x <= max.getX(); x++) {
            framePositions.add(new BlockPos(x, min.getY(), min.getZ()));
            framePositions.add(new BlockPos(x, max.getY(), min.getZ()));
            framePositions.add(new BlockPos(x, min.getY(), max.getZ()));
            framePositions.add(new BlockPos(x, max.getY(), max.getZ()));
        }

        for (int z = min.getZ(); z <= max.getZ(); z++) {
            framePositions.add(new BlockPos(min.getX(), min.getY(), z));
            framePositions.add(new BlockPos(max.getX(), min.getY(), z));
            framePositions.add(new BlockPos(min.getX(), max.getY(), z));
            framePositions.add(new BlockPos(max.getX(), max.getY(), z));
        }

        for (int y = min.getY(); y <= max.getY(); y++) {
            framePositions.add(new BlockPos(min.getX(), y, min.getZ()));
            framePositions.add(new BlockPos(max.getX(), y, min.getZ()));
            framePositions.add(new BlockPos(min.getX(), y, max.getZ()));
            framePositions.add(new BlockPos(max.getX(), y, max.getZ()));
        }

        framePositions = new ArrayList<>(new HashSet<>(framePositions));

        framePositions.sort(Comparator.comparing(blockPos ->
            Math.pow(blockPos.getX() - pos.getX(), 2) +
                Math.pow(blockPos.getY() - pos.getY(), 2) +
                Math.pow(blockPos.getZ() - pos.getZ(), 2)
        ));

        List<BlockPos> framePositionsSorted = new ArrayList<>();
        EnumFacing facing = state.getValue(BlockBCBase_Neptune.PROP_FACING).getOpposite();
        framePositionsSorted.add(pos.offset(facing));
        while (framePositions.size() != framePositionsSorted.size()) {
            for (BlockPos blockPos : framePositions) {
                if (!framePositionsSorted.contains(blockPos)) {
                    if (framePositionsSorted.stream()
                        .flatMap(blockPosLocal -> Arrays.stream(EnumFacing.VALUES).map(blockPosLocal::offset))
                        .anyMatch(Predicate.isEqual(blockPos))) {
                        framePositionsSorted.add(blockPos);
                        break;
                    }
                }
            }
        }

        return framePositionsSorted;
    }

    public List<BlockPos> getFramePositions() {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != BCBuildersBlocks.quarry) {
            return Collections.emptyList();
        }
        return getFramePositions(state);
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
        BlockPos min, max;
        if (tile instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) tile;
            min = provider.min();
            max = provider.max();
            provider.removeFromWorld();
        } else {
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
        if (max.getY() - min.getY() < 4) {
            max = new BlockPos(max.getX(), min.getY() + 4, max.getZ());
        }
        frameBox.reset();
        frameBox.setMin(min);
        frameBox.setMax(max);
        miningBox.reset();
        miningBox.setMin(new BlockPos(min.getX() + 1, 0, min.getZ() + 1));
        miningBox.setMax(new BlockPos(max.getX() - 1, max.getY() - 1, max.getZ() - 1));
    }

    private boolean canNotMine(BlockPos blockPos) {
        if (world.getBlockState(blockPos).getBlockHardness(world, blockPos) < 0) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(world, blockPos);
        return fluid != null && fluid.getViscosity() > 1000;
    }

    private boolean canSkip(BlockPos blockPos) {
        Fluid fluid = BlockUtil.getFluidWithFlowing(world, blockPos);
        return fluid != null && fluid.getViscosity() <= 1000;
    }

    @Override
    public void validate() {
        super.validate();
        BCBuildersEventDist.INSTANCE.validateQuarry(this);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        BCBuildersEventDist.INSTANCE.invalidateQuarry(this);
    }

    @Override
    public void update() {
        if (world.isRemote) {
            prevClientDrillPos = clientDrillPos;
            clientDrillPos = drillPos;
            return;
        }
        recentPowerAverage.tick();

        if (!frameBox.isInitialized() || !miningBox.isInitialized()) {
            return;
        }
        BlockPos min = frameBox.min();
        BlockPos max = frameBox.max();

        if (drillPos != null) {
            Map<BlockPos, EnumFacing.Axis> entityPoses = new HashMap<>();

            for (int x = min.getX(); x <= max.getX(); x++) {
                entityPoses.put(new BlockPos(x, max.getY(), drillPos.zCoord + 0.5), EnumFacing.Axis.X);
            }

            for (int y = (int) drillPos.yCoord; y < max.getY(); y++) {
                entityPoses.put(new BlockPos(drillPos.xCoord + 0.5, y, drillPos.zCoord + 0.5), EnumFacing.Axis.Y);
            }

            for (int z = min.getZ(); z <= max.getZ(); z++) {
                entityPoses.put(new BlockPos(drillPos.xCoord + 0.5, max.getY(), z), EnumFacing.Axis.Z);
            }

            /*
            List<EntityQuarryFrame> allEntities = world.getEntitiesWithinAABB(
                    EntityQuarryFrame.class,
                    miningBox.getBoundingBox().union(frameBox.getBoundingBox()).expandXyz(1)
            );
            entityPoses.forEach((currentPos, axis) -> {
                List<EntityQuarryFrame> entities = allEntities.stream()
                        .filter(entity ->
                                entity != null &&
                                        EnumFacing.Axis.values()[entity.getDataManager().get(EntityQuarryFrame.AXIS)] == axis &&
                                        entity.getDataManager().get(EntityQuarryFrame.QUARRY_POS).equals(pos) &&
                                        entity.getDataManager().get(EntityQuarryFrame.FRAME_POS).equals(currentPos))
                        .collect(Collectors.toList());
                if (entities.isEmpty()) {
                    world.spawnEntity(new EntityQuarryFrame(world, pos, currentPos, axis));
                } else {
                    for (EntityQuarryFrame entity : entities) {
                        entity.getDataManager().set(EntityQuarryFrame.TIMEOUT, EntityQuarryFrame.INIT_TIMEOUT);
                    }
                }
            });
            */
        }

        if (currentTask != null) {
            long maxToExtract = MjAPI.MJ * 10;
            if (currentTask.addPower(
                battery.extractPower(
                    0,
                    Math.min(currentTask.getTarget() - currentTask.getPower(), maxToExtract)
                )
            )) {
                currentTask = null;
            }
            sendNetworkUpdate(NET_RENDER_DATA);
            return;
        }

        List<BlockPos> breakPoses = new ArrayList<>();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos framePos = new BlockPos(x, y, z);
                    boolean shouldBeFrame = ((x == min.getX() || x == max.getX()) && (y == min.getY() || y == max.getY())) ||
                        ((y == min.getY() || y == max.getY()) && (z == min.getZ() || z == max.getZ())) ||
                        ((z == min.getZ() || z == max.getZ()) && (x == min.getX() || x == max.getX()));
                    boolean isAir = world.isAirBlock(framePos);
                    if (shouldBeFrame
                        ? !isAir && world.getBlockState(framePos).getBlock() != BCBuildersBlocks.frame
                        : !isAir) {
                        if (!canSkip(framePos)) {
                            breakPoses.add(framePos);
                        }
                    }
                }
            }
        }

        if (breakPoses.size() > 0) {
            double closestDistance = Double.MAX_VALUE;
            BlockPos closestPos = null;

            for (BlockPos breakPos : breakPoses) {
                double distance = breakPos.distanceSq(pos);

                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPos = breakPos;
                }
            }

            if (!canNotMine(closestPos)) {
                drillPos = null;
                currentTask = new TaskBreakBlock(closestPos);
                sendNetworkUpdate(NET_RENDER_DATA);
            }
            return;
        }

        for (BlockPos framePos : getFramePositions()) {
            if (world.isAirBlock(framePos)) {
                drillPos = null;
                currentTask = new TaskAddFrame(framePos);
                sendNetworkUpdate(NET_RENDER_DATA);
                return;
            }
        }

        if ((boxIterator == null || drillPos == null) && miningBox.isInitialized()) {
            boxIterator = new BoxIterator(miningBox, AxisOrder.getFor(EnumAxisOrder.XZY, AxisOrder.Inversion.NNN), true);
            while (world.isAirBlock(boxIterator.getCurrent()) || canSkip(boxIterator.getCurrent())) {
                if (boxIterator.advance() == null) {
                    break;
                }
            }
            drillPos = new Vec3d(miningBox.closestInsideTo(pos));
        }

        if (boxIterator != null && boxIterator.hasNext()) {
            boolean found = false;

            if (drillPos.squareDistanceTo(new Vec3d(boxIterator.getCurrent())) > 2) {
                currentTask = new TaskMoveDrill(drillPos, new Vec3d(boxIterator.getCurrent()));
                found = true;
            } else if (!world.isAirBlock(boxIterator.getCurrent()) && !canSkip(boxIterator.getCurrent())) {
                if (!canNotMine(boxIterator.getCurrent())) {
                    currentTask = new TaskBreakBlock(boxIterator.getCurrent());
                    found = true;
                }
            } else {
                found = true;
                BlockPos next = boxIterator.advance();
                if (next == null) {
                    currentTask = null;
                } else {
                    currentTask = new TaskMoveDrill(drillPos, new Vec3d(next));
                }
            }

            if (found) {
                sendNetworkUpdate(NET_RENDER_DATA);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("box", miningBox.writeToNBT());
        nbt.setTag("frame", frameBox.writeToNBT());
        if (boxIterator != null) {
            nbt.setTag("box_iterator", boxIterator.writeToNbt());
        }
        nbt.setTag("mj_battery", battery.serializeNBT());
        if (currentTask != null) {
            nbt.setTag("currentTask", EnumTaskType.writeToNbt(currentTask));
        }
        if (drillPos != null) {
            nbt.setTag("drill_pos", NBTUtilBC.writeVec3d(drillPos));
        }
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        miningBox.initialize(nbt.getCompoundTag("box"));
        frameBox.initialize(nbt.getCompoundTag("frame"));
        boxIterator = BoxIterator.readFromNbt(nbt.getCompoundTag("box_iterator"));
        battery.deserializeNBT(nbt.getCompoundTag("mj_battery"));

        if (nbt.hasKey("currentTask")) {
            NBTTagCompound nbtTask = nbt.getCompoundTag("currentTask");
            currentTask = EnumTaskType.readFromNbt(this, nbtTask);
        } else {
            currentTask = null;
        }
        drillPos = NBTUtilBC.readVec3d(nbt.getTag("drill_pos"));
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (id == NET_RENDER_DATA) {
            frameBox.writeData(buffer);
            miningBox.writeData(buffer);

            buffer.writeBoolean(drillPos != null);
            if (drillPos != null) {
                MessageUtil.writeVec3d(buffer, drillPos);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (id == NET_RENDER_DATA) {
            frameBox.readData(buffer);
            miningBox.readData(buffer);

            if (buffer.readBoolean()) {
                drillPos = MessageUtil.readVec3d(buffer);
            } else {
                drillPos = null;
            }
        }
    }

    public Iterable<AxisAlignedBB> getCollisionBoxes() {
        if (!frameBox.isInitialized() || drillPos == null) {
            return ImmutableList.of();
        }
        List<AxisAlignedBB> list = new ArrayList<>(3);
        Vec3d min = VecUtil.convertCenter(frameBox.min());
        Vec3d max = VecUtil.convertCenter(frameBox.max());
        min = VecUtil.replaceValue(min, Axis.Y, max.yCoord);

        Vec3d minXAdj = VecUtil.replaceValue(min, Axis.X, drillPos.xCoord + 0.5);
        Vec3d maxXAdj = VecUtil.replaceValue(max, Axis.X, drillPos.xCoord + 0.5);
        list.add(BoundingBoxUtil.makeFrom(minXAdj, maxXAdj, 0.25));

        Vec3d minZAdj = VecUtil.replaceValue(min, Axis.Z, drillPos.zCoord + 0.5);
        Vec3d maxZAdj = VecUtil.replaceValue(max, Axis.Z, drillPos.zCoord + 0.5);
        list.add(BoundingBoxUtil.makeFrom(minZAdj, maxZAdj, 0.25));

        Vec3d realDrillPos = drillPos.addVector(0.5, 0, 0.5);
        Vec3d minYAdj = realDrillPos;
        Vec3d maxYAdj = VecUtil.replaceValue(realDrillPos, Axis.Y, max.yCoord);
        list.add(BoundingBoxUtil.makeFrom(minYAdj, maxYAdj, 0.25));
        return list;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("battery = " + battery.getDebugString());
        left.add("recent power = " + recentPowerAverage.getAverage());
        left.add("frameBox");
        left.add(" - min = " + frameBox.min());
        left.add(" - max = " + frameBox.max());
        left.add("miningBox:");
        left.add(" - min = " + miningBox.min());
        left.add(" - max = " + miningBox.max());
        left.add("current = " + (boxIterator == null ? "null" : boxIterator.getCurrent()));
        if (currentTask != null) {
            left.add("task:");
            left.add(" - class = " + currentTask.getClass().getName());
            left.add(" - power = " + LocaleUtil.localizeMj(currentTask.getPower()));
            left.add(" - target = " + LocaleUtil.localizeMj(currentTask.getTarget()));
        } else {
            left.add("task = null");
        }
        left.add("drill = " + drillPos);
    }

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

    private enum EnumTaskType {
        BREAK_BLOCK((quarry, nbt) -> quarry.new TaskBreakBlock(nbt)),
        ADD_FRAME((quarry, nbt) -> quarry.new TaskAddFrame(nbt)),
        MOVE_DRILL((quarry, nbt) -> quarry.new TaskMoveDrill(nbt));

        public static final EnumTaskType[] VALUES = values();

        public final BiFunction<TileQuarry, NBTTagCompound, Task> constructor;

        EnumTaskType(BiFunction<TileQuarry, NBTTagCompound, Task> constructor) {
            this.constructor = constructor;
        }

        public static Task readFromNbt(TileQuarry quarry, NBTTagCompound nbt) {
            int idx = nbt.getByte("task_idx");
            return VALUES[idx].constructor.apply(quarry, nbt.getCompoundTag("task"));
        }

        public static NBTTagCompound writeToNbt(Task task) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setByte("task_idx", (byte) task.getType().ordinal());
            nbt.setTag("task", task.serializeNBT());
            return nbt;
        }
    }

    private abstract class Task {
        protected long power;

        public Task() {
        }

        public Task(NBTTagCompound nbt) {
            power = nbt.getLong("power");
        }

        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setLong("power", power);
            return nbt;
        }

        public abstract EnumTaskType getType();

        public abstract long getTarget();

        /**
         * @return True if this task has been completed, or cancelled.
         */
        protected abstract boolean onReceivePower();

        protected abstract boolean finish();

        public final long getPower() {
            return power;
        }

        /**
         * @return True if this task has been completed, or cancelled.
         */
        public final boolean addPower(long microJoules) {
            power += microJoules;
            if (power >= getTarget()) {
                if (!finish()) {
                    battery.addPower(Math.min(power, battery.getCapacity() - battery.getStored()), false);
                }
                return true;
            } else {
                return onReceivePower();
            }
        }
    }

    public class TaskBreakBlock extends Task {
        public final BlockPos breakPos;

        TaskBreakBlock(BlockPos pos) {
            this.breakPos = pos;
        }

        public TaskBreakBlock(NBTTagCompound nbt) {
            super(nbt);
            breakPos = NBTUtilBC.readBlockPos(nbt.getTag("pos"));
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = super.serializeNBT();
            nbt.setTag("pos", NBTUtilBC.writeBlockPos(breakPos));
            return nbt;
        }

        @Override
        public EnumTaskType getType() {
            return EnumTaskType.BREAK_BLOCK;
        }

        @Override
        public long getTarget() {
            return BlockUtil.computeBlockBreakPower(world, breakPos);
        }

        @Override
        protected boolean onReceivePower() {
            if (!world.isAirBlock(breakPos)) {
                world.sendBlockBreakProgress(breakPos.hashCode(), breakPos, (int) (power * 9 / getTarget()));
                return false;
            } else {
                return true;
            }
        }

        @Override
        protected boolean finish() {
            EntityPlayer fake = BuildCraftAPI.fakePlayerProvider.getFakePlayer((WorldServer) world, getOwner(), pos);

            IBlockState state = world.getBlockState(breakPos);
            if (canNotMine(breakPos)) {
                return true;
            }

            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, breakPos, state, fake);
            MinecraftForge.EVENT_BUS.post(breakEvent);
            if (!breakEvent.isCanceled()) {
                List<ItemStack> stacks = new ArrayList<>();
                // The drill pos will be null if we are making the frame: this is when we want to destroy the block, not
                // drop its contents
                world.sendBlockBreakProgress(breakPos.hashCode(), breakPos, -1);
                world.destroyBlock(breakPos, true);
                if (drillPos != null) {
                    world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(breakPos)).stream()
                        .map(entity -> ItemTransactorHelper.getTransactorForEntity(entity, EnumFacing.UP))
                        .forEach(transactor -> {
                            ItemStack stack;
                            while (!(stack = transactor.extract(
                                StackFilter.ALL,
                                0,
                                Integer.MAX_VALUE,
                                false
                            )).isEmpty()) {
                                stacks.add(stack);
                            }
                        });

                }
                for (ItemStack stack : stacks) {
                    InventoryUtil.addToBestAcceptor(world, pos, null, stack);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public class TaskAddFrame extends Task {
        public final BlockPos framePos;

        TaskAddFrame(BlockPos framePos) {
            this.framePos = framePos;
        }

        public TaskAddFrame(NBTTagCompound nbt) {
            super(nbt);
            framePos = NBTUtilBC.readBlockPos(nbt.getTag("pos"));
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = super.serializeNBT();
            nbt.setTag("pos", NBTUtilBC.writeBlockPos(framePos));
            return nbt;
        }

        @Override
        public EnumTaskType getType() {
            return EnumTaskType.ADD_FRAME;
        }

        @Override
        public long getTarget() {
            return 24 * MjAPI.MJ;
        }

        @Override
        protected boolean onReceivePower() {
            return !world.isAirBlock(framePos);
        }

        @Override
        protected boolean finish() {
            if (world.isAirBlock(framePos)) {
                world.setBlockState(framePos, BCBuildersBlocks.frame.getDefaultState());
            }
            return true;
        }
    }

    private class TaskMoveDrill extends Task {
        final Vec3d from;
        final Vec3d to;

        TaskMoveDrill(Vec3d from, Vec3d to) {
            this.from = from;
            this.to = to;
        }

        public TaskMoveDrill(NBTTagCompound nbt) {
            super(nbt);
            from = NBTUtilBC.readVec3d(nbt.getTag("from"));
            to = NBTUtilBC.readVec3d(nbt.getTag("to"));
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = super.serializeNBT();
            nbt.setTag("from", NBTUtilBC.writeVec3d(from));
            nbt.setTag("to", NBTUtilBC.writeVec3d(to));
            return nbt;
        }

        @Override
        public EnumTaskType getType() {
            return EnumTaskType.MOVE_DRILL;
        }

        @Override
        public long getTarget() {
            return (long) (from.distanceTo(to) * 20 * MjAPI.MJ);
        }

        private void moveEntities(Vec3d oldDrillPos) {
            if (drillPos.yCoord < oldDrillPos.yCoord) {
                return;
            }
            /*
            List<Entity> moved = new ArrayList<>();
            for (EntityQuarryFrame entityQuarryFrame : world.getEntitiesWithinAABB(
                    EntityQuarryFrame.class,
                    miningBox.getBoundingBox().union(frameBox.getBoundingBox()).expandXyz(1)
            )) {
                AxisAlignedBB collisionBoundingBox = entityQuarryFrame.getCollisionBoundingBox();
                if (collisionBoundingBox != null) {
                    for (Entity entity : world.getEntitiesWithinAABB(
                            Entity.class,
                            collisionBoundingBox.expandXyz(0.001)
                    )) {
                        if (!moved.contains(entity)) {
                            Vec3d newPos = entity.getPositionVector().add(drillPos.subtract(oldDrillPos));
                            entity.setPositionAndUpdate(newPos.xCoord, newPos.yCoord, newPos.zCoord);
                            moved.add(entity);
                        }
                    }
                }
            }
            */
        }

        @Override
        protected boolean onReceivePower() {
            Vec3d oldDrillPos = drillPos;
            drillPos = from.scale(1 - power / (double) getTarget()).add(to.scale(power / (double) getTarget()));
            moveEntities(oldDrillPos);
            return false;
        }

        @Override
        protected boolean finish() {
            Vec3d oldDrillPos = drillPos;
            drillPos = to;
            moveEntities(oldDrillPos);
            return true;
        }
    }
}

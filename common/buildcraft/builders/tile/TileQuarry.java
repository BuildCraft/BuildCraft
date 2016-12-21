package buildcraft.builders.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.entity.EntityQuarryFrame;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.*;
import buildcraft.lib.misc.data.*;
import buildcraft.lib.mj.MjBatteryReciver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class TileQuarry extends TileBC_Neptune implements ITickable, IDebuggable {
    private final MjBattery battery;
    private final MjCapabilityHelper mjCapHelper;
    private Box box = new Box();
    public BlockPos min;
    public BlockPos max;
    private BoxIterator boxIterator;
    public Task currentTask = null;
    public final ItemHandlerSimple invFrames = itemManager.addInvHandler("frames", 9, ItemHandlerManager.EnumAccess.NONE, EnumPipePart.VALUES);
    public Vec3d drillPos;
    public Vec3d clientDrillPos;
    public Vec3d prevClientDrillPos;
    /** Recent power input, in MJ (not micro) */
    private final AverageInt recentPowerAverage = new AverageInt(200);
    private final List<EntityQuarryFrame> xArm = new ArrayList<>();
    private final List<EntityQuarryFrame> yArm = new ArrayList<>();
    private final List<EntityQuarryFrame> zArm = new ArrayList<>();

    public TileQuarry() {
        battery = new MjBattery(1600L * MjAPI.MJ);
        mjCapHelper = new MjCapabilityHelper(new MjBatteryReciver(battery) {
            @Override
            public long receivePower(long microJoules, boolean simulate) {
                long excess = super.receivePower(microJoules, simulate);
                if (!simulate) {
                    recentPowerAverage.push((int) ((microJoules - excess) / MjAPI.MJ));
                }
                return excess;
            }
        });
    }

    public List<BlockPos> getFramePositions() {
        List<BlockPos> framePositions = new ArrayList<>();
        Map<BlockPos, Boolean> placingMap = new HashMap<>();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                BlockPos framePos = new BlockPos(x, min.getY(), z);
                boolean shouldBeFrame = x == min.getX() || x == max.getX() || z == min.getZ() || z == max.getZ();
                if (shouldBeFrame) {
                    placingMap.put(framePos, false);
                }
            }
        }
        BlockPos first = pos.offset(world.getBlockState(pos).getValue(BuildCraftProperties.BLOCK_FACING).getOpposite());
        placingMap.put(first, true);
        framePositions.add(first); // "place" frame near quarry
        BlockPos second = pos.offset(world.getBlockState(pos).getValue(BuildCraftProperties.BLOCK_FACING).getOpposite(), 2);
        placingMap.put(second, true);
        framePositions.add(second); // "place" frame in 2 block
        while (placingMap.size() != framePositions.size()) {
            BlockPos lastPlaced = framePositions.get(framePositions.size() - 1);
            placingMap.keySet().stream().filter(blockPos -> !placingMap.get(blockPos)).filter(blockPos -> Stream.of(EnumFacing.values()).anyMatch(side -> lastPlaced.offset(side).equals(blockPos))).forEach(blockPos -> {
                placingMap.put(blockPos, true);
                framePositions.add(blockPos);
            });
        }
        return framePositions;
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (placer.world.isRemote) {
            return;
        }
        EnumFacing facing = world.getBlockState(getPos()).getValue(BlockBCBase_Neptune.PROP_FACING);
        BlockPos areaPos = getPos().offset(facing.getOpposite());
        TileEntity tile = world.getTileEntity(areaPos);
        if (tile instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) tile;
            box.reset();
            min = provider.min();
            max = provider.max();
            box.setMin(new BlockPos(min.getX() + 1, 0, min.getZ() + 1));
            box.setMax(new BlockPos(max.getX() - 1, min.getY() - 1, max.getZ() - 1));
            provider.removeFromWorld();
        }
    }

    @Override
    public void update() {
        if (world.isRemote) {
            prevClientDrillPos = clientDrillPos;
            clientDrillPos = drillPos;

            if (min != null && max != null && drillPos != null) {
                int xSize = max.getX() - min.getX();
                resizeTo(xArm, xSize);

                int zSize = max.getZ() - min.getZ();
                resizeTo(zArm, zSize);

                int ySize = max.getY() - (int) Math.ceil(drillPos.yCoord);
                resizeTo(yArm, ySize + 10);// it will probably expand on the server at some point, so expand it early
            }

            return;
        }
        recentPowerAverage.tick();

        if (min == null || max == null || box == null) {
            return;
        }

        if (drillPos != null) {
            int xSize = max.getX() - min.getX();
            resizeTo(xArm, xSize);
            for (int x = 0; x < xSize; x++) {
                EntityQuarryFrame current = xArm.get(x);
                BlockPos currentPos = new BlockPos(x + min.getX(), max.getY(), drillPos.zCoord);
                if (current == null || current.isDead) {
                    current = new EntityQuarryFrame(world, this, currentPos, Axis.X, x);
                    BCLog.logger.info("[quarry.frame] Didn't have a frame entity for " + currentPos);
                    world.spawnEntity(current);
                    xArm.set(x, current);
                }
                current.setPosition(currentPos.getX(), currentPos.getY(), currentPos.getZ());
            }

            int zSize = max.getZ() - min.getZ();
            resizeTo(zArm, zSize);
            for (int z = 0; z < zSize; z++) {
                EntityQuarryFrame current = zArm.get(z);
                BlockPos currentPos = new BlockPos(drillPos.xCoord, max.getY(), z + min.getZ());
                if (current == null || current.isDead) {
                    current = new EntityQuarryFrame(world, this, currentPos, Axis.Z, z);
                    BCLog.logger.info("[quarry.frame] Didn't have a frame entity for " + currentPos);
                    world.spawnEntity(current);
                    zArm.set(z, current);
                }
                current.setPosition(currentPos.getX(), currentPos.getY(), currentPos.getZ());
            }

            int ySize = max.getY() - (int) Math.ceil(drillPos.yCoord);
            resizeTo(yArm, ySize);
            for (int y = 0; y < ySize; y++) {
                EntityQuarryFrame current = yArm.get(y);
                BlockPos currentPos = new BlockPos(drillPos.xCoord, max.getY() - y, drillPos.zCoord);
                if (current == null || current.isDead) {
                    current = new EntityQuarryFrame(world, this, currentPos, Axis.Y, y);
                    BCLog.logger.info("[quarry.frame] Didn't have a frame entity for " + currentPos);
                    world.spawnEntity(current);
                    yArm.set(y, current);
                }
                current.setPosition(currentPos.getX(), currentPos.getY(), currentPos.getZ());
            }
        }

        if (currentTask != null) {
            long maxToExtract = MjAPI.MJ * 4;
            if (currentTask.addPower(battery.extractPower(0, Math.min(currentTask.getTarget() - currentTask.getPower(), maxToExtract)))) {
                currentTask = null;
            }
            sendNetworkUpdate(NET_RENDER_DATA);
            return;
        }

        List<BlockPos> breakPoses = new ArrayList<>();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                BlockPos framePos = new BlockPos(x, min.getY(), z);
                boolean shouldBeFrame = x == min.getX() || x == max.getX() || z == min.getZ() || z == max.getZ();
                Block block = world.getBlockState(framePos).getBlock();
                if ((block != Blocks.AIR && !shouldBeFrame) || (block != BCBuildersBlocks.frame && block != Blocks.AIR && shouldBeFrame)) {
                    breakPoses.add(framePos);
                }
            }
        }

        if (breakPoses.size() > 0) {
            double closestDistance = Integer.MAX_VALUE;
            BlockPos closestPos = null;

            for (BlockPos breakPos : breakPoses) {
                double distance = breakPos.distanceSq(pos);

                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPos = breakPos;
                }
            }

            drillPos = null;
            currentTask = new TaskBreakBlock(closestPos);
            sendNetworkUpdate(NET_RENDER_DATA);
            return;
        }

        for (BlockPos framePos : getFramePositions()) {
            Block block = world.getBlockState(framePos).getBlock();
            if (block == Blocks.AIR) {
                drillPos = null;
                if (!invFrames.extract(null, 1, 1, true).isEmpty()) {
                    currentTask = new TaskAddFrame(framePos);
                    sendNetworkUpdate(NET_RENDER_DATA);
                }
                return;
            }
        }

        if (boxIterator == null || drillPos == null) {
            boxIterator = new BoxIterator(box, AxisOrder.getFor(EnumAxisOrder.XZY, AxisOrder.Inversion.NNN), true);
            while (world.isAirBlock(boxIterator.getCurrent())) {
                boxIterator.advance();
            }
            drillPos = new Vec3d(boxIterator.getCurrent());
        }

        if (boxIterator.getMin() != null && boxIterator.getMax() != null) {
            if (!world.isAirBlock(boxIterator.getCurrent())) {
                currentTask = new TaskBreakBlock(boxIterator.getCurrent());
            } else {
                currentTask = new TaskMoveDrill(drillPos, new Vec3d(boxIterator.advance()));
            }

            sendNetworkUpdate(NET_RENDER_DATA);
        }
    }

    private static void resizeTo(List<?> list, int newSize) {
        while (list.size() > newSize) {
            list.remove(list.size() - 1);
        }
        while (list.size() < newSize) {
            list.add(null);
        }
    }

    private List<EntityQuarryFrame> getArmList(Axis axis) {
        switch (axis) {
            default:
            case X:
                return xArm;
            case Y:
                return yArm;
            case Z:
                return zArm;
        }
    }

    public boolean tryPairEntity(EntityQuarryFrame frame, Axis axis, int listIndex) {
        List<EntityQuarryFrame> entities = getArmList(axis);
        if (world.isRemote && entities.size() == 0) {
            int xSize = max.getX() - min.getX();
            resizeTo(xArm, xSize);

            int zSize = max.getZ() - min.getZ();
            resizeTo(zArm, zSize);

            int ySize = max.getY() - (int) Math.ceil(drillPos.yCoord);
            resizeTo(yArm, ySize + 10);// it will probably expand on the server at some point, so expand it early
        }
        if (listIndex < 0 || listIndex >= entities.size()) {
            return false;
        }
        EntityQuarryFrame current = entities.get(listIndex);
        if (current == null || current.isDead || !current.isConnected(this)) {
            if (current != null) {
                current.setDead();
            }
            entities.set(listIndex, frame);
            return true;
        }
        return false;
    }

    public boolean isPaired(EntityQuarryFrame frame, Axis axis, int listIndex) {
        List<EntityQuarryFrame> entities = getArmList(axis);
        if (listIndex < 0 || listIndex >= entities.size()) {
            return false;
        }
        return entities.get(listIndex) == frame;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("box", box.writeToNBT());
        if (min != null) {
            nbt.setTag("min", NBTUtilBC.writeBlockPos(min));
        }
        if (max != null) {
            nbt.setTag("max", NBTUtilBC.writeBlockPos(max));
        }
        if (boxIterator != null) {
            nbt.setTag("box_iterator", boxIterator.writeToNBT());
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
        box.initialize(nbt.getCompoundTag("box"));
        min = NBTUtilBC.readBlockPos(nbt.getTag("min"));
        max = NBTUtilBC.readBlockPos(nbt.getTag("max"));
        boxIterator = new BoxIterator(nbt.getCompoundTag("box_iterator"));
        battery.deserializeNBT(nbt.getCompoundTag("mj_battery"));

        if (nbt.hasKey("currentTask")) {
            NBTTagCompound nbtTask = nbt.getCompoundTag("currentTask");
            currentTask = EnumTaskType.readFromNbt(this, nbtTask);
        } else {
            currentTask = null;
        }
        drillPos = NBTUtilBC.readVec3d(nbt.getTag("drill_pos"));

        if (min != null && max != null && drillPos != null) {
            int xSize = max.getX() - min.getX();
            resizeTo(xArm, xSize);

            int zSize = max.getZ() - min.getZ();
            resizeTo(zArm, zSize);

            int ySize = max.getY() - (int) Math.ceil(drillPos.yCoord);
            resizeTo(yArm, ySize + 10);// it will probably expand on the server at some point, so expand it early
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (id == NET_RENDER_DATA) {
            buffer.writeBoolean(min != null);
            if (min != null) {
                buffer.writeBlockPos(min);
            }
            buffer.writeBoolean(max != null);
            if (max != null) {
                buffer.writeBlockPos(max);
            }
            buffer.writeBoolean(drillPos != null);
            if (drillPos != null) {
                MessageUtil.writeVec3d(buffer, drillPos);
            }
            buffer.writeBoolean(box != null);
            if (box != null) {
                box.writeData(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (id == NET_RENDER_DATA) {
            if (buffer.readBoolean()) {
                min = buffer.readBlockPos();
            } else {
                min = null;
            }
            if (buffer.readBoolean()) {
                max = buffer.readBlockPos();
            } else {
                max = null;
            }
            if (buffer.readBoolean()) {
                drillPos = MessageUtil.readVec3d(buffer);
            } else {
                drillPos = null;
            }
            if (buffer.readBoolean()) {
                box = new Box();
                box.readData(buffer);
            } else {
                box = null;
            }
            if (min != null && max != null && drillPos != null) {
                int xSize = max.getX() - min.getX();
                resizeTo(xArm, xSize);

                int zSize = max.getZ() - min.getZ();
                resizeTo(zArm, zSize);

                int ySize = max.getY() - (int) Math.ceil(drillPos.yCoord);
                resizeTo(yArm, ySize + 10);// it will probably expand on the server at some point, so expand it early
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("battery = " + battery.getDebugString());
        left.add("recent power = " + (int) recentPowerAverage.getAverage());
        left.add("box:");
        left.add(" - min = " + box.min());
        left.add(" - max = " + box.max());
        left.add("min = " + min);
        left.add("max = " + max);
        left.add("current = " + (boxIterator == null ? null : boxIterator.getCurrent()));
        if (currentTask != null) {
            left.add("task:");
            left.add(" - class = " + currentTask.getClass().getName());
            left.add(" - power = " + MjAPI.formatMjShort(currentTask.getPower()));
            left.add(" - target = " + MjAPI.formatMjShort(currentTask.getTarget()));
        } else {
            left.add("task = null");
        }
        left.add("drill = " + drillPos);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return mjCapHelper.hasCapability(capability, facing) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (mjCapHelper.hasCapability(capability, facing)) {
            return mjCapHelper.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(getPos(), box);
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

        public Task() {}

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

        /** @return True if this task has been completed, or cancelled. */
        protected abstract boolean onReceivePower();

        protected abstract void finish();

        public final long getPower() {
            return power;
        }

        /** @return True if this task has been completed, or cancelled. */
        public final boolean addPower(long microJoules) {
            if (microJoules <= 0) {
                return false;
            }
            power += microJoules;
            if (power >= getTarget()) {
                finish();
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
        protected void finish() {
            EntityPlayer fake = FakePlayerUtil.INSTANCE.getFakePlayer((WorldServer) world, TileQuarry.this.pos, TileQuarry.this.getOwner().getOwner());

            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, breakPos, world.getBlockState(breakPos), fake);
            MinecraftForge.EVENT_BUS.post(breakEvent);
            if (!breakEvent.isCanceled()) {
                boolean put = TileQuarry.this.drillPos != null;
                // The drill pos will be null if we are making the frame: this is when we want to destroy the block, not
                // drop its contents
                if (put) {
                    NonNullList<ItemStack> stacks = BlockUtil.getItemStackFromBlock((WorldServer) world, breakPos, TileQuarry.this.getOwner().getOwner());
                    if (stacks != null) {
                        for (int i = 0; i < stacks.size(); i++) {
                            InventoryUtil.addToBestAcceptor(getWorld(), getPos(), null, stacks.get(i));
                        }
                    }
                }
                world.sendBlockBreakProgress(breakPos.hashCode(), breakPos, -1);
                world.destroyBlock(breakPos, false);
            }
        }
    }

    public class TaskAddFrame extends Task {
        public final BlockPos framePos;

        TaskAddFrame(BlockPos pos) {
            this.framePos = pos;
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
            return Math.max(getFramePositions().indexOf(framePos) * 3 * MjAPI.MJ, MjAPI.MJ);
        }

        @Override
        protected boolean onReceivePower() {
            return !world.isAirBlock(framePos);
        }

        @Override
        protected void finish() {
            if (world.isAirBlock(framePos)) {
                ItemStack extracted = invFrames.extract(null, 1, 1, false);
                if (!extracted.isEmpty()) {
                    world.setBlockState(framePos, BCBuildersBlocks.frame.getDefaultState());
                    return;
                }
            }
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
            return 10 * MjAPI.MJ;
        }

        @Override
        protected boolean onReceivePower() {
            drillPos = from.scale(1 - power / (double) getTarget()).add(to.scale(power / (double) getTarget()));
            return false;
        }

        @Override
        protected void finish() {
            drillPos = to;
        }
    }
}

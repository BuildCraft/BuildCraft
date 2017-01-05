package buildcraft.builders.tile;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.entity.EntityQuarryFrame;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.inventory.AutomaticProvidingTransactor;
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
    public final Box frameBox = new Box();
    private final Box miningBox = new Box();
    private BoxIterator boxIterator;
    public Task currentTask = null;
    public final ItemHandlerSimple invFrames = itemManager.addInvHandler("frames",
                                                                         9,
                                                                         ItemHandlerManager.EnumAccess.NONE,
                                                                         EnumPipePart.VALUES);
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
                Math.pow(blockPos.getX() - pos.getX(), 2) + Math.pow(blockPos.getY() - pos.getY(), 2) + Math.pow(blockPos.getZ() - pos.getZ(), 2)
        ));

        List<BlockPos> framePositionsSorted = new ArrayList<>();
        EnumFacing facing = world.getBlockState(getPos()).getValue(BlockBCBase_Neptune.PROP_FACING).getOpposite();
        framePositionsSorted.add(pos.offset(facing));
        while (framePositions.size() != framePositionsSorted.size()) {
            for (BlockPos blockPos : framePositions) {
                if (!framePositionsSorted.contains(blockPos)) {
                    if (framePositionsSorted.stream()
                            .flatMap(blockPosLocal -> Arrays.stream(EnumFacing.values()).map(blockPosLocal::offset))
                            .anyMatch(Predicate.isEqual(blockPos))) {
                        framePositionsSorted.add(blockPos);
                        break;
                    }
                }
            }
        }

        return framePositionsSorted;
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
                    min = getPos().add(1, 0, -5);
                    max = getPos().add(11, 4, 5);
                    break;
                case WEST: // -X
                    min = getPos().add(-11, 0, -5);
                    max = getPos().add(-1, 4, 5);
                    break;
                case SOUTH: // +Z
                    min = getPos().add(-5, 0, 1);
                    max = getPos().add(5, 4, 11);
                    break;
                case NORTH: // -Z
                    min = getPos().add(-5, 0, -11);
                    max = getPos().add(5, 4, -1);
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
        Fluid fluid = BlockUtil.getFluidWithFlowing(world, blockPos);
        return fluid != null && fluid.getViscosity() > 1000;
    }

    private boolean canSkip(BlockPos blockPos) {
        Fluid fluid = BlockUtil.getFluidWithFlowing(world, blockPos);
        return fluid != null && fluid.getViscosity() <= 1000;
    }

    @Override
    public void update() {
        if (world.isRemote) {
            prevClientDrillPos = clientDrillPos;
            clientDrillPos = drillPos;

            if (frameBox.isInitialized() && drillPos != null) {
                BlockPos size = frameBox.size();
                resizeTo(xArm, size.getX());
                resizeTo(zArm, size.getZ());

                int ySize = frameBox.max().getY() - (int) Math.ceil(drillPos.yCoord);
                resizeTo(yArm, ySize + 10);// it will probably expand on the server at some point, so expand it early
            }

            return;
        }
        recentPowerAverage.tick();

        if (!frameBox.isInitialized() || !miningBox.isInitialized()) {
            return;
        }
        final BlockPos min = frameBox.min();
        final BlockPos max = frameBox.max();

        if (drillPos != null) {
            int xSize = frameBox.size().getX();
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
            long maxToExtract = MjAPI.MJ * 10;
            if (currentTask.addPower(battery.extractPower(0,
                                                          Math.min(currentTask.getTarget() - currentTask.getPower(),
                                                                   maxToExtract)))) {
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
                    boolean shouldBeFrame = x == min.getX() || x == max.getX()
                        || y == min.getY()
                        || y == max.getY()
                        || z == min.getZ()
                        || z == max.getZ();
                    Block block = world.getBlockState(framePos).getBlock();
                    if (((block != Blocks.AIR && !shouldBeFrame) || (block != BCBuildersBlocks.frame
                        && block != Blocks.AIR && shouldBeFrame)) && !canSkip(framePos)) {
                        breakPoses.add(framePos);
                    }
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

            if (!canNotMine(closestPos)) {
                drillPos = null;
                currentTask = new TaskBreakBlock(closestPos);
                sendNetworkUpdate(NET_RENDER_DATA);
            }
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

        if ((boxIterator == null || drillPos == null) && miningBox.isInitialized()) {
            boxIterator = new BoxIterator(miningBox,
                                          AxisOrder.getFor(EnumAxisOrder.XZY, AxisOrder.Inversion.NNN),
                                          true);
            while (world.isAirBlock(boxIterator.getCurrent()) || canSkip(boxIterator.getCurrent())) {
                boxIterator.advance();
            }
            drillPos = new Vec3d(miningBox.closestInsideTo(getPos()));
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
                currentTask = new TaskMoveDrill(drillPos, new Vec3d(boxIterator.advance()));
            }

            if (found) {
                sendNetworkUpdate(NET_RENDER_DATA);
            }
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
        if (!frameBox.isInitialized()) {
            return false;
        }
        List<EntityQuarryFrame> entities = getArmList(axis);
        if (world.isRemote && entities.size() == 0) {
            BlockPos size = frameBox.size();
            resizeTo(xArm, size.getX());
            resizeTo(zArm, size.getZ());

            int ySize = frameBox.max().getY() - (int) Math.ceil(drillPos.yCoord);
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

        if (frameBox.isInitialized() && drillPos != null) {
            BlockPos size = frameBox.size();
            resizeTo(xArm, size.getX());
            resizeTo(zArm, size.getZ());

            int ySize = frameBox.max().getY() - (int) Math.ceil(drillPos.yCoord);
            resizeTo(yArm, ySize + 10);// it will probably expand on the server at some point, so expand it early
        }
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
            if (frameBox.isInitialized() && drillPos != null) {
                BlockPos size = frameBox.size();
                resizeTo(xArm, size.getX());
                resizeTo(zArm, size.getZ());

                int ySize = frameBox.max().getY() - (int) Math.ceil(drillPos.yCoord);
                resizeTo(yArm, ySize + 10);// it will probably expand on the server at some point, so expand it early
            }
        }
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
            left.add(" - power = " + MjAPI.formatMjShort(currentTask.getPower()));
            left.add(" - target = " + MjAPI.formatMjShort(currentTask.getTarget()));
        } else {
            left.add("task = null");
        }
        left.add("drill = " + drillPos);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (mjCapHelper.hasCapability(capability, facing)) {
            return mjCapHelper.getCapability(capability, facing);
        }
        if (capability == CapUtil.CAP_ITEM_TRANSACTOR) {
            return (T) AutomaticProvidingTransactor.INSTANCE;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(getPos(), miningBox);
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

        protected abstract boolean finish();

        public final long getPower() {
            return power;
        }

        /** @return True if this task has been completed, or cancelled. */
        public final boolean addPower(long microJoules) {
            power += microJoules;
            if (power >= getTarget()) {
                if (!finish()) {
                    battery.addPower(Math.min(power, battery.getCapacity() - battery.getStored()));
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
            EntityPlayer fake = FakePlayerUtil.INSTANCE.getFakePlayer((WorldServer) world,
                                                                      TileQuarry.this.pos,
                                                                      TileQuarry.this.getOwner());

            IBlockState state = world.getBlockState(breakPos);
            if (state.getBlockHardness(getWorld(), breakPos) < 0) {
                return true;
            }

            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, breakPos, state, fake);
            MinecraftForge.EVENT_BUS.post(breakEvent);
            if (!breakEvent.isCanceled()) {
                boolean put = TileQuarry.this.drillPos != null;
                // The drill pos will be null if we are making the frame: this is when we want to destroy the block, not
                // drop its contents
                if (put) {
                    NonNullList<ItemStack> stacks = BlockUtil.getItemStackFromBlock((WorldServer) world,
                                                                                    breakPos,
                                                                                    TileQuarry.this.getOwner());
                    if (stacks != null) {
                        for (int i = 0; i < stacks.size(); i++) {
                            InventoryUtil.addToBestAcceptor(getWorld(), getPos(), null, stacks.get(i));
                        }
                    }
                }
                world.sendBlockBreakProgress(breakPos.hashCode(), breakPos, -1);
                world.destroyBlock(breakPos, false);
                return true;
            } else {
                return false;
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
            return 24 * MjAPI.MJ;
        }

        @Override
        protected boolean onReceivePower() {
            return !world.isAirBlock(framePos);
        }

        @Override
        protected boolean finish() {
            if (world.isAirBlock(framePos)) {
                ItemStack extracted = invFrames.extract(null, 1, 1, false);
                if (!extracted.isEmpty()) {
                    world.setBlockState(framePos, BCBuildersBlocks.frame.getDefaultState());
                }
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

        @Override
        protected boolean onReceivePower() {
            drillPos = from.scale(1 - power / (double) getTarget()).add(to.scale(power / (double) getTarget()));
            return false;
        }

        @Override
        protected boolean finish() {
            drillPos = to;
            return true;
        }
    }
}

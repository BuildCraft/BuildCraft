package buildcraft.builders.tile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

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
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;

public class TileQuarry extends TileBCInventory_Neptune implements ITickable, IDebuggable {
    private final MjBattery battery;
    private final MjCapabilityHelper mjCapHelper;
    private Box box = new Box();
    public BlockPos min;
    public BlockPos max;
    private BoxIterator boxIterator;
    public Task currentTask = null;
    public final IItemHandlerModifiable invFrames = addInventory("frames", 9, ItemHandlerManager.EnumAccess.NONE, EnumPipePart.VALUES);
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

    public List<BlockPos> getFramePoses() {
        List<BlockPos> framePoses = new ArrayList<>();
        Map<BlockPos, Boolean> placingMap = new HashMap<>();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                BlockPos pos = new BlockPos(x, min.getY(), z);
                boolean shouldBeFrame = x == min.getX() || x == max.getX() || z == min.getZ() || z == max.getZ();
                if (shouldBeFrame) {
                    placingMap.put(pos, false);
                }
            }
        }
        BlockPos first = pos.offset(worldObj.getBlockState(pos).getValue(BuildCraftProperties.BLOCK_FACING).getOpposite());
        placingMap.put(first, true);
        framePoses.add(first); // "place" frame near quarry
        BlockPos second = pos.offset(worldObj.getBlockState(pos).getValue(BuildCraftProperties.BLOCK_FACING).getOpposite(), 2);
        placingMap.put(second, true);
        framePoses.add(second); // "place" frame in 2 block
        while (placingMap.size() != framePoses.size()) {
            BlockPos lastPlaced = framePoses.get(framePoses.size() - 1);
            placingMap.keySet().stream().filter(blockPos -> !placingMap.get(blockPos)).filter(blockPos -> Stream.of(EnumFacing.values()).anyMatch(side -> lastPlaced.offset(side).equals(blockPos))).forEach(blockPos -> {
                placingMap.put(blockPos, true);
                framePoses.add(blockPos);
            });
        }
        return framePoses;
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (placer.worldObj.isRemote) {
            return;
        }
        EnumFacing facing = worldObj.getBlockState(getPos()).getValue(BlockBCBase_Neptune.PROP_FACING);
        BlockPos areaPos = getPos().offset(facing.getOpposite());
        TileEntity tile = worldObj.getTileEntity(areaPos);
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
        if (worldObj.isRemote) {
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
                    current = new EntityQuarryFrame(worldObj, this, currentPos, Axis.X, x);
                    BCLog.logger.info("[quarry.frame] Didn't have a frame entity for " + currentPos);
                    worldObj.spawnEntityInWorld(current);
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
                    current = new EntityQuarryFrame(worldObj, this, currentPos, Axis.Z, z);
                    BCLog.logger.info("[quarry.frame] Didn't have a frame entity for " + currentPos);
                    worldObj.spawnEntityInWorld(current);
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
                    current = new EntityQuarryFrame(worldObj, this, currentPos, Axis.Y, y);
                    BCLog.logger.info("[quarry.frame] Didn't have a frame entity for " + currentPos);
                    worldObj.spawnEntityInWorld(current);
                    yArm.set(y, current);
                }
                current.setPosition(currentPos.getX(), currentPos.getY(), currentPos.getZ());
            }
        }

        if (currentTask != null) {
            long max = MjAPI.MJ * 4;
            if (currentTask.addEnergy(battery.extractPower(0, Math.min(currentTask.getTarget() - currentTask.getPower(), max)))) {
                currentTask = null;
            }
            sendNetworkUpdate(NET_RENDER_DATA);
            return;
        }

        List<BlockPos> breakPoses = new ArrayList<>();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                BlockPos pos = new BlockPos(x, min.getY(), z);
                boolean shouldBeFrame = x == min.getX() || x == max.getX() || z == min.getZ() || z == max.getZ();
                Block block = worldObj.getBlockState(pos).getBlock();
                if ((block != Blocks.AIR && !shouldBeFrame) || (block != BCBuildersBlocks.frame && block != Blocks.AIR && shouldBeFrame)) {
                    breakPoses.add(pos);
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

        for (BlockPos pos : getFramePoses()) {
            Block block = worldObj.getBlockState(pos).getBlock();
            if (block == Blocks.AIR) {
                drillPos = null;
                if (IntStream.range(0, invFrames.getSlots()).anyMatch(slot -> invFrames.getStackInSlot(slot) != null)) {
                    currentTask = new TaskAddFrame(pos);
                    sendNetworkUpdate(NET_RENDER_DATA);
                }
                return;
            }
        }

        if (boxIterator == null || drillPos == null) {
            boxIterator = new BoxIterator(box, AxisOrder.getFor(EnumAxisOrder.XZY, AxisOrder.Inversion.NNN), true);
            while (worldObj.isAirBlock(boxIterator.getCurrent())) {
                boxIterator.advance();
            }
            drillPos = new Vec3d(boxIterator.getCurrent());
        }

        if (boxIterator.getMin() != null && boxIterator.getMax() != null) {
            if (!worldObj.isAirBlock(boxIterator.getCurrent())) {
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
        if (worldObj.isRemote && entities.size() == 0) {
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
            nbt.setTag("min", NBTUtils.writeBlockPos(min));
        }
        if (max != null) {
            nbt.setTag("max", NBTUtils.writeBlockPos(max));
        }
        if (boxIterator != null) {
            nbt.setTag("box_iterator", boxIterator.writeToNBT());
        }
        nbt.setTag("mj_battery", battery.serializeNBT());
        if (currentTask != null) {
            nbt.setInteger("current_task_type", currentTask.getType().ordinal());
            nbt.setTag("current_task_data", currentTask.serializeNBT());
        }
        if (drillPos != null) {
            nbt.setTag("drill_pos", NBTUtils.writeVec3d(drillPos));
        }
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        box.initialize(nbt.getCompoundTag("box"));
        min = NBTUtils.readBlockPos(nbt.getTag("min"));
        max = NBTUtils.readBlockPos(nbt.getTag("max"));
        boxIterator = new BoxIterator(nbt.getCompoundTag("box_iterator"));
        battery.deserializeNBT(nbt.getCompoundTag("mj_battery"));
        if (nbt.hasKey("current_task_type")) {
            try {
                // noinspection unchecked
                currentTask = (Task) EnumTaskType.values()[nbt.getInteger("current_task_type")].clazz.getDeclaredConstructor(TileQuarry.class).newInstance(this);
                if (nbt.hasKey("current_task_data")) {
                    currentTask.deserializeNBT(nbt.getCompoundTag("current_task_data"));
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            currentTask = null;
        }
        drillPos = NBTUtils.readVec3d(nbt.getTag("drill_pos"));

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
            buffer.writeBoolean(currentTask != null);
            if (currentTask != null) {
                buffer.writeInt(currentTask.getType().ordinal());
                currentTask.writeToByteBuf(buffer);
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
                try {
                    // noinspection unchecked
                    currentTask = (Task) EnumTaskType.values()[buffer.readInt()].clazz.getDeclaredConstructor(TileQuarry.class).newInstance(this);
                    currentTask.readFromByteBuf(buffer);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                currentTask = null;
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
        BREAK_BLOCK(TaskBreakBlock.class),
        ADD_FRAME(TaskAddFrame.class),
        MOVE_DRILL(TaskMoveDrill.class);

        public Class clazz;

        EnumTaskType(Class clazz) {
            this.clazz = clazz;
        }
    }

    private abstract class Task implements INBTSerializable<NBTTagCompound> {
        protected long energy = 0;

        public abstract long getTarget();

        /** @return true means that task is canceled */
        protected abstract boolean energyReceived();

        protected abstract void finish();

        public final long getPower() {
            return energy;
        }

        /** @return true means that task is canceled */
        public final boolean addEnergy(long count) {
            if (count == 0) {
                return false;
            }
            energy += count;
            if (energy >= getTarget()) {
                finish();
                return true;
            } else {
                return energyReceived();
            }
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setLong("energy", energy);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            energy = nbt.getLong("energy");
        }

        public void writeToByteBuf(PacketBuffer buf) {
            buf.writeLong(energy);
        }

        public Task readFromByteBuf(PacketBuffer buf) {
            energy = buf.readLong();
            return this;
        }

        public EnumTaskType getType() {
            for (EnumTaskType taskType : EnumTaskType.values()) {
                if (taskType.clazz.isInstance(this)) {
                    return taskType;
                }
            }
            return null;
        }
    }

    public class TaskBreakBlock extends Task {
        public BlockPos pos;

        @SuppressWarnings("unused")
        TaskBreakBlock() {}

        TaskBreakBlock(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public long getTarget() {
            return BlockUtil.computeBlockBreakPower(worldObj, pos);
        }

        @Override
        protected boolean energyReceived() {
            if (!worldObj.isAirBlock(pos)) {
                worldObj.sendBlockBreakProgress(pos.hashCode(), pos, (int) (energy * 9 / getTarget()));
                return false;
            } else {
                return true;
            }
        }

        @Override
        protected void finish() {
            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(worldObj, pos, worldObj.getBlockState(pos), FakePlayerUtil.INSTANCE.getBuildCraftPlayer((WorldServer) worldObj).get());
            MinecraftForge.EVENT_BUS.post(breakEvent);
            if (!breakEvent.isCanceled()) {
                boolean put = TileQuarry.this.drillPos != null;
                // The drill pos will be null if we are making the frame: this is when we want to destroy the block, not
                // drop its contents
                if (put) {
                    List<ItemStack> stacks = BlockUtil.getItemStackFromBlock((WorldServer) worldObj, pos, TileQuarry.this.pos);
                    // noinspection Duplicates
                    if (stacks != null) {
                        for (ItemStack stack : stacks) {
                            InventoryUtil.addToBestAcceptor(getWorld(), getPos(), null, stack);
                        }
                    }
                }
                worldObj.sendBlockBreakProgress(pos.hashCode(), pos, -1);
                worldObj.destroyBlock(pos, false);
            }
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = super.serializeNBT();
            nbt.setTag("pos", NBTUtils.writeBlockPos(pos));
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            pos = NBTUtils.readBlockPos(nbt.getTag("pos"));
        }

        @Override
        public void writeToByteBuf(PacketBuffer buf) {
            super.writeToByteBuf(buf);
            buf.writeInt(pos.getX());
            buf.writeInt(pos.getY());
            buf.writeInt(pos.getZ());
        }

        @Override
        public Task readFromByteBuf(PacketBuffer buf) {
            super.readFromByteBuf(buf);
            pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            return this;
        }
    }

    public class TaskAddFrame extends Task {
        public BlockPos pos;

        @SuppressWarnings("unused")
        TaskAddFrame() {}

        TaskAddFrame(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public long getTarget() {
            return Math.max(getFramePoses().indexOf(pos) * 3 * MjAPI.MJ, MjAPI.MJ);
        }

        @Override
        protected boolean energyReceived() {
            return !worldObj.isAirBlock(pos);
        }

        @Override
        protected void finish() {
            if (worldObj.isAirBlock(pos)) {
                for (int slot = invFrames.getSlots(); slot >= 0; slot--) {
                    ItemStack stackInSlot = invFrames.getStackInSlot(slot);
                    if (stackInSlot != null) {
                        worldObj.setBlockState(pos, BCBuildersBlocks.frame.getDefaultState());
                        invFrames.setStackInSlot(slot, stackInSlot.stackSize > 0 ? new ItemStack(stackInSlot.getItem(), stackInSlot.stackSize - 1) : null);
                        return;
                    }
                }
            }
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = super.serializeNBT();
            nbt.setTag("pos", NBTUtils.writeBlockPos(pos));
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            pos = NBTUtils.readBlockPos(nbt.getTag("pos"));
        }

        @Override
        public void writeToByteBuf(PacketBuffer buf) {
            super.writeToByteBuf(buf);
            buf.writeInt(pos.getX());
            buf.writeInt(pos.getY());
            buf.writeInt(pos.getZ());
        }

        @Override
        public Task readFromByteBuf(PacketBuffer buf) {
            super.readFromByteBuf(buf);
            pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            return this;
        }
    }

    private class TaskMoveDrill extends Task {
        Vec3d from;
        Vec3d to;

        @SuppressWarnings("unused")
        TaskMoveDrill() {}

        TaskMoveDrill(Vec3d from, Vec3d to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public long getTarget() {
            return 10 * MjAPI.MJ;
        }

        @Override
        protected boolean energyReceived() {
            drillPos = from.scale(1 - (double) energy / getTarget()).add(to.scale((double) energy / getTarget()));
            return false;
        }

        @Override
        protected void finish() {
            drillPos = to;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = super.serializeNBT();
            nbt.setTag("from", NBTUtils.writeVec3d(from));
            nbt.setTag("to", NBTUtils.writeVec3d(to));
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            from = NBTUtils.readVec3d(nbt.getTag("from"));
            to = NBTUtils.readVec3d(nbt.getTag("to"));
        }

        @Override
        public void writeToByteBuf(PacketBuffer buf) {
            super.writeToByteBuf(buf);
            buf.writeDouble(from.xCoord);
            buf.writeDouble(from.yCoord);
            buf.writeDouble(from.zCoord);
            buf.writeDouble(to.xCoord);
            buf.writeDouble(to.yCoord);
            buf.writeDouble(to.zCoord);
        }

        @Override
        public Task readFromByteBuf(PacketBuffer buf) {
            super.readFromByteBuf(buf);
            from = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            to = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            return this;
        }
    }
}

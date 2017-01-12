package buildcraft.builders.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.builders.filling.Filling;
import buildcraft.core.marker.volume.EnumAddonSlot;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.fake.FakePlayerBC;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.FakePlayerUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.mj.MjBatteryReciver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackInsertionFunction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class TileFiller extends TileBC_Neptune implements ITickable, IDebuggable {
    public static final int MAX_QUEUE_SIZE = 64;
    public final ItemHandlerSimple invResources =
            itemManager.addInvHandler(
                    "resources",
                    new ItemHandlerSimple(
                            27,
                            (slot, stack) -> Filling.INSTANCE.getItemBlocks().contains(stack.getItem()),
                            StackInsertionFunction.getDefaultInserter(),
                            this::onSlotChange
                    ),
                    EnumAccess.NONE,
                    EnumPipePart.VALUES
            );
    public final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);
    private final IMjReceiver mjReceiver = new MjBatteryReciver(battery);
    private final MjCapabilityHelper mjCapHelper = new MjCapabilityHelper(mjReceiver);
    public AddonFillingPlanner addon;
    public Queue<MutablePair<BlockPos, Long>> breakTasks = new ArrayDeque<>();
    public Queue<MutableTriple<BlockPos, ItemStack, Long>> placeTasks = new ArrayDeque<>();

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (world.isRemote) {
            return;
        }
        IBlockState blockState = world.getBlockState(pos);
        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(world);
        VolumeBox box = volumeBoxes.getBoxAt(pos.offset(blockState.getValue(BlockBCBase_Neptune.PROP_FACING).getOpposite()));
        if (box != null) {
            addon = (AddonFillingPlanner) box.addons
                    .values()
                    .stream()
                    .filter(addon -> addon instanceof AddonFillingPlanner)
                    .findFirst()
                    .orElse(null);
            if (addon != null) {
                box.locks.add(
                        new Lock(
                                new Lock.LockCause.LockCauseBlock(pos, blockState.getBlock()),
                                new Lock.LockTarget.LockTargetResize(),
                                new Lock.LockTarget.LockTargetAddon(addon.getSlot()),
                                new Lock.LockTarget.LockTargetUsedByMachine()
                        )
                );
                volumeBoxes.markDirty();
            }
        }
    }

    @Override
    public void update() {
        battery.tick(getWorld(), getPos());
        if (world.isRemote) {
            return;
        }

        if (addon == null) {
            return;
        }

        breakTasks.removeIf(breakTask -> world.isAirBlock(breakTask.getLeft()));
        placeTasks.removeIf(placeTask -> !world.isAirBlock(placeTask.getLeft()));

        if (breakTasks.size() < MAX_QUEUE_SIZE) {
            List<BlockPos> blocksShouldBeBroken = addon.getBlocksShouldBeBroken();
            blocksShouldBeBroken.sort(Comparator.comparing(blockPos ->
                    Math.pow(blockPos.getX() - pos.getX(), 2) + Math.pow(blockPos.getY() - pos.getY(), 2) + Math.pow(blockPos.getZ() - pos.getZ(), 2)
            ));
            blocksShouldBeBroken.stream()
                    .filter(blockPos -> breakTasks.stream().map(MutablePair::getLeft).noneMatch(Predicate.isEqual(blockPos)))
                    .filter(blockPos -> !world.isAirBlock(blockPos))
                    .map(blockPos ->
                            MutablePair.of(
                                    blockPos,
                                    0L
                            )
                    )
                    .limit(MAX_QUEUE_SIZE - breakTasks.size())
                    .forEach(breakTasks::add);
        }

        if (breakTasks.isEmpty() && placeTasks.size() < MAX_QUEUE_SIZE) {
            if (!invResources.extract(null, 1, 1, true).isEmpty()) {
                List<BlockPos> blocksShouldBePlaced = addon.getBlocksShouldBePlaced();
                blocksShouldBePlaced.sort(Comparator.comparing(blockPos ->
                        100_000 - (Math.pow(blockPos.getX() - pos.getX(), 2) + Math.pow(blockPos.getZ() - pos.getZ(), 2)) +
                                Math.abs(blockPos.getY() - pos.getY()) * 100_000
                ));
                blocksShouldBePlaced.stream()
                        .filter(blockPos -> placeTasks.stream().map(MutableTriple::getLeft).noneMatch(Predicate.isEqual(blockPos)))
                        .filter(blockPos -> world.isAirBlock(blockPos))
                        .map(blockPos ->
                                MutableTriple.of(
                                        blockPos,
                                        invResources.extract(null, 1, 1, false),
                                        0L
                                )
                        )
                        .limit(MAX_QUEUE_SIZE - placeTasks.size())
                        .forEach(placeTasks::add);
            }
        }

        if (!breakTasks.isEmpty()) {
            for (Iterator<MutablePair<BlockPos, Long>> iterator = breakTasks.iterator(); iterator.hasNext(); ) {
                MutablePair<BlockPos, Long> breakTask = iterator.next();
                long target = BlockUtil.computeBlockBreakPower(world, breakTask.getLeft());
                breakTask.setRight(
                        breakTask.getRight() +
                                battery.extractPower(0, Math.min(target - breakTask.getRight(), battery.getCapacity() / breakTasks.size()))
                );
                if (breakTask.getRight() >= target) {
                    BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(
                            world,
                            breakTask.getLeft(),
                            world.getBlockState(breakTask.getLeft()),
                            FakePlayerUtil.INSTANCE.getFakePlayer((WorldServer) world, getPos(), getOwner())
                    );
                    MinecraftForge.EVENT_BUS.post(breakEvent);
                    if (!breakEvent.isCanceled()) {
                        world.sendBlockBreakProgress(breakTask.getLeft().hashCode(), breakTask.getLeft(), -1);
                        world.destroyBlock(breakTask.getLeft(), false);
                    } else {
                        battery.addPower(Math.min(target, battery.getCapacity() - battery.getStored()));
                    }
                    iterator.remove();
                } else {
                    world.sendBlockBreakProgress(breakTask.getLeft().hashCode(), breakTask.getLeft(), (int) ((breakTask.getRight() * 9) / target));
                }
            }
        }

        if (!placeTasks.isEmpty()) {
            for (Iterator<MutableTriple<BlockPos, ItemStack, Long>> iterator = placeTasks.iterator(); iterator.hasNext(); ) {
                MutableTriple<BlockPos, ItemStack, Long> placeTask = iterator.next();
                long target = 4 * MjAPI.MJ;
                placeTask.setRight(
                        placeTask.getRight() +
                                battery.extractPower(0, Math.min(target - placeTask.getRight(), battery.getCapacity() / placeTasks.size()))
                );
                if (placeTask.getRight() >= target) {
                    FakePlayerBC fakePlayer = FakePlayerUtil.INSTANCE.getFakePlayer((WorldServer) world, getPos(), getOwner());
                    fakePlayer.setHeldItem(fakePlayer.getActiveHand(), placeTask.getMiddle());
                    EnumActionResult result = placeTask.getMiddle().onItemUse(
                            fakePlayer,
                            world,
                            placeTask.getLeft(),
                            fakePlayer.getActiveHand(),
                            EnumFacing.UP,
                            0.5F,
                            0.0F,
                            0.5F
                    );
                    if (result != EnumActionResult.SUCCESS) {
                        battery.addPower(Math.min(target, battery.getCapacity() - battery.getStored()));
                        invResources.insert(placeTask.getMiddle(), false, false);
                    }
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
            }
        }
    }

    // Read-write

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("battery", battery.serializeNBT());
        if (addon != null) {
            nbt.setUniqueId("addonBoxId", addon.box.id);
            nbt.setTag("addonSlot", NBTUtilBC.writeEnum(addon.getSlot()));
        }
//        if (currentTaskType != null) {
//            nbt.setTag("currentTaskType", NBTUtilBC.writeEnum(currentTaskType));
//        }
//        if (currentPos != null) {
//            nbt.setTag("currentPos", NBTUtilBC.writeBlockPos(currentPos));
//        }
//        if (stackToPlace != null) {
//            nbt.setTag("stackToPlace", stackToPlace.writeToNBT(new NBTTagCompound()));
//        }
//        nbt.setInteger("progress", progress);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        battery.deserializeNBT(nbt.getCompoundTag("battery"));
        if (nbt.hasKey("addonSlot")) {
            addon = (AddonFillingPlanner) WorldSavedDataVolumeBoxes.get(world)
                    .getBoxFromId(nbt.getUniqueId("addonBoxId"))
                    .addons
                    .get(NBTUtilBC.readEnum(nbt.getTag("addonSlot"), EnumAddonSlot.class));
        }
//        if (nbt.hasKey("currentTaskType")) {
//            currentTaskType = NBTUtilBC.readEnum(nbt.getTag("currentTaskType"), EnumTaskType.class);
//        }
//        if (nbt.hasKey("currentPos")) {
//            currentPos = NBTUtilBC.readBlockPos(nbt.getTag("currentPos"));
//        }
//        if (nbt.hasKey("stackToPlace")) {
//            stackToPlace = new ItemStack(nbt.getCompoundTag("stackToPlace"));
//        }
//        progress = nbt.getInteger("progress");
    }

    // Rendering

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return addon == null ? super.getRenderBoundingBox() : BoundingBoxUtil.makeFrom(pos, addon.box.box);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        T cap = mjCapHelper.getCapability(capability, facing);
        if (cap != null) {
            return cap;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("battery = " + battery.getDebugString());
        left.add("addon = " + addon);
        left.add("break tasks = " + breakTasks.size());
        left.add("place tasks = " + placeTasks.size());
    }

    public enum EnumTaskType {
        BREAK,
        PLACE
    }
}

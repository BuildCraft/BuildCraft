package buildcraft.builders.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.core.marker.volume.EnumAddonSlot;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.FakePlayerUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.mj.MjBatteryReciver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class TileFiller extends TileBC_Neptune implements ITickable, IDebuggable {
    public final ItemHandlerSimple invResources = itemManager.addInvHandler("resources", 27, EnumAccess.NONE, EnumPipePart.VALUES);
    public final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);
    private final IMjReceiver mjReceiver = new MjBatteryReciver(battery);
    private final MjCapabilityHelper mjCapHelper = new MjCapabilityHelper(mjReceiver);
    public AddonFillingPlanner addon;
    public EnumTaskType currentTaskType = null;
    public BlockPos currentPos = null;
    private ItemStack stackToPlace;
    protected int progress = 0;

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

        if (currentTaskType == null) {
            List<BlockPos> blocksShouldBeBroken = addon.getBlocksShouldBeBroken();
            blocksShouldBeBroken.sort(Comparator.comparing(blockPos ->
                    Math.pow(blockPos.getX() - pos.getX(), 2) + Math.pow(blockPos.getY() - pos.getY(), 2) + Math.pow(blockPos.getZ() - pos.getZ(), 2)
            ));
            for (BlockPos blockPos : blocksShouldBeBroken) {
                if (!world.isAirBlock(blockPos)) {
                    currentTaskType = EnumTaskType.BREAK;
                    currentPos = blockPos;
                    break;
                }
            }
        }

        if (currentTaskType == null && !invResources.extract(null, 1, 1, true).isEmpty()) {
            List<BlockPos> blocksShouldBePlaced = addon.getBlocksShouldBePlaced();
            blocksShouldBePlaced.sort(Comparator.comparing(blockPos ->
                    100_000 - (Math.pow(blockPos.getX() - pos.getX(), 2) + Math.pow(blockPos.getZ() - pos.getZ(), 2)) +
                            Math.abs(blockPos.getY() - pos.getY()) * 100_000
            ));
            for (BlockPos blockPos : blocksShouldBePlaced) {
                if (world.isAirBlock(blockPos)) {
                    stackToPlace = invResources.extract(null, 1, 1, false);
                    currentTaskType = EnumTaskType.PLACE;
                    currentPos = blockPos;
                    break;
                }
            }
        }

        if (currentTaskType != null) {
            long target = 0;
            if (currentTaskType == EnumTaskType.BREAK) {
                target = BlockUtil.computeBlockBreakPower(world, currentPos);
            }
            if (currentTaskType == EnumTaskType.PLACE) {
                target = 4 * MjAPI.MJ;
            }
            progress += battery.extractPower(0, target - progress);
            if (progress >= target) {
                progress = 0;
                EntityPlayer fakePlayer = FakePlayerUtil.INSTANCE.getFakePlayer((WorldServer) world, getPos(), getOwner());
                boolean revert = false;
                if (currentTaskType == EnumTaskType.BREAK) {
                    BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, currentPos, world.getBlockState(currentPos), fakePlayer);
                    MinecraftForge.EVENT_BUS.post(breakEvent);
                    if (!breakEvent.isCanceled()) {
                        world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
                        world.destroyBlock(currentPos, false);
                    } else {
                        revert = true;
                    }
                }
                if (currentTaskType == EnumTaskType.PLACE) {
                    fakePlayer.setHeldItem(fakePlayer.getActiveHand(), stackToPlace);
                    EnumActionResult result = stackToPlace.onItemUse(
                            fakePlayer,
                            world,
                            currentPos,
                            fakePlayer.getActiveHand(),
                            EnumFacing.UP,
                            0.5F,
                            0.0F,
                            0.5F
                    );
                    System.out.println(result);
                    if (result != EnumActionResult.SUCCESS) {
                        revert = true;
                        invResources.insert(stackToPlace, false, false);
                    }
                    stackToPlace = null;
                }
                currentTaskType = null;
                currentPos = null;
                if (revert) {
                    battery.addPower(Math.min(target, battery.getCapacity() - battery.getStored()));
                }
            } else {
                if (currentTaskType == EnumTaskType.BREAK) {
                    if (!world.isAirBlock(currentPos)) {
                        world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, (int) ((progress * 9) / target));
                    }
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
        if (currentTaskType != null) {
            nbt.setTag("currentTaskType", NBTUtilBC.writeEnum(currentTaskType));
        }
        if (currentPos != null) {
            nbt.setTag("currentPos", NBTUtilBC.writeBlockPos(currentPos));
        }
        if (stackToPlace != null) {
            nbt.setTag("stackToPlace", stackToPlace.writeToNBT(new NBTTagCompound()));
        }
        nbt.setInteger("progress", progress);
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
        if (nbt.hasKey("currentTaskType")) {
            currentTaskType = NBTUtilBC.readEnum(nbt.getTag("currentTaskType"), EnumTaskType.class);
        }
        if (nbt.hasKey("currentPos")) {
            currentPos = NBTUtilBC.readBlockPos(nbt.getTag("currentPos"));
        }
        if (nbt.hasKey("stackToPlace")) {
            stackToPlace = new ItemStack(nbt.getCompoundTag("stackToPlace"));
        }
        progress = nbt.getInteger("progress");
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
        left.add("task = " + currentTaskType);
        left.add("current = " + currentPos);
        left.add("progress = " + progress);
    }

    public enum EnumTaskType {
        BREAK,
        PLACE
    }
}

package buildcraft.builders.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.builders.filling.Filling;
import buildcraft.builders.snapshot.ITileForTemplateBuilder;
import buildcraft.builders.snapshot.Template;
import buildcraft.builders.snapshot.TemplateBuilder;
import buildcraft.core.marker.volume.EnumAddonSlot;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.BoundingBoxUtil;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

public class TileFiller extends TileBC_Neptune implements ITickable, IDebuggable, ITileForTemplateBuilder {
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
    private final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);
    private final IMjReceiver mjReceiver = new MjBatteryReciver(battery);
    private final MjCapabilityHelper mjCapHelper = new MjCapabilityHelper(mjReceiver);
    public AddonFillingPlanner addon;
    public TemplateBuilder builder = new TemplateBuilder(this);

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
        battery.addPowerChecking(64 * MjAPI.MJ, false);
        if (addon != null || world.isRemote) {
            builder.tick();
        }
        sendNetworkUpdate(NET_RENDER_DATA); // FIXME
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                builder.writeToByteBuf(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                builder.readFromByteBuf(buffer);
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
    @Nonnull
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
    }

    @Override
    public MjBattery getBattery() {
        return battery;
    }

    @Override
    public BlockPos getBuilderPos() {
        return pos;
    }

    @Override
    public Template.BuildingInfo getTemplateBuildingInfo() {
        return addon == null ? null : addon.buildingInfo;
    }

    @Override
    public IItemTransactor getInvResources() {
        return invResources;
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.builders.filling.Filling;
import buildcraft.builders.filling.IParameter;
import buildcraft.builders.snapshot.ITileForTemplateBuilder;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.builders.snapshot.Template;
import buildcraft.builders.snapshot.TemplateBuilder;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.EnumAddonSlot;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public class TileFiller extends TileBC_Neptune implements ITickable, IDebuggable, ITileForTemplateBuilder {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("filler");
    @SuppressWarnings("WeakerAccess")
    public static final int NET_INVERTED = IDS.allocId("INVERTED");
    @SuppressWarnings("WeakerAccess")
    public static final int NET_PARAMETERS = IDS.allocId("PARAMETERS");
    @SuppressWarnings("WeakerAccess")
    public static final int NET_CAN_EXCAVATE = IDS.allocId("CAN_EXCAVATE");

    public final ItemHandlerSimple invResources =
        itemManager.addInvHandler(
            "resources",
            27,
            (slot, stack) -> Filling.getItemBlocks().contains(stack.getItem()),
            EnumAccess.INSERT,
            EnumPipePart.VALUES
        );
    private final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);
    private boolean canExcavate = true;
    private AddonFillingPlanner addon;
    private final List<IParameter> parameters = new ArrayList<>();
    private boolean inverted;
    public final Box box = new Box();
    public boolean markerBox = false;
    private Template.BuildingInfo buildingInfo;
    private List<IParameter> prevParameters;
    private boolean prevInverted;
    public final TemplateBuilder builder = new TemplateBuilder(this);
    private final Runnable updateBuildingInfoListener = () ->
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::updateSnapshot);

    public TileFiller() {
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler,
                                int slot,
                                @Nonnull ItemStack before,
                                @Nonnull ItemStack after) {
        if (!world.isRemote) {
            if (handler == invResources) {
                Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::resourcesChanged);
            }
        }
        super.onSlotChange(handler, slot, before, after);
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (world.isRemote) {
            return;
        }
        IBlockState blockState = world.getBlockState(pos);
        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(world);
        BlockPos offsetPos = pos.offset(blockState.getValue(BlockBCBase_Neptune.PROP_FACING).getOpposite());
        VolumeBox volumeBox = volumeBoxes.getBoxAt(offsetPos);
        TileEntity tile = world.getTileEntity(offsetPos);
        if (volumeBox != null) {
            addon = (AddonFillingPlanner) volumeBox.addons
                .values()
                .stream()
                .filter(AddonFillingPlanner.class::isInstance)
                .findFirst()
                .orElse(null);
            if (addon != null) {
                volumeBox.locks.add(
                    new Lock(
                        new Lock.Cause.CauseBlock(pos, blockState.getBlock()),
                        new Lock.Target.TargetAddon(addon.getSlot()),
                        new Lock.Target.TargetResize(),
                        new Lock.Target.TargetUsedByMachine(
                            Lock.Target.TargetUsedByMachine.EnumType.STRIPES_WRITE
                        )
                    )
                );
                volumeBoxes.markDirty();
                addon.updateBuildingInfo();
            } else {
                box.reset();
                box.setMin(volumeBox.box.min());
                box.setMax(volumeBox.box.max());
                volumeBox.locks.add(
                    new Lock(
                        new Lock.Cause.CauseBlock(pos, blockState.getBlock()),
                        new Lock.Target.TargetResize(),
                        new Lock.Target.TargetUsedByMachine(
                            Lock.Target.TargetUsedByMachine.EnumType.STRIPES_WRITE
                        )
                    )
                );
                volumeBoxes.markDirty();
                parameters.addAll(Filling.initParameters());
                updateBuildingInfo();
            }
        } else if (tile instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) tile;
            box.reset();
            box.setMin(provider.min());
            box.setMax(provider.max());
            markerBox = true;
            provider.removeFromWorld();
            parameters.addAll(Filling.initParameters());
            updateBuildingInfo();
        }
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    @Override
    public void validate() {
        super.validate();
        builder.validate();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        builder.invalidate();
        if (addon != null) {
            addon.updateBuildingInfoListeners.remove(updateBuildingInfoListener);
        }
    }

    @Override
    public void update() {
        battery.tick(getWorld(), getPos());
        battery.addPowerChecking(64 * MjAPI.MJ, false);
        if (!isValid()) {
            return;
        }
        if (addon != null) {
            addon.updateBuildingInfoListeners.add(updateBuildingInfoListener);
        }
        if (!world.isRemote) {
            if (prevParameters == null ||
                !Arrays.equals(prevParameters.toArray(), getParameters().toArray()) ||
                prevInverted != isInverted()) {
                if (prevParameters != null) {
                    builder.cancel();
                }
                builder.updateSnapshot();
            }
        }
        builder.tick();
        prevParameters = getParameters();
        prevInverted = isInverted();
        if (world.getTotalWorldTime() % 3 == 0)
        sendNetworkUpdate(NET_RENDER_DATA); // FIXME
    }

    public boolean isValid() {
        return addon != null || box.isInitialized();
    }

    private void updateBuildingInfo() {
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::cancel);
        buildingInfo = Filling.createBuildingInfo(
            box.min(),
            box.size(),
            parameters,
            inverted
        );
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::updateSnapshot);
    }

    public void sendInverted(boolean value) {
        MessageManager.sendToServer(createMessage(NET_INVERTED, buffer -> buffer.writeBoolean(value)));
    }

    public void sendParameters(List<IParameter> value) {
        MessageManager.sendToServer(createMessage(NET_PARAMETERS, buffer -> {
            buffer.writeInt(value.size());
            value.forEach(parameter -> IParameter.toBytes(buffer, parameter));
        }));
    }

    public void sendCanExcavate(boolean value) {
        MessageManager.sendToServer(createMessage(NET_CAN_EXCAVATE, buffer -> buffer.writeBoolean(value)));
    }

    public boolean isInverted() {
        return addon == null ? inverted : addon.inverted;
    }

    public List<IParameter> getParameters() {
        return addon == null ? parameters : addon.parameters;
    }

    public boolean isCanExcavate() {
        return canExcavate;
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                box.writeData(buffer);
                buffer.writeBoolean(markerBox);
                buffer.writeBoolean(addon != null);
                if (addon != null) {
                    buffer.writeUniqueId(addon.box.id);
                    buffer.writeEnumValue(addon.getSlot());
                }
                builder.writeToByteBuf(buffer);
                writePayload(NET_INVERTED, buffer, side);
                writePayload(NET_PARAMETERS, buffer, side);
                writePayload(NET_CAN_EXCAVATE, buffer, side);
            }
            if (id == NET_PARAMETERS) {
                buffer.writeInt(parameters.size());
                parameters.forEach(parameter -> IParameter.toBytes(buffer, parameter));
            }
            if (id == NET_INVERTED) {
                buffer.writeBoolean(addon == null ? inverted : addon.inverted);
            }
            if (id == NET_CAN_EXCAVATE) {
                buffer.writeBoolean(canExcavate);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                box.readData(buffer);
                markerBox = buffer.readBoolean();
                if (buffer.readBoolean()) {
                    UUID boxId = buffer.readUniqueId();
                    VolumeBox volumeBox = world.isRemote
                        ? ClientVolumeBoxes.INSTANCE.boxes.stream()
                        .filter(localVolumeBox -> localVolumeBox.id.equals(boxId))
                        .findFirst()
                        .orElseThrow(NullPointerException::new)
                        : WorldSavedDataVolumeBoxes.get(world).getBoxFromId(boxId);
                    addon = (AddonFillingPlanner) volumeBox
                        .addons
                        .get(buffer.readEnumValue(EnumAddonSlot.class));
                }
                builder.readFromByteBuf(buffer);
                readPayload(NET_INVERTED, buffer, side, ctx);
                readPayload(NET_PARAMETERS, buffer, side, ctx);
                readPayload(NET_CAN_EXCAVATE, buffer, side, ctx);
            }
            if (id == NET_INVERTED) {
                if (addon == null) {
                    inverted = buffer.readBoolean();
                } else {
                    buffer.readBoolean();
                }
            }
            if (id == NET_PARAMETERS) {
                if (addon == null) {
                    parameters.clear();
                    IntStream.range(0, buffer.readInt())
                        .mapToObj(i -> IParameter.fromBytes(buffer))
                        .forEach(parameters::add);
                } else {
                    IntStream.range(0, buffer.readInt()).forEach(i -> IParameter.fromBytes(buffer));
                }
            }
            if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
            }
        }
        if (side == Side.SERVER) {
            if (id == NET_INVERTED) {
                if (addon == null) {
                    inverted = buffer.readBoolean();
                    updateBuildingInfo();
                } else {
                    addon.inverted = buffer.readBoolean();
                    addon.updateBuildingInfo();
                    WorldSavedDataVolumeBoxes.get(world).markDirty();
                }
            }
            if (id == NET_PARAMETERS) {
                if (addon == null) {
                    parameters.clear();
                    IntStream.range(0, buffer.readInt())
                        .mapToObj(i -> IParameter.fromBytes(buffer))
                        .forEach(parameters::add);
                    updateBuildingInfo();
                } else {
                    addon.parameters.clear();
                    IntStream.range(0, buffer.readInt())
                        .mapToObj(i -> IParameter.fromBytes(buffer))
                        .forEach(addon.parameters::add);
                    addon.updateBuildingInfo();
                    WorldSavedDataVolumeBoxes.get(world).markDirty();
                }
            }
            if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
                sendNetworkUpdate(NET_CAN_EXCAVATE);
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
        nbt.setBoolean("canExcavate", canExcavate);
        nbt.setTag(
            "parameters",
            NBTUtilBC.writeCompoundList(
                parameters.stream()
                    .map(parameter -> IParameter.writeToNBT(new NBTTagCompound(), parameter))
            )
        );
        nbt.setBoolean("inverted", inverted);
        nbt.setTag("box", box.writeToNBT());
        nbt.setBoolean("markerBox", markerBox);
        if (prevParameters != null) {
            nbt.setTag(
                "prevParameters",
                NBTUtilBC.writeCompoundList(
                    prevParameters.stream()
                        .map(parameter -> IParameter.writeToNBT(new NBTTagCompound(), parameter))
                )
            );
        }
        nbt.setBoolean("prevInverted", prevInverted);
        Optional.ofNullable(getBuilder()).ifPresent(builder -> nbt.setTag("builder", builder.serializeNBT()));
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
        canExcavate = nbt.getBoolean("canExcavate");
        NBTUtilBC.readCompoundList(nbt.getTag("parameters"))
            .map(IParameter::readFromNBT)
            .forEach(parameters::add);
        inverted = nbt.getBoolean("inverted");
        box.initialize(nbt.getCompoundTag("box"));
        markerBox = nbt.getBoolean("markerBox");
        if (nbt.hasKey("prevParameters")) {
            prevParameters = new ArrayList<>();
            NBTUtilBC.readCompoundList(nbt.getTag("prevParameters"))
                .map(IParameter::readFromNBT)
                .forEach(prevParameters::add);
        }
        prevInverted = nbt.getBoolean("prevInverted");
        if (addon == null) {
            updateBuildingInfo();
        }
        if (nbt.hasKey("builder")) {
            Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::updateSnapshot);
            Optional.ofNullable(getBuilder()).ifPresent(builder -> builder.deserializeNBT(nbt.getCompoundTag("builder")));
        }
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
        return INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("battery = " + battery.getDebugString());
        left.add("addon = " + addon);
    }

    @Override
    public World getWorldBC() {
        return world;
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
    public boolean canExcavate() {
        return canExcavate;
    }

    @Override
    public SnapshotBuilder getBuilder() {
        return isValid() ? builder : null;
    }

    @Override
    public Template.BuildingInfo getTemplateBuildingInfo() {
        return addon == null ? buildingInfo : addon.buildingInfo;
    }

    @Override
    public IItemTransactor getInvResources() {
        return invResources;
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IBox;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.api.tiles.TilesAPI;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.mj.MjBatteryReciver;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackInsertionChecker;
import buildcraft.lib.tile.item.StackInsertionFunction;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.filling.Filling;
import buildcraft.builders.snapshot.ITileForTemplateBuilder;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.builders.snapshot.Template;
import buildcraft.builders.snapshot.Template.BuildingInfo;
import buildcraft.builders.snapshot.TemplateBuilder;
import buildcraft.core.BCCoreStatements;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public class TileFiller extends TileBC_Neptune
    implements ITickable, IDebuggable, ITileForTemplateBuilder, IFillerStatementContainer {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("filler");
    public static final int NET_CAN_EXCAVATE = IDS.allocId("CAN_EXCAVATE");

    public final ItemHandlerSimple invResources;
    private final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);
    public TemplateBuilder builder = new TemplateBuilder(this);
    private boolean canExcavate = true;

    private boolean invertPattern = false;

    public final Box box = new Box();
    public boolean placedWithVolume = false;
    public boolean placedWithVolumeType = false;

    public IFillerPattern pattern;
    public IStatementParameter[] params;
    private FilledTemplate template;
    private BuildingInfo buildingInfo;

    public TileFiller() {
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReciver(battery)));
        pattern = BCCoreStatements.PATTERN_NONE;
        params = new IStatementParameter[0];
        StackInsertionChecker checker = (slot, stack) -> Filling.INSTANCE.getItemBlocks().contains(stack.getItem());
        StackInsertionFunction insertor = StackInsertionFunction.getDefaultInserter();
        ItemHandlerSimple handler = new ItemHandlerSimple(27, checker, insertor, this::onSlotChange);
        invResources = itemManager.addInvHandler("resources", handler, EnumAccess.BOTH, EnumPipePart.VALUES);
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (world.isRemote) {
            return;
        }
        IBlockState blockState = getCurrentStateForBlock(BCBuildersBlocks.filler);
        if (blockState == null) {
            return;
        }
        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(world);
        EnumFacing facing = blockState.getValue(BlockBCBase_Neptune.PROP_FACING);
        BlockPos volumePos = pos.offset(facing.getOpposite());
        VolumeBox volumeBox = volumeBoxes.getBoxAt(volumePos);
        if (volumeBox != null && volumeBox.box.isOnEdge(volumePos)) {
            box.setMin(volumeBox.box.min());
            box.setMax(volumeBox.box.max());
            placedWithVolume = true;
        } else {
            TileEntity areaProvider = getNeighbourTile(facing.getOpposite());
            if (areaProvider != null) {
                ITileAreaProvider provider = areaProvider.getCapability(TilesAPI.CAP_TILE_AREA_PROVIDER, facing);
                if (provider != null && provider.isValidFromLocation(pos)) {
                    box.setMin(provider.min());
                    box.setMax(provider.max());
                    provider.removeFromWorld();
                }
            }
        }
    }

    @Override
    public void update() {
        // battery.tick(getWorld(), getPos());
        // battery.addPowerChecking(64 * MjAPI.MJ, false);
        if (pattern != null || world.isRemote) {
            // builder.tick();
        }
        sendNetworkUpdate(NET_RENDER_DATA); // FIXME
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                builder.writeToByteBuf(buffer);
            } else if (id == NET_CAN_EXCAVATE) {
                buffer.writeBoolean(canExcavate);
            } else if (id == NET_GUI_DATA) {
                writePayload(NET_CAN_EXCAVATE, buffer, side);
                buffer.writeString(pattern.getUniqueTag());
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                builder.readFromByteBuf(buffer);
            } else if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
            } else if (id == NET_GUI_DATA) {
                readPayload(NET_CAN_EXCAVATE, buffer, side, ctx);
                String uniqueTag = buffer.readString();
                IFillerPattern p = FillerManager.registry.getPattern(uniqueTag);
                if (p != null) {
                    pattern = p;
                }
            }
        }
        if (side == Side.SERVER) {
            if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
                sendNetworkUpdate(NET_CAN_EXCAVATE);
            }
        }
    }

    public void sendCanExcavate(boolean newValue) {
        MessageManager.sendToServer(createMessage(NET_CAN_EXCAVATE, buffer -> buffer.writeBoolean(newValue)));
    }

    // Read-write

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("battery", battery.serializeNBT());
        if (pattern != null && pattern != BCCoreStatements.PATTERN_NONE) {
            nbt.setString("pattern", pattern.getUniqueTag());
            NBTTagList paramList = new NBTTagList();
            for (IStatementParameter param : params) {
                NBTTagCompound cpt = new NBTTagCompound();
                if (param != null) {
                    param.writeToNbt(cpt);
                    cpt.setString("kind", param.getUniqueTag());
                }
                paramList.appendTag(cpt);
            }
            nbt.setTag("params", paramList);
        }
        nbt.setBoolean("canExcavate", canExcavate);
        nbt.setBoolean("invertPattern", invertPattern);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        battery.deserializeNBT(nbt.getCompoundTag("battery"));
        invertPattern = nbt.getBoolean("invertPattern");
        canExcavate = nbt.getBoolean("canExcavate");
    }

    // Rendering

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }

    // @Override
    // @Nonnull
    // @SideOnly(Side.CLIENT)
    // public AxisAlignedBB getRenderBoundingBox() {
    // return addon == null ? super.getRenderBoundingBox() : BoundingBoxUtil.makeFrom(pos, addon.box.box);
    // }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("battery = " + battery.getDebugString());
        // left.add("addon = " + addon);
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
        return builder;
    }

    @Override
    public Template.BuildingInfo getTemplateBuildingInfo() {
        return buildingInfo;
    }

    @Override
    public IItemTransactor getInvResources() {
        return invResources;
    }

    // IFillerStatmentContainer

    @Override
    public TileEntity getTile() {
        return this;
    }

    @Override
    public boolean hasBox() {
        return box.isInitialized();
    }

    @Override
    public IBox getBox() {
        if (!hasBox()) {
            throw new IllegalStateException("Called getBox() when hasBox() returned false!");
        }
        return box;
    }

    @Override
    public void setPattern(IFillerPattern pattern, IStatementParameter[] params) {
        this.pattern = pattern;
        this.params = params;
        if (hasBox()) {
            template = pattern.createTemplate(this, params);
            // buildingInfo = new BuildingInfo(template.min, Rotation.NONE);
        }
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IBox;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.api.tiles.TilesAPI;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackInsertionChecker;
import buildcraft.lib.tile.item.StackInsertionFunction;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.filler.FillerType;
import buildcraft.builders.snapshot.ITileForTemplateBuilder;
import buildcraft.builders.snapshot.Template;
import buildcraft.builders.snapshot.Template.BuildingInfo;
import buildcraft.builders.snapshot.TemplateBuilder;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public class TileFiller extends TileBC_Neptune
    implements ITickable, IDebuggable, ITileForTemplateBuilder, IFillerStatementContainer, IControllable {

    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("filler");
    public static final int NET_CAN_EXCAVATE = IDS.allocId("CAN_EXCAVATE");
    public static final int NET_INVERT = IDS.allocId("INVERT");
    public static final int NET_PATTERN = IDS.allocId("PATTERN");
    public static final int NET_BOX = IDS.allocId("BOX");

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public final ItemHandlerSimple invResources;
    private final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);
    public TemplateBuilder builder = new TemplateBuilder(this);
    private boolean canExcavate = true;
    private boolean invertPattern = false;
    private boolean finished = false;
    private int lockedTicks = 0;
    private Mode mode = Mode.ON;

    public final Box box = new Box();
    public boolean placedWithVolume = false;
    public boolean placedWithVolumeType = false;

    public final FullStatement<IFillerPattern> patternStatement;
    private FilledTemplate patternTemplate;
    private Template blueprintTemplate;
    private BuildingInfo buildingInfo;

    public TileFiller() {
        patternStatement = new FullStatement<>(FillerType.INSTANCE, 4, this::onStatementChange);
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
        caps.addCapabilityInstance(TilesAPI.CAP_CONTROLLABLE, this, EnumPipePart.VALUES);
        // TODO: Add a way of handling any itemstack or block properly,
        // including valid placement locations and removal.
        StackInsertionChecker checker = (slot, stack) -> {
            return stack.getItem() instanceof ItemBlock || stack.getItem() instanceof ItemBlockSpecial;
        };
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
            sendNetworkUpdate(NET_BOX);
        } else {
            TileEntity areaProvider = getNeighbourTile(facing.getOpposite());
            if (areaProvider != null) {
                ITileAreaProvider provider = areaProvider.getCapability(TilesAPI.CAP_TILE_AREA_PROVIDER, facing);
                if (provider != null && provider.isValidFromLocation(pos)) {
                    box.setMin(provider.min());
                    box.setMax(provider.max());
                    provider.removeFromWorld();
                    sendNetworkUpdate(NET_BOX);
                }
            }
        }
    }

    @Override
    public void update() {
        if (world.isRemote) {
            builder.tick();
            patternStatement.canInteract = !isLocked();
            return;
        }
        sendNetworkUpdate(NET_RENDER_DATA);
        lockedTicks--;
        if (lockedTicks < 0) {
            lockedTicks = 0;
        }
        IFillerPattern p = patternStatement.get();
        if (hasBox() && p != null && patternTemplate == null) {
            IStatementParameter[] params = new IStatementParameter[patternStatement.maxParams];
            for (int i = 0; i < params.length; i++) {
                params[i] = patternStatement.get(i);
            }
            patternTemplate = p.createTemplate(this, params);
            if (patternTemplate == null) {
                blueprintTemplate = null;
                buildingInfo = null;
            } else {
                blueprintTemplate = new Template();
                blueprintTemplate.size = patternTemplate.size;
                blueprintTemplate.offset = BlockPos.ORIGIN;
                blueprintTemplate.data = patternTemplate;
                if (invertPattern) {
                    blueprintTemplate.data.invert();
                }
                buildingInfo = blueprintTemplate.new BuildingInfo(patternTemplate.min, Rotation.NONE);
                builder.updateSnapshot();
            }
        }
        if (mode == Mode.OFF || (mode == Mode.ON && finished)) {
            return;
        }
        if (buildingInfo != null) {
            finished = builder.tick();
        }
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
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                builder.writeToByteBuf(buffer);
                writePayload(NET_BOX, buffer, side);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                writePayload(NET_CAN_EXCAVATE, buffer, side);
                writePayload(NET_INVERT, buffer, side);
                writePayload(NET_PATTERN, buffer, side);
                builder.writeToByteBuf(buffer);
                buffer.writeBoolean(finished);
                buffer.writeBoolean(lockedTicks > 0);
                buffer.writeEnumValue(mode);
            } else if (id == NET_BOX) {
                box.writeData(buffer);
            } else if (id == NET_CAN_EXCAVATE) {
                buffer.writeBoolean(canExcavate);
            } else if (id == NET_INVERT) {
                buffer.writeBoolean(invertPattern);
            } else if (id == NET_PATTERN) {
                patternStatement.writeToBuffer(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                builder.readFromByteBuf(buffer);
                readPayload(NET_BOX, buffer, side, ctx);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                readPayload(NET_CAN_EXCAVATE, buffer, side, ctx);
                readPayload(NET_INVERT, buffer, side, ctx);
                readPayload(NET_PATTERN, buffer, side, ctx);
                builder.readFromByteBuf(buffer);
                finished = buffer.readBoolean();
                lockedTicks = buffer.readBoolean() ? 1 : 0;
                mode = buffer.readEnumValue(Mode.class);
            } else if (id == NET_BOX) {
                box.readData(buffer);
            } else if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
            } else if (id == NET_INVERT) {
                invertPattern = buffer.readBoolean();
            } else if (id == NET_PATTERN) {
                patternStatement.readFromBuffer(buffer);
            }
        }
        if (side == Side.SERVER) {
            if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
                sendNetworkGuiUpdate(NET_CAN_EXCAVATE);
            } else if (id == NET_INVERT) {
                invertPattern = buffer.readBoolean();
                sendNetworkGuiUpdate(NET_INVERT);
            } else if (id == NET_PATTERN) {
                if (isLocked()) {
                    new FullStatement<>(FillerType.INSTANCE, 4, (a, b) -> {}).readFromBuffer(buffer);
                } else {
                    patternStatement.readFromBuffer(buffer);
                    sendNetworkUpdate(NET_PATTERN);
                    onStatementChange(null, -1);
                }
            }
        }
    }

    public void sendCanExcavate(boolean newValue) {
        MessageManager.sendToServer(createMessage(NET_CAN_EXCAVATE, buffer -> buffer.writeBoolean(newValue)));
    }

    public void sendInvert(boolean newValue) {
        MessageManager.sendToServer(createMessage(NET_INVERT, buffer -> buffer.writeBoolean(newValue)));
    }

    private void onStatementChange(FullStatement<?> stmnt, int paramIndex) {
        createAndSendMessage(NET_PATTERN, b -> patternStatement.writeToBuffer(b));
        patternTemplate = null;
        blueprintTemplate = null;
        finished = false;
    }

    // Read-write

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("battery", battery.serializeNBT());
        nbt.setTag("pattern", patternStatement.writeToNbt());
        nbt.setTag("box", box.writeToNBT());
        nbt.setBoolean("canExcavate", canExcavate);
        nbt.setBoolean("invertPattern", invertPattern);
        nbt.setBoolean("finished", finished);
        nbt.setByte("lockedTicks", (byte) lockedTicks);
        nbt.setTag("mode", NBTUtilBC.writeEnum(mode));
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        battery.deserializeNBT(nbt.getCompoundTag("battery"));
        invertPattern = nbt.getBoolean("invertPattern");
        canExcavate = nbt.getBoolean("canExcavate");
        finished = nbt.getBoolean("finished");
        lockedTicks = nbt.getByte("lockedTicks");
        mode = NBTUtilBC.readEnum(nbt.getTag("mode"), Mode.class);
        if (mode == null) mode = Mode.ON;
        box.initialize(nbt.getCompoundTag("box"));
        patternStatement.readFromNbt(nbt.getCompoundTag("pattern"));
        patternTemplate = null;
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
        return BoundingBoxUtil.makeFrom(getPos(), box);
    }

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
        left.add("box = " + box);
        left.add("pattern = " + patternStatement.get());
        left.add("mode = " + mode);
        left.add("is_finished = " + finished);
        left.add("lockedTicks = " + lockedTicks);
        left.add("pattern.template = " + (patternTemplate == null ? "null" : "[...]"));
        left.add("builder.template = " + (blueprintTemplate == null ? "null" : "[...]"));
    }

    @Override
    public World getWorldBC() {
        return world;
    }

    public int getCountToPlace() {
        return builder == null ? 0 : builder.leftToPlace;
    }

    public int getCountToBreak() {
        return builder == null ? 0 : builder.leftToBreak;
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

    public boolean shouldInvert() {
        return invertPattern;
    }

    public boolean isFinished() {
        return mode == Mode.LOOP ? false : this.finished;
    }

    public boolean isLocked() {
        return lockedTicks > 0;
    }

    @Override
    public TemplateBuilder getBuilder() {
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
    public World getFillerWorld() {
        return world;
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
        this.patternStatement.set(pattern);
        params = Arrays.copyOf(params, this.patternStatement.maxParams);
        for (int i = 0; i < this.patternStatement.maxParams; i++) {
            this.patternStatement.set(i, params[i]);
        }
        finished = false;
        lockedTicks = 3;
        // patternTemplate = null;
        // blueprintTemplate = null;
    }

    // IControllable

    @Override
    public Mode getControlMode() {
        return mode;
    }

    @Override
    public void setControlMode(Mode mode) {
        if (this.mode == Mode.OFF && mode != Mode.OFF) {
            finished = false;
        }
        this.mode = mode;
    }
}

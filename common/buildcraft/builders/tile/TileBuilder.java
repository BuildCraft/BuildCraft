/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.enums.EnumOptionalSnapshotType;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.mj.MjBatteryReciver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.BlueprintBuilder;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.ITileForBlueprintBuilder;
import buildcraft.builders.snapshot.ITileForTemplateBuilder;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.builders.snapshot.Template;
import buildcraft.builders.snapshot.TemplateBuilder;

public class TileBuilder extends TileBC_Neptune implements ITickable, IDebuggable, ITileForTemplateBuilder, ITileForBlueprintBuilder {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("builder");
    public static final int NET_CAN_EXCAVATE = IDS.allocId("CAN_EXCAVATE");
    public static final int NET_SNAPSHOT_TYPE = IDS.allocId("SNAPSHOT_TYPE");

    public final ItemHandlerSimple invSnapshot = itemManager.addInvHandler("snapshot", 1, EnumAccess.BOTH, EnumPipePart.VALUES);
    public final ItemHandlerSimple invResources = itemManager.addInvHandler("resources", 27, EnumAccess.BOTH, EnumPipePart.VALUES);
    private final TankManager<Tank> tankManager = new TankManager<>();

    private final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);
    private boolean canExcavate = true;

    /** Stores the real path - just a few block positions. */
    public List<BlockPos> path = null;
    /** Stores the real path plus all possible block positions inbetween. */
    private List<BlockPos> basePoses = new ArrayList<>();
    private int currentBasePosIndex = 0;
    private Snapshot snapshot = null;
    public EnumSnapshotType snapshotType = null;
    private Template.BuildingInfo templateBuildingInfo = null;
    private Blueprint.BuildingInfo blueprintBuildingInfo = null;
    public TemplateBuilder templateBuilder = new TemplateBuilder(this);
    public BlueprintBuilder blueprintBuilder = new BlueprintBuilder(this);
    private Box currentBox = new Box();

    public TileBuilder() {
        for (int i = 1; i <= 4; i++) {
            tankManager.add(new Tank("fluid" + i, Fluid.BUCKET_VOLUME * 8, this));
        }
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReciver(battery)));
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankManager, EnumPipePart.VALUES);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable itemHandler, int slot, ItemStack before, ItemStack after) {
        if (itemHandler == invSnapshot) {
            currentBasePosIndex = 0;
            snapshot = null;
            if (after.getItem() instanceof ItemSnapshot) {
                Snapshot.Header header = BCBuildersItems.snapshot.getHeader(after);
                if (header != null) {
                    Snapshot newSnapshot = GlobalSavedDataSnapshots.get(world).getSnapshotByHeader(header);
                    if (newSnapshot != null) {
                        snapshot = newSnapshot;
                    }
                }
            }
            updateSnapshot();
            if (!world.isRemote) {
                sendNetworkUpdate(NET_SNAPSHOT_TYPE);
            }
        }
        super.onSlotChange(itemHandler, slot, before, after);
    }

    private void updateSnapshot() {
        if (getBuilder() != null) {
            getBuilder().cancel();
        }
        if (snapshot != null && getCurrentBasePos() != null) {
            snapshotType = snapshot.getType();
            EnumFacing facing = world.getBlockState(pos).getValue(BlockBCBase_Neptune.PROP_FACING);
            Rotation rotation = Arrays.stream(Rotation.values()).filter(r -> r.rotate(snapshot.facing) == facing).findFirst().orElse(null);
            if (snapshot.getType() == EnumSnapshotType.TEMPLATE) {
                templateBuildingInfo = ((Template) snapshot).new BuildingInfo(getCurrentBasePos(), rotation);
            }
            if (snapshot.getType() == EnumSnapshotType.BLUEPRINT) {
                blueprintBuildingInfo = ((Blueprint) snapshot).new BuildingInfo(getCurrentBasePos(), rotation);
            }
            currentBox = Optional.ofNullable(getBuilder()).map(SnapshotBuilder::getBox).orElse(null);
        } else {
            snapshotType = null;
            templateBuildingInfo = null;
            blueprintBuildingInfo = null;
            currentBox = null;
        }
        if (currentBox == null) {
            currentBox = new Box();
        }
    }

    private void updateBasePoses() {
        basePoses.clear();
        if (path != null) {
            int max = path.size() - 1;
            // Create a list of all the possible block positions on the path that could be used
            basePoses.add(path.get(0));
            for (int i = 1; i <= max; i++) {
                basePoses.addAll(PositionUtil.getAllOnPath(path.get(i - 1), path.get(i)));
            }
        } else {
            basePoses.add(pos.offset(world.getBlockState(pos).getValue(BlockBCBase_Neptune.PROP_FACING).getOpposite()));
        }
    }

    private BlockPos getCurrentBasePos() {
        return currentBasePosIndex < basePoses.size() ? basePoses.get(currentBasePosIndex) : null;
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        EnumFacing facing = world.getBlockState(pos).getValue(BlockBCBase_Neptune.PROP_FACING);
        TileEntity inFront = world.getTileEntity(pos.offset(facing.getOpposite()));
        if (inFront instanceof IPathProvider) {
            IPathProvider provider = (IPathProvider) inFront;
            ImmutableList<BlockPos> copiedPath = ImmutableList.copyOf(provider.getPath());
            if (copiedPath.size() >= 2) {
                path = copiedPath;
                provider.removeFromWorld();
            }
        }
        updateBasePoses();
    }

    @Override
    public void update() {
        battery.tick(getWorld(), getPos());
        battery.addPowerChecking(64 * MjAPI.MJ, false);
        if (getBuilder() != null) {
            if (getBuilder().tick()) {
                if (currentBasePosIndex < basePoses.size() - 1) {
                    currentBasePosIndex++;
                    if (currentBasePosIndex >= basePoses.size()) {
                        currentBasePosIndex = basePoses.size() - 1;
                    }
                    updateSnapshot();
                }
            }
        }
        sendNetworkUpdate(NET_RENDER_DATA); // FIXME
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeInt(path == null ? 0 : path.size());
                if (path != null) {
                    path.forEach(buffer::writeBlockPos);
                }
                buffer.writeBoolean(snapshotType != null);
                if (snapshotType != null) {
                    buffer.writeEnumValue(snapshotType);
                    // noinspection ConstantConditions
                    getBuilder().writeToByteBuf(buffer);
                }
                currentBox.writeData(buffer);
                writePayload(NET_CAN_EXCAVATE, buffer, side);
                writePayload(NET_SNAPSHOT_TYPE, buffer, side);
            }
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.writeData(buffer);
            }
            if (id == NET_CAN_EXCAVATE) {
                buffer.writeBoolean(canExcavate);
            }
            if (id == NET_SNAPSHOT_TYPE) {
                buffer.writeEnumValue(EnumOptionalSnapshotType.fromNullable(snapshotType));
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                path = new ArrayList<>();
                int pathSize = buffer.readInt();
                if (pathSize != 0) {
                    Stream.generate(buffer::readBlockPos).limit(pathSize).forEach(path::add);
                } else {
                    path = null;
                }
                updateBasePoses();
                if (buffer.readBoolean()) {
                    snapshotType = buffer.readEnumValue(EnumSnapshotType.class);
                    // noinspection ConstantConditions
                    getBuilder().readFromByteBuf(buffer);
                } else {
                    snapshotType = null;
                }
                currentBox.readData(buffer);
                readPayload(NET_CAN_EXCAVATE, buffer, side, ctx);
                readPayload(NET_SNAPSHOT_TYPE, buffer, side, ctx);
            }
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.readData(buffer);
            }
            if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
            }
            if (id == NET_SNAPSHOT_TYPE) {
                snapshotType = buffer.readEnumValue(EnumOptionalSnapshotType.class).type;
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 0);
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
        MessageUtil.getWrapper().sendToServer(createMessage(NET_CAN_EXCAVATE, buffer -> buffer.writeBoolean(newValue)));
    }

    // Read-write

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (path != null) {
            nbt.setTag("path", NBTUtilBC.writeCompoundList(path.stream().map(NBTUtil::createPosTag)));
        }
        nbt.setTag("basePoses", NBTUtilBC.writeCompoundList(basePoses.stream().map(NBTUtil::createPosTag)));
        nbt.setBoolean("canExcavate", canExcavate);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("path")) {
            path = NBTUtilBC.readCompoundList(nbt.getTagList("path", Constants.NBT.TAG_COMPOUND)).map(NBTUtil::getPosFromTag).collect(Collectors.toList());
        }
        basePoses = NBTUtilBC.readCompoundList(nbt.getTagList("basePoses", Constants.NBT.TAG_COMPOUND)).map(NBTUtil::getPosFromTag).collect(Collectors.toList());
        canExcavate = nbt.getBoolean("canExcavate");
    }

    // Rendering

    @SideOnly(Side.CLIENT)
    public Box getBox() {
        return currentBox;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(getPos(), getBox(), path);
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
        left.add("basePoses = " + (basePoses == null ? "null" : basePoses.size()));
        left.add("currentBasePosIndex = " + currentBasePosIndex);
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
    public SnapshotBuilder<?> getBuilder() {
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            return templateBuilder;
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            return blueprintBuilder;
        }
        return null;
    }

    @Override
    public Template.BuildingInfo getTemplateBuildingInfo() {
        return templateBuildingInfo;
    }

    @Override
    public Blueprint.BuildingInfo getBlueprintBuildingInfo() {
        return blueprintBuildingInfo;
    }

    @Override
    public IItemTransactor getInvResources() {
        return invResources;
    }

    @Override
    public TankManager<Tank> getTankManager() {
        return tankManager;
    }
}

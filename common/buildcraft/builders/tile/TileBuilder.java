/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.builders.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.enums.EnumOptionalSnapshotType;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITickable;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.block.BlockBuilder;
import buildcraft.builders.container.ContainerBuilder;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.*;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.misc.*;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// public class TileBuilder extends TileBC_Neptune implements ITickable, IDebuggable, ITileForTemplateBuilder, ITileForBlueprintBuilder, IBCTileMenuProvider
public class TileBuilder extends TileBC_Neptune implements ITickable, IDebuggable, ITileForTemplateBuilder, ITileForBlueprintBuilder, IBCTileMenuProvider, IRequestProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("builder");
    public static final int NET_CAN_EXCAVATE = IDS.allocId("CAN_EXCAVATE");
    public static final int NET_SNAPSHOT_TYPE = IDS.allocId("SNAPSHOT_TYPE");
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftbuilders:paving_the_way");

    public final ItemHandlerSimple invSnapshot =
            itemManager
                    .addInvHandler("snapshot", 1,
                            (slot, stack) -> stack.getItem() instanceof ItemSnapshot
                                    && ItemSnapshot.EnumItemSnapshotType.getFromStack(stack).used,
                            EnumAccess.BOTH, EnumPipePart.VALUES
                    );
    public final ItemHandlerSimple invResources =
            itemManager.addInvHandler("resources", 27, EnumAccess.BOTH, EnumPipePart.VALUES);

    private final MjBattery battery = new MjBattery(16000 * MjAPI.MJ);
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
    @SuppressWarnings("WeakerAccess")
    public TemplateBuilder templateBuilder = new TemplateBuilder(this);
    @SuppressWarnings("WeakerAccess")
    public BlueprintBuilder blueprintBuilder = new BlueprintBuilder(this);
    private Box currentBox = new Box();
    private Rotation rotation = null;

    private boolean isDone = false;

    public TileBuilder() {
        super(BCBuildersBlocks.builderTile.get());
        for (int i = 1; i <= 4; i++) {
            tankManager.add(new Tank("tank" + i, FluidAttributes.BUCKET_VOLUME * 8, this) {
                @Override
                protected void onContentsChanged() {
                    super.onContentsChanged();
                    Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::resourcesChanged);
                }
            });
        }
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankManager, EnumPipePart.VALUES);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after) {
        // if (!level.isClientSide)
        if (!level.isClientSide && !StackUtil.isSameItemSameDamageSameTagSameCount(before, after)) {
            if (handler == invSnapshot) {
                currentBasePosIndex = 0;
                snapshot = null;
                if (after.getItem() instanceof ItemSnapshot) {
//                    Snapshot.Header header = BCBuildersItems.snapshot.getHeader(after);
                    Snapshot.Header header = BCBuildersItems.snapshotBLUEPRINT.get().getHeader(after);
                    if (header != null) {
                        Snapshot newSnapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
                        if (newSnapshot != null) {
                            snapshot = newSnapshot;
                        }
                    }
                }
                updateSnapshot(true);
                sendNetworkUpdate(NET_SNAPSHOT_TYPE);
            }
            if (handler == invResources) {
                Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::resourcesChanged);
            }
        }
        super.onSlotChange(handler, slot, before, after);
    }

    @Override
//    public void validate()
    public void clearRemoved() {
//        super.validate();
        super.clearRemoved();
        templateBuilder.validate();
        blueprintBuilder.validate();
    }

    // Calen when this called, #saveAdditional has already been called
    @Override
//    public void invalidate()
    public void setRemoved() {
//        super.invalidate();
        super.setRemoved();
        templateBuilder.invalidate();
        blueprintBuilder.invalidate();
    }

    private void updateSnapshot(boolean canGetFacing) {
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::cancel);
        if (snapshot != null && getCurrentBasePos() != null) {
            snapshotType = snapshot.getType();
            if (canGetFacing) {
                rotation = Arrays.stream(Rotation.values()).filter(r -> r.rotate(snapshot.facing) == level
                        .getBlockState(worldPosition).getValue(BlockBCBase_Neptune.PROP_FACING)).findFirst().orElse(null);
            }
            if (snapshot.getType() == EnumSnapshotType.TEMPLATE) {
                templateBuildingInfo = ((Template) snapshot).new BuildingInfo(getCurrentBasePos(), rotation);
            }
            if (snapshot.getType() == EnumSnapshotType.BLUEPRINT) {
                blueprintBuildingInfo = ((Blueprint) snapshot).new BuildingInfo(getCurrentBasePos(), rotation);
            }
            currentBox = Optional.ofNullable(getBuildingInfo()).map(buildingInfo -> buildingInfo.box).orElse(null);
            Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::updateSnapshot);
        } else {
            snapshotType = null;
            rotation = null;
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
            // Calen: without this, may get Air block when chunk unloading, without BlockBCBase_Neptune.PROP_FACING
            BlockState state = level.getBlockState(worldPosition);
            if (!(state.getBlock() instanceof BlockBuilder)) {
                return;
            }
            basePoses.add(worldPosition.relative(state.getValue(BlockBCBase_Neptune.PROP_FACING).getOpposite()));
        }
    }

    private BlockPos getCurrentBasePos() {
        return currentBasePosIndex < basePoses.size() ? basePoses.get(currentBasePosIndex) : null;
    }

    @Override
//    public void onPlacedBy(EntityLivingBase placer, ItemStack stack)
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        Direction facing = level.getBlockState(worldPosition).getValue(BlockBCBase_Neptune.PROP_FACING);
        TileEntity inFront = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
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
        ITickable.super.update();
        IProfiler profiler = level.getProfiler();
        profiler.push("main");
        profiler.push("power");
        battery.tick(getLevel(), getBlockPos());
        profiler.popPush("builder");
        SnapshotBuilder<?> builder = getBuilder();
        if (builder != null) {
            isDone = builder.tick();
            if (isDone) {
                if (currentBasePosIndex < basePoses.size() - 1) {
                    currentBasePosIndex++;
                    if (currentBasePosIndex == basePoses.size() && currentBasePosIndex > 1)
                        AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT);
                    if (currentBasePosIndex >= basePoses.size()) {
                        currentBasePosIndex = basePoses.size() - 1;
                    }
                    updateSnapshot(true);
                }
            }
        }
        profiler.popPush("net_update");
        sendNetworkUpdate(NET_RENDER_DATA); // FIXME
        profiler.pop();
        profiler.pop();
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeInt(path == null ? 0 : path.size());
                if (path != null) {
                    path.forEach((p) -> MessageUtil.writeBlockPos(buffer, p));
                }
                buffer.writeBoolean(snapshotType != null);
                if (snapshotType != null) {
                    buffer.writeEnum(snapshotType);
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
                // Calen: to update blockstate
                BlockState state = level.getBlockState(worldPosition);
                if (state.getBlock() instanceof BlockBCBase_Neptune) {
                    BlockBCBase_Neptune blockBC = (BlockBCBase_Neptune) state.getBlock();
                    blockBC.checkActualStateAndUpdate(state, level, worldPosition, this);
                }

                buffer.writeEnum(EnumOptionalSnapshotType.fromNullable(snapshotType));
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_RENDER_DATA) {
                path = new ArrayList<>();
                int pathSize = buffer.readInt();
                if (pathSize != 0) {
                    for (int i = 0; i < pathSize; i++) {
                        path.add(MessageUtil.readBlockPos(buffer));
                    }
                } else {
                    path = null;
                }
                // Calen: calling level.getBlockState when loading world will get Air Block
                runWhenWorldNotNull(this::updateBasePoses, true);
                if (buffer.readBoolean()) {
                    snapshotType = buffer.readEnum(EnumSnapshotType.class);
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
                EnumSnapshotType old = snapshotType;
                snapshotType = buffer.readEnum(EnumOptionalSnapshotType.class).type;
                if (old != snapshotType) {
                    redrawBlock();
                }
            }
        }
        if (side == NetworkDirection.PLAY_TO_SERVER) {
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
//    public CompoundNBT writeToNBT(CompoundNBT nbt)
    public CompoundNBT save(CompoundNBT nbt) {
//        super.writeToNBT(nbt);
        super.save(nbt);
        if (path != null) {
            nbt.put("path", NBTUtilBC.writeCompoundList(path.stream().map(NBTUtil::writeBlockPos)));
        }
        nbt.put("basePoses", NBTUtilBC.writeCompoundList(basePoses.stream().map(NBTUtil::writeBlockPos)));
        nbt.putBoolean("canExcavate", canExcavate);
        nbt.put("rotation", NBTUtilBC.writeEnum(rotation));
        Optional.ofNullable(getBuilder()).ifPresent(builder -> nbt.put("builder", builder.serializeNBT()));
        return nbt;
    }

    @Override
//    public void readFromNBT(CompoundNBT nbt)
    public void load(BlockState state, CompoundNBT nbt) {
//        super.readFromNBT(nbt);
        super.load(state, nbt);
        if (nbt.contains("path")) {
            path =
                    NBTUtilBC.readCompoundList(nbt.get("path")).map(NBTUtil::readBlockPos).collect(Collectors.toList());
        }
        basePoses = NBTUtilBC.readCompoundList(nbt.get("basePoses")).map(NBTUtil::readBlockPos)
                .collect(Collectors.toList());
        canExcavate = nbt.getBoolean("canExcavate");
        rotation = NBTUtilBC.readEnum(nbt.get("rotation"), Rotation.class);
        // Calen: don't save/load currentBox/currentBasePosIndex/snapshotType/snapshot withNBT, or this will make the builder destroy the placed block and place again
        // Calen FIX: the items prepared to build will not disappear after tileentity reloaded in 1.18.2
        runWhenWorldNotNull(() ->
                {
                    // Calen FIX: the items prepared to build will not disappear after tileentity reloaded in 1.18.2
                    if (snapshot == null) {
                        // Calen: load snapshot, this should before builder#deserializeNBT
                        itemManager.getCapability(CapUtil.CAP_ITEMS).ifPresent(h ->
                        {
                            ItemStack stack = h.getStackInSlot(0);
                            if (stack.getItem() instanceof ItemSnapshot) {
                                ItemSnapshot itemSnapshot = (ItemSnapshot) stack.getItem();
                                Snapshot.Header header = itemSnapshot.getHeader(stack);
                                snapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
                            }
                        });
                    }
                    if (nbt.contains("builder")) {
                        updateSnapshot(false);
                        Optional.ofNullable(getBuilder())
                                .ifPresent(builder -> builder.deserializeNBT(nbt.getCompound("builder")));
                        // Calen: make the required items able to be seen, this should after builder#deserializeNBT
                        itemManager.getCapability(CapUtil.CAP_ITEMS).ifPresent(h ->
                        {
                            ItemStack stack = h.getStackInSlot(0);
                            if (stack.getItem() instanceof ItemSnapshot) {
                                onSlotChange(invSnapshot, 0, StackUtil.EMPTY, stack);
                            }
                        });
                    }
                    // Calen: check snapshot type and update blockstate
                    BlockState currentState = level.getBlockState(worldPosition);
                    Block b = currentState.getBlock();
                    if (b instanceof BlockBuilder) {
                        BlockBuilder builder = (BlockBuilder) b;
                        builder.checkActualStateAndUpdate(currentState, level, worldPosition, this);
                    }
                },
                false
        );
    }

    // Rendering

    @OnlyIn(Dist.CLIENT)
    public Box getBox() {
        return currentBox;
    }

//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public boolean hasFastRenderer() {
//        return true;
//    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(worldPosition, getBox(), path);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
//    public double getMaxRenderDistanceSquared()
    public double getViewDistance() {
//        return Double.MAX_VALUE;
        return 512;
    }

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<ITextComponent> left, List<ITextComponent> right, Direction side) {
//        left.add("battery = " + battery.getDebugString());
//        left.add("basePoses = " + (basePoses == null ? "null" : basePoses.size()));
//        left.add("currentBasePosIndex = " + currentBasePosIndex);
//        left.add("isDone = " + isDone);
        left.add(new StringTextComponent("battery = " + battery.getDebugString()));
        left.add(new StringTextComponent("basePoses = " + (basePoses == null ? "null" : basePoses.size())));
        left.add(new StringTextComponent("currentBasePosIndex = " + currentBasePosIndex));
        left.add(new StringTextComponent("isDone = " + isDone));
    }

    @Override
    public World getWorldBC() {
        return level;
    }

    @Override
    public MjBattery getBattery() {
        return battery;
    }

    @Override
    public BlockPos getBuilderPos() {
        return worldPosition;
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

    private Snapshot.BuildingInfo getBuildingInfo() {
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            return templateBuildingInfo;
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            return blueprintBuildingInfo;
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
    public TankManager getTankManager() {
        return tankManager;
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new ContainerBuilder(BCBuildersMenuTypes.BUILDER, id, player, this);
    }

    // IRequestProvider

    @Override
    public int getRequestsCount() {
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            return 0;
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            return this.blueprintBuilder.remainingDisplayRequired.size();
        }
        return 0;
    }

    @Nonnull
    @Override
    public ItemStack getRequest(int slot) {
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            return StackUtil.EMPTY;
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            if (slot >= this.blueprintBuilder.remainingDisplayRequired.size()) {
                return StackUtil.EMPTY;
            } else {
                return this.blueprintBuilder.remainingDisplayRequired.get(slot).copy();
            }
        }
        return StackUtil.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack offerItem(int slot, ItemStack stack) {
        return this.invResources.insert(stack, true, false);
    }
}

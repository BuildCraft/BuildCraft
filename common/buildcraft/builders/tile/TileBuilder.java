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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TileBuilder extends TileBC_Neptune
        implements ITickable, IDebuggable, ITileForTemplateBuilder, ITileForBlueprintBuilder, IBCTileMenuProvider {
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

    public TileBuilder(BlockPos pos, BlockState blockState) {
        super(BCBuildersBlocks.builderTile.get(), pos, blockState);
        for (int i = 1; i <= 4; i++) {
            tankManager.add(new Tank("tank" + i, FluidType.BUCKET_VOLUME * 8, this) {
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
        if (!level.isClientSide) {
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
        BlockEntity inFront = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
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
        ProfilerFiller profiler = level.getProfiler();
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
                if (state.getBlock() instanceof BlockBCBase_Neptune blockBC) {
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
//    public CompoundTag writeToNBT(CompoundTag nbt)
    public void saveAdditional(CompoundTag nbt) {
//        super.writeToNBT(nbt);
        super.saveAdditional(nbt);
        if (path != null) {
            nbt.put("path", NBTUtilBC.writeCompoundList(path.stream().map(NbtUtils::writeBlockPos)));
        }
        nbt.put("basePoses", NBTUtilBC.writeCompoundList(basePoses.stream().map(NbtUtils::writeBlockPos)));
        nbt.putBoolean("canExcavate", canExcavate);
        nbt.put("rotation", NBTUtilBC.writeEnum(rotation));
        Optional.ofNullable(getBuilder()).ifPresent(builder -> nbt.put("builder", builder.serializeNBT()));
//        return nbt;
    }

    @Override
//    public void readFromNBT(CompoundTag nbt)
    public void load(CompoundTag nbt) {
//        super.readFromNBT(nbt);
        super.load(nbt);
        if (nbt.contains("path")) {
            path =
                    NBTUtilBC.readCompoundList(nbt.get("path")).map(NbtUtils::readBlockPos).collect(Collectors.toList());
        }
        basePoses = NBTUtilBC.readCompoundList(nbt.get("basePoses")).map(NbtUtils::readBlockPos)
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
                        itemManager.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h ->
                        {
                            ItemStack stack = h.getStackInSlot(0);
                            if (stack.getItem() instanceof ItemSnapshot itemSnapshot) {
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
                        itemManager.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h ->
                        {
                            ItemStack stack = h.getStackInSlot(0);
                            if (stack.getItem() instanceof ItemSnapshot itemSnapshot) {
                                onSlotChange(invSnapshot, 0, StackUtil.EMPTY, stack);
                            }
                        });
                    }
                    // Calen: check snapshot type and update blockstate
                    BlockState state = level.getBlockState(worldPosition);
                    if (state.getBlock() instanceof BlockBuilder builder) {
                        builder.checkActualStateAndUpdate(state, level, worldPosition, this);
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
    public AABB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(worldPosition, getBox(), path);
    }

    // Calen: moved to RenderBuilder#getViewDistance
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public double getMaxRenderDistanceSquared() {
//        return Double.MAX_VALUE;
//    }

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
//        left.add("battery = " + battery.getDebugString());
//        left.add("basePoses = " + (basePoses == null ? "null" : basePoses.size()));
//        left.add("currentBasePosIndex = " + currentBasePosIndex);
//        left.add("isDone = " + isDone);
        left.add(Component.literal("battery = " + battery.getDebugString()));
        left.add(Component.literal("basePoses = " + (basePoses == null ? "null" : basePoses.size())));
        left.add(Component.literal("currentBasePosIndex = " + currentBasePosIndex));
        left.add(Component.literal("isDone = " + isDone));
    }

    @Override
    public Level getWorldBC() {
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
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerBuilder(BCBuildersMenuTypes.BUILDER, id, player, this);
    }
}

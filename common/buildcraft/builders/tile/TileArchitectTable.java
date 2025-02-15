/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.builders.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.schematics.SchematicEntityContext;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITickable;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.block.BlockArchitectTable;
import buildcraft.builders.client.ClientArchitectTables;
import buildcraft.builders.container.ContainerArchitectTable;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.*;
import buildcraft.builders.snapshot.Snapshot.Header;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.BoxIterator;
import buildcraft.lib.misc.data.EnumAxisOrder;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

public class TileArchitectTable extends TileBC_Neptune implements ITickable, IDebuggable, IBCTileMenuProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("architect");
    @SuppressWarnings("WeakerAccess")
    public static final int NET_BOX = IDS.allocId("BOX");
    @SuppressWarnings("WeakerAccess")
    public static final int NET_SCAN = IDS.allocId("SCAN");
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftbuilders:architect");

    public final ItemHandlerSimple invSnapshotIn = itemManager.addInvHandler(
            "in",
            1,
            (slot, stack) -> stack.getItem() instanceof ItemSnapshot,
            EnumAccess.INSERT,
            EnumPipePart.VALUES
    );
    public final ItemHandlerSimple invSnapshotOut = itemManager.addInvHandler(
            "out",
            1,
            EnumAccess.EXTRACT,
            EnumPipePart.VALUES
    );

    private EnumSnapshotType snapshotType = EnumSnapshotType.BLUEPRINT;
    public final Box box = new Box();
    public boolean markerBox = false;
    private BitSet templateScannedBlocks;
    private final List<ISchematicBlock> blueprintScannedPalette = new ArrayList<>();
    private int[] blueprintScannedData;
    private final List<ISchematicEntity> blueprintScannedEntities = new ArrayList<>();
    private BoxIterator boxIterator;
    private boolean isValid = false;
    private boolean scanning = false;
    public String name = "<unnamed>";
    public final DeltaInt deltaProgress = deltaManager.addDelta(
            "progress",
            DeltaManager.EnumNetworkVisibility.GUI_ONLY
    );

    public TileArchitectTable(BlockPos pos, BlockState state) {
        super(BCBuildersBlocks.architectTile.get(), pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
//    public void onPlacedBy(EntityLivingBase placer, ItemStack stack)
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (placer.level().isClientSide) {
            return;
        }
        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(level);
        BlockState blockState = level.getBlockState(worldPosition);
        BlockPos offsetPos = worldPosition.relative(blockState.getValue(BlockArchitectTable.PROP_FACING).getOpposite());
        VolumeBox volumeBox = volumeBoxes.getVolumeBoxAt(offsetPos);
        BlockEntity tile = level.getBlockEntity(offsetPos);
        if (volumeBox != null) {
            box.reset();
            box.setMin(volumeBox.box.min());
            box.setMax(volumeBox.box.max());
            isValid = true;
            volumeBox.locks.add(
                    new Lock(
                            new Lock.Cause.CauseBlock(worldPosition, blockState.getBlock()),
                            new Lock.Target.TargetRemove(),
                            new Lock.Target.TargetResize(),
                            new Lock.Target.TargetUsedByMachine(
                                    Lock.Target.TargetUsedByMachine.EnumType.STRIPES_READ
                            )
                    )
            );
            volumeBoxes.setDirty();
            sendNetworkUpdate(NET_BOX);
        } else if (tile instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) tile;
            box.reset();
            box.setMin(provider.min());
            box.setMax(provider.max());
            markerBox = true;
            isValid = true;
            provider.removeFromWorld();
        } else {
            isValid = false;
            BlockState state = level.getBlockState(worldPosition);
            state = state.setValue(BlockArchitectTable.PROP_VALID, Boolean.FALSE);
            level.setBlock(worldPosition, state, Block.UPDATE_ALL);
        }
    }

    @Override
    public void update() {
        ITickable.super.update();
        deltaManager.tick();

        if (level.isClientSide) {
            if (box.isInitialized()) {
                ClientArchitectTables.BOXES.put(box.getBoundingBox(), ClientArchitectTables.START_BOX_VALUE);
            }
            return;
        }

        if (!invSnapshotIn.getStackInSlot(0).isEmpty() && invSnapshotOut.getStackInSlot(0).isEmpty() && isValid) {
            if (!scanning) {
                snapshotType = ItemSnapshot.EnumItemSnapshotType.getFromStack(
                        invSnapshotIn.getStackInSlot(0)
                ).snapshotType;
                int size = box.size().getX() * box.size().getY() * box.size().getZ();
                size /= snapshotType.maxPerTick;
                deltaProgress.addDelta(0, size, 1);
                deltaProgress.addDelta(size, size + 10, -1);
                scanning = true;
            }
        } else {
            scanning = false;
        }

        if (scanning) {
            scanMultipleBlocks();
            if (!scanning) {
                if (snapshotType == EnumSnapshotType.BLUEPRINT) {
                    scanEntities();
                }
                finishScanning();
            }
        }
    }

    private void scanMultipleBlocks() {
        for (int i = snapshotType.maxPerTick; i > 0; i--) {
            scanSingleBlock();
            if (!scanning) {
                break;
            }
        }
    }

    private void scanSingleBlock() {
        BlockPos size = box.size();
        if (templateScannedBlocks == null || blueprintScannedData == null) {
            boxIterator = new BoxIterator(box, EnumAxisOrder.XZY.getMinToMaxOrder(), true);
            templateScannedBlocks = new BitSet(Snapshot.getDataSize(size));
            blueprintScannedData = new int[Snapshot.getDataSize(size)];
        }

        // Read from world
        BlockPos worldScanPos = boxIterator.getCurrent();
        BlockPos schematicPos = worldScanPos.subtract(box.min());
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            templateScannedBlocks.set(Snapshot.posToIndex(box.size(), schematicPos), !level.isEmptyBlock(worldScanPos));
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            ISchematicBlock schematicBlock = readSchematicBlock(worldScanPos);
            int index = blueprintScannedPalette.indexOf(schematicBlock);
            if (index == -1) {
                index = blueprintScannedPalette.size();
                blueprintScannedPalette.add(schematicBlock);
            }
            blueprintScannedData[Snapshot.posToIndex(box.size(), schematicPos)] = index;
        }

        createAndSendMessage(NET_SCAN, buffer -> MessageUtil.writeBlockPos(buffer, worldScanPos));

        sendNetworkUpdate(NET_RENDER_DATA);

        // Move scanPos along
        boxIterator.advance();

        if (boxIterator.hasFinished()) {
            scanning = false;
            boxIterator = null;
        }
    }

    private ISchematicBlock readSchematicBlock(BlockPos worldScanPos) {
//        return SchematicBlockManager.getSchematicBlock(new SchematicBlockContext(
//                level,
//                box.min(),
//                worldScanPos,
//                level.getBlockState(worldScanPos),
//                level.getBlockState(worldScanPos).getBlock()
//        ));
        // Calen: for bed
        // if the other half is out of the box, ignore this bed
        BlockState state = level.getBlockState(worldScanPos);
        Block block = state.getBlock();
        if (block instanceof BedBlock bed) {
            if (!box.contains(worldScanPos.relative(bed.getConnectedDirection(state)))) {
                state = Blocks.AIR.defaultBlockState();
                block = Blocks.AIR;
            }
        }
        return SchematicBlockManager.getSchematicBlock(new SchematicBlockContext(
                level,
                box.min(),
                worldScanPos,
                state,
                block
        ));

    }

    private void scanEntities() {
//        level.getEntitiesWithinAABB(Entity.class, box.getBoundingBox()).stream()
        level.getEntitiesOfClass(Entity.class, box.getBoundingBox()).stream()
                .map(entity ->
                        SchematicEntityManager.getSchematicEntity(new SchematicEntityContext(
                                level,
                                box.min(),
                                entity
                        ))
                )
                .filter(Objects::nonNull)
                .forEach(blueprintScannedEntities::add);
    }

    private void finishScanning() {
        BlockState thisState = getCurrentStateForBlock(BCBuildersBlocks.architect.get());
        if (thisState == null) {
            return;
        }

        Direction facing = thisState.getValue(BlockArchitectTable.PROP_FACING);
        Snapshot snapshot = Snapshot.create(snapshotType);
        snapshot.size = box.size();
        snapshot.facing = facing;
        snapshot.offset = box.min().subtract(worldPosition.relative(facing.getOpposite()));
        if (snapshot instanceof Template) {
            ((Template) snapshot).data = templateScannedBlocks;
        }
        if (snapshot instanceof Blueprint) {
            ((Blueprint) snapshot).palette.addAll(blueprintScannedPalette);
            ((Blueprint) snapshot).data = blueprintScannedData;
            ((Blueprint) snapshot).entities.addAll(blueprintScannedEntities);
        }
        snapshot.computeKey();
        GlobalSavedDataSnapshots.get(level).addSnapshot(snapshot);
        ItemStack stackIn = invSnapshotIn.getStackInSlot(0);
        stackIn.setCount(stackIn.getCount() - 1);
        if (stackIn.getCount() == 0) {
            stackIn = ItemStack.EMPTY;
        }
        invSnapshotIn.setStackInSlot(0, stackIn);
        invSnapshotOut.setStackInSlot(
                0,
//                BCBuildersItems.snapshot.getUsed(
                BCBuildersItems.snapshotBLUEPRINT.get().getUsed(
                        snapshotType,
                        new Header(
                                snapshot.key,
                                getOwner().getId(),
                                new Date(),
                                name
                        )
                )
        );
        templateScannedBlocks = null;
        blueprintScannedData = null;
        blueprintScannedEntities.clear();
        boxIterator = null;
        sendNetworkUpdate(NET_RENDER_DATA);
        AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT);
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_BOX, buffer, side);
                buffer.writeUtf(name);
            } else if (id == NET_BOX) {
                box.writeData(buffer);
                buffer.writeBoolean(markerBox);
            }
        }
    }

    @Override
//    public void readPayload(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_BOX, buffer, side, ctx);
                name = buffer.readString();
            } else if (id == NET_BOX) {
                box.readData(buffer);
                markerBox = buffer.readBoolean();
            } else if (id == NET_SCAN) {
                ClientArchitectTables.SCANNED_BLOCKS.put(
                        MessageUtil.readBlockPos(buffer),
                        ClientArchitectTables.START_SCANNED_BLOCK_VALUE
                );
            }
        }
    }

    @Override
//    public CompoundTag writeToNBT(CompoundTag nbt)
    public void saveAdditional(CompoundTag nbt) {
//        super.writeToNBT(nbt);
        super.saveAdditional(nbt);
        nbt.put("box", box.writeToNBT());
        nbt.putBoolean("markerBox", markerBox);
        if (boxIterator != null) {
            nbt.put("iter", boxIterator.writeToNbt());
        }
        nbt.putBoolean("scanning", scanning);
        nbt.put("snapshotType", NBTUtilBC.writeEnum(snapshotType));
        nbt.putBoolean("isValid", isValid);
        nbt.putString("name", name);
//        return nbt;
    }

    @Override
//    public void readFromNBT(CompoundTag nbt)
    public void load(CompoundTag nbt) {
//        super.readFromNBT(nbt);
        super.load(nbt);
        box.initialize(nbt.getCompound("box"));
        markerBox = nbt.getBoolean("markerBox");
        if (nbt.contains("iter")) {
            boxIterator = BoxIterator.readFromNbt(nbt.getCompound("iter"));
        }
        scanning = nbt.getBoolean("scanning");
        snapshotType = NBTUtilBC.readEnum(nbt.get("snapshotType"), EnumSnapshotType.class);
        isValid = nbt.getBoolean("isValid");
        name = nbt.getString("name");
    }

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
//        left.add("box:");
//        left.add(" - min = " + box.min());
//        left.add(" - max = " + box.max());
//        left.add("scanning = " + scanning);
//        left.add("current = " + (boxIterator == null ? null : boxIterator.getCurrent()));
        left.add(Component.literal("box:"));
        left.add(Component.literal(" - min = " + box.min()));
        left.add(Component.literal(" - max = " + box.max()));
        left.add(Component.literal("scanning = " + scanning));
        left.add(Component.literal("current = " + (boxIterator == null ? null : boxIterator.getCurrent())));
    }

    // Rendering

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(getBlockPos(), box);
    }

    // Calen added from MenuProvider
    @Override
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    // Calen added from MenuProvider
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerArchitectTable(BCBuildersMenuTypes.ARCHITECT_TABLE, id, player, this);
    }

    // Calen: moved to RenderArchitectTable#shouldRenderOffScreen+getViewDistance
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public double getMaxRenderDistanceSquared() {
//        return Double.MAX_VALUE;
//    }
}

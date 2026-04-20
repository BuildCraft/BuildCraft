/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package ct.buildcraft.builders.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.core.IAreaProvider;
import ct.buildcraft.api.enums.EnumSnapshotType;
import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.api.schematics.ISchematicEntity;
import ct.buildcraft.api.schematics.SchematicBlockContext;
import ct.buildcraft.api.schematics.SchematicEntityContext;
import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.builders.block.BlockArchitectTable;
import ct.buildcraft.builders.client.ClientArchitectTables;
import ct.buildcraft.builders.gui.MenuArchitectTable;
import ct.buildcraft.builders.item.ItemSnapshot;
import ct.buildcraft.builders.menu.ContainerArchitectTable;
import ct.buildcraft.builders.snapshot.Blueprint;
import ct.buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import ct.buildcraft.builders.snapshot.SchematicBlockManager;
import ct.buildcraft.builders.snapshot.SchematicEntityManager;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.builders.snapshot.Snapshot.Header;
import ct.buildcraft.builders.snapshot.Template;
import ct.buildcraft.core.marker.volume.Lock;
import ct.buildcraft.core.marker.volume.VolumeBox;
import ct.buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import ct.buildcraft.lib.delta.DeltaInt;
import ct.buildcraft.lib.delta.DeltaManager;
import ct.buildcraft.lib.misc.AdvancementUtil;
import ct.buildcraft.lib.misc.BoundingBoxUtil;
import ct.buildcraft.lib.misc.MessageUtil;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.data.Box;
import ct.buildcraft.lib.misc.data.BoxIterator;
import ct.buildcraft.lib.misc.data.EnumAxisOrder;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class TileArchitectTable extends TileBC_Neptune implements IDebuggable, MenuProvider{

	public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("architect");
    public static final int NET_BOX = IDS.allocId("BOX");
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
    
    private boolean isCreative = true;
    private boolean canRotate = true;
    private boolean canExcavate = true;
    
    private DataSlot menuSetting = new DataSlot() {
		
		@Override
		public void set(int p) {
			isCreative = (p&0b1) == 1;
			canRotate = (p&0b10) == 0b10;
			canExcavate = (p&0b100) == 0b100;
		}
		
		@Override
		public int get() {
			return (isCreative ? 1 : 0) | (canRotate ? 0b10 : 0) | (canExcavate ? 0b100 : 0);
		}
	};
    
    public TileArchitectTable(BlockPos pos, BlockState state) {
		super(BCBuildersBlocks.ARCHITECT_TILE_BC8.get(), pos, state);
	}

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (placer.level.isClientSide) {
            return;
        }
        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(level);
        BlockState blockState = level.getBlockState(worldPosition);
        BlockPos offsetPos = worldPosition.offset(blockState.getValue(BlockArchitectTable.PROP_FACING).getOpposite().getNormal());
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
            provider.removeFromWorld(placer instanceof Player player? player : null);
        } else {
            isValid = false;
            BlockState state = level.getBlockState(worldPosition);
            state = state.setValue(BlockArchitectTable.PROP_VALID, Boolean.FALSE);
            level.setBlockAndUpdate(worldPosition, state);
        }
    }

    @Override
    public void update() {
        deltaManager.tick();

        if (level.isClientSide) {
            if (box.isInitialized()) {
                ClientArchitectTables.BOXES.put(box.getBoundingBox(), ClientArchitectTables.START_BOX_VALUE);
            }
            return;
        }

        if (!invSnapshotIn.getStackInSlot(0).isEmpty() && invSnapshotOut.getStackInSlot(0).isEmpty() && isValid) {
            if (!scanning) {
            	//TODO add blueprint info
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

        // Read from level
        BlockPos levelScanPos = boxIterator.getCurrent();
        BlockPos schematicPos = levelScanPos.subtract(box.min());
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            templateScannedBlocks.set(Snapshot.posToIndex(box.size(), schematicPos), !level.isEmptyBlock(levelScanPos));
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            ISchematicBlock schematicBlock = readSchematicBlock(levelScanPos);
            int index = blueprintScannedPalette.indexOf(schematicBlock);
            if (index == -1) {
                index = blueprintScannedPalette.size();
                blueprintScannedPalette.add(schematicBlock);
            }
            blueprintScannedData[Snapshot.posToIndex(box.size(), schematicPos)] = index;
        }

        createAndSendMessage(NET_SCAN, buffer -> MessageUtil.writeBlockPos(buffer, levelScanPos));

        sendNetworkUpdate(NET_RENDER_DATA);

        // Move scanPos along
        boxIterator.advance();

        if (boxIterator.hasFinished()) {
            scanning = false;
            boxIterator = null;
        }
    }

    private ISchematicBlock readSchematicBlock(BlockPos levelScanPos) {
        return SchematicBlockManager.getSchematicBlock(new SchematicBlockContext(
            level,
            box.min(),
            levelScanPos,
            level.getBlockState(levelScanPos),
            level.getBlockState(levelScanPos).getBlock()
        ));
    }

    private void scanEntities() {
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
        BlockState thisState = getCurrentStateForBlock(BCBuildersBlocks.ARCHITECT.get());
        if (thisState == null) {
            return;
        }

        Direction facing = thisState.getValue(BlockArchitectTable.PROP_FACING);
        Snapshot snapshot = Snapshot.create(snapshotType);
        snapshot.size = box.size();
        snapshot.facing = facing;
        snapshot.offset = box.min().subtract(worldPosition.offset(facing.getOpposite().getNormal()));
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
            ItemSnapshot.getUsed(
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
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
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
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
    	super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_BOX, buffer, side, ctx);
                name = buffer.readUtf();
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
	public void saveAdditional(CompoundTag nbt) {
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
	}

    @Override
	public void load(CompoundTag nbt) {
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
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("box:");
        left.add(" - min = " + box.min());
        left.add(" - max = " + box.max());
        left.add("scanning = " + scanning);
        left.add("current = " + (boxIterator == null ? null : boxIterator.getCurrent()));
    }

    // Rendering

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
	public AABB getRenderBoundingBox() {
    	return BoundingBoxUtil.makeFrom(worldPosition, box);
	}
/*
	@Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }*/

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new ContainerArchitectTable(id, inventory, invSnapshotIn, invSnapshotOut, menuSetting, /* deltaProgress.getContainerData(), */ContainerLevelAccess.create(getLevel(), worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return getBlockState().getBlock().getName();
	}
}

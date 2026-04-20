/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.tile;

import java.util.Date;

import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.enums.EnumSnapshotType;
import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.builders.gui.MenuReplacer;
import ct.buildcraft.builders.item.ItemSchematicSingle;
import ct.buildcraft.builders.item.ItemSnapshot;
import ct.buildcraft.builders.snapshot.Blueprint;
import ct.buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import ct.buildcraft.builders.snapshot.SchematicBlockManager;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.builders.snapshot.Snapshot.Header;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.ItemHandlerManager;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileReplacer extends TileBC_Neptune implements MenuProvider{

	public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("replacer");

    public final ItemHandlerSimple invSnapshot = itemManager.addInvHandler(
        "snapshot",
        1,
        (slot, stack) -> stack.getItem() instanceof ItemSnapshot &&
            ItemSnapshot.EnumItemSnapshotType.getFromStack(stack) == ItemSnapshot.EnumItemSnapshotType.BLUEPRINT_USED,
        ItemHandlerManager.EnumAccess.NONE
    );
    public final ItemHandlerSimple invSchematicFrom = itemManager.addInvHandler(
        "schematicFrom",
        1,
        (slot, stack) -> stack.getItem() instanceof ItemSchematicSingle &&
            stack.getDamageValue() == ItemSchematicSingle.DAMAGE_USED,
        ItemHandlerManager.EnumAccess.NONE
    );
    public final ItemHandlerSimple invSchematicTo = itemManager.addInvHandler(
        "schematicTo",
        1,
        (slot, stack) -> stack.getItem() instanceof ItemSchematicSingle &&
            stack.getDamageValue() == ItemSchematicSingle.DAMAGE_USED,
        ItemHandlerManager.EnumAccess.NONE
    );
    
    public TileReplacer(BlockPos pos, BlockState state) {
		super(BCBuildersBlocks.REPLACER_TILE_BC8.get(), pos, state);
	}

    @Override
    public void update() {
        if (level.isClientSide) {
            return;
        }
        if (!invSnapshot.getStackInSlot(0).isEmpty() &&
            !invSchematicFrom.getStackInSlot(0).isEmpty() &&
            !invSchematicTo.getStackInSlot(0).isEmpty()) {
            Header header = ItemSnapshot.getHeader(invSnapshot.getStackInSlot(0));
            if (header != null) {
                Snapshot snapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
                if (snapshot instanceof Blueprint) {
                    Blueprint blueprint = (Blueprint) snapshot;
                    try {
                        ISchematicBlock from = SchematicBlockManager.readFromNBT(
                            NBTUtilBC.getItemData(invSchematicFrom.getStackInSlot(0))
                                .getCompound(ItemSchematicSingle.NBT_KEY)
                        );
                        ISchematicBlock to = SchematicBlockManager.readFromNBT(
                            NBTUtilBC.getItemData(invSchematicTo.getStackInSlot(0))
                                .getCompound(ItemSchematicSingle.NBT_KEY)
                        );
                        Blueprint newBlueprint = blueprint.copy();
                        newBlueprint.replace(from, to);
                        newBlueprint.computeKey();
                        GlobalSavedDataSnapshots.get(level).addSnapshot(newBlueprint);
                        invSnapshot.setStackInSlot(
                            0,
                            ItemSnapshot.getUsed(
                                EnumSnapshotType.BLUEPRINT,
                                new Header(
                                    blueprint.key,
                                    getOwner().getId(),
                                    new Date(),
                                    header.name
                                )
                            )
                        );
                        invSchematicFrom.setStackInSlot(0, ItemStack.EMPTY);
                        invSchematicTo.setStackInSlot(0, ItemStack.EMPTY);
                    } catch (InvalidInputDataException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new MenuReplacer(id, inv, invSnapshot, invSchematicFrom, invSchematicTo, ContainerLevelAccess.create(level, worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return getBlockState().getBlock().getName();
	}
}

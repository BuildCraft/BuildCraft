/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.api.tiles.ITickable;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.container.ContainerReplacer;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.SchematicBlockManager;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Snapshot.Header;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class TileReplacer extends TileBC_Neptune implements ITickable, IBCTileMenuProvider {
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

    public TileReplacer(BlockPos pos, BlockState blockState) {
        super(BCBuildersBlocks.replacerTile.get(), pos, blockState);
    }

    @Override
    public void update() {
        ITickable.super.update();
        if (level.isClientSide) {
            return;
        }
        if (!invSnapshot.getStackInSlot(0).isEmpty() &&
                !invSchematicFrom.getStackInSlot(0).isEmpty() &&
                !invSchematicTo.getStackInSlot(0).isEmpty())
        {
//            Header header = BCBuildersItems.snapshot.getHeader(invSnapshot.getStackInSlot(0));
            Header header = BCBuildersItems.snapshotBLUEPRINT.get().getHeader(invSnapshot.getStackInSlot(0));
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
//                                BCBuildersItems.snapshot.getUsed(
                                BCBuildersItems.snapshotBLUEPRINT.get().getUsed(
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
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerReplacer(BCBuildersMenuTypes.REPLACER, id, player, this);
    }
}

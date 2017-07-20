/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.tile.TileReplacer;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerReplacer extends ContainerBCTile<TileReplacer> {
    public ContainerReplacer(EntityPlayer player, TileReplacer tile) {
        super(player, tile);

        addSlotToContainer(new SlotBase(tile.invSnapshot, 0, 8, 115) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return stack.getItem() instanceof ItemSnapshot &&
                    ItemSnapshot.EnumItemSnapshotType.getFromStack(stack) ==
                        ItemSnapshot.EnumItemSnapshotType.BLUEPRINT_USED;
            }
        });
        addSlotToContainer(new SlotBase(tile.invSchematicFrom, 0, 8, 137) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return stack.getItem() instanceof ItemSchematicSingle &&
                    stack.getItemDamage() == ItemSchematicSingle.DAMAGE_USED;
            }
        });
        addSlotToContainer(new SlotBase(tile.invSchematicTo, 0, 56, 137) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return stack.getItem() instanceof ItemSchematicSingle &&
                    stack.getItemDamage() == ItemSchematicSingle.DAMAGE_USED;
            }
        });

        addFullPlayerInventory(159);
    }
}

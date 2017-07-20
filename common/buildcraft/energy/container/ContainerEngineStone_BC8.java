/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.container;

import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import javax.annotation.Nonnull;

public class ContainerEngineStone_BC8 extends ContainerBCTile<TileEngineStone_BC8> {
    public ContainerEngineStone_BC8(EntityPlayer player, TileEngineStone_BC8 engine) {
        super(player, engine);

        addFullPlayerInventory(84);
        addSlotToContainer(new SlotBase(engine.invFuel, 0, 80, 41) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return TileEntityFurnace.getItemBurnTime(stack) > 0;
            }
        });
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.canInteractWith(player);
    }
}

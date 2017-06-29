/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;

import buildcraft.builders.filling.Filling;
import buildcraft.builders.filling.IParameter;
import buildcraft.builders.tile.TileFiller;

public class ContainerFiller extends ContainerBCTile<TileFiller> implements IContainerFilling {
    public ContainerFiller(EntityPlayer player, TileFiller tile) {
        super(player, tile);

        addFullPlayerInventory(108);

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlotToContainer(new SlotBase(tile.invResources, sx + sy * 9, sx * 18 + 8, sy * 18 + 40) {
                    @Override
                    public boolean isItemValid(@Nonnull ItemStack stack) {
                        return Filling.getItemBlocks().contains(stack.getItem());
                    }
                });
            }
        }
    }

    @Override
    public boolean isInverted() {
        return tile.isInverted();
    }

    @Override
    public void setInverted(boolean value) {
        tile.sendInverted(value);
    }

    @Override
    public List<IParameter> getParameters() {
        return tile.getParameters();
    }

    @Override
    public void setParameters(List<IParameter> value) {
        tile.sendParameters(value);
    }

    @Override
    public boolean isCanExcavate() {
        return tile.isCanExcavate();
    }

    @Override
    public void setCanExcavate(boolean value) {
        tile.sendCanExcavate(value);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}

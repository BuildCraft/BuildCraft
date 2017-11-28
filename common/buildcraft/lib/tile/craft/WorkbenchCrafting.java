/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile.craft;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import buildcraft.lib.tile.item.ItemHandlerSimple;

public class WorkbenchCrafting extends InventoryCrafting {
    private final ItemHandlerSimple invBlueprint;

    public WorkbenchCrafting(int width, int height, ItemHandlerSimple invBlueprint) {
        super(null, width, height);
        this.invBlueprint = invBlueprint;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return invBlueprint.getStackInSlot(index);
    }
}

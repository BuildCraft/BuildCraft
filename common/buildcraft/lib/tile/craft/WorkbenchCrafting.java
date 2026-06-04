/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): InventoryCrafting removed in 1.20.1 — crafting logic deferred
package buildcraft.lib.tile.craft;

import net.minecraft.item.ItemStack;

import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class WorkbenchCrafting {

    private final TileBC_Neptune tile;
    private final ItemHandlerSimple invBlueprint;
    private final ItemHandlerSimple invMaterials;
    private final ItemHandlerSimple invResult;

    public WorkbenchCrafting(int width, int height, TileBC_Neptune tile, ItemHandlerSimple invBlueprint,
            ItemHandlerSimple invMaterials, ItemHandlerSimple invResult) {
        this.tile = tile;
        this.invBlueprint = invBlueprint;
        this.invMaterials = invMaterials;
        this.invResult = invResult;
    }

    public boolean hasRequirements() {
        return false;
    }

    public boolean canCraft() {
        return false;
    }

    public boolean craft() {
        return false;
    }

    public ItemStack getResult() {
        return ItemStack.EMPTY;
    }

    public void markBlueprintDirty() {
        // STUB
    }

    public void markMaterialsDirty() {
        // STUB
    }
}

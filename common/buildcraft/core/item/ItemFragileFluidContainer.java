/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): blocked by Forge fluid capability migration — Phase 3 fluid migration
package buildcraft.core.item;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import buildcraft.lib.compat.FluidStackBC;
import buildcraft.api.items.IItemFluidShard;
import buildcraft.lib.item.ItemBC_Neptune;

public class ItemFragileFluidContainer extends ItemBC_Neptune implements IItemFluidShard {

    public ItemFragileFluidContainer(Settings settings, String id) {
        super(settings, id);
    }

    @Override
    public void addFluidDrops(DefaultedList<ItemStack> toDrop, @Nullable FluidStackBC fluid) {
        // STUB
    }
}

/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// STUB(R.Chen): ItemSchematicSingle logic deferred
package buildcraft.builders.item;

import net.minecraft.item.ItemStack;

import buildcraft.lib.item.ItemBC_Neptune;

public class ItemSchematicSingle extends ItemBC_Neptune {

    // STUB(R.Chen): legacy meta/NBT constants used by TileReplacer — values match BuildCraft 1.12.2
    public static final int DAMAGE_CLEAN = 0;
    public static final int DAMAGE_USED = 1;
    public static final String NBT_KEY = "schematic";

    public ItemSchematicSingle(Settings settings, String id) {
        super(settings, id);
    }

    public static ItemStack getSchematic(ItemStack stack) {
        return ItemStack.EMPTY;
    }
}

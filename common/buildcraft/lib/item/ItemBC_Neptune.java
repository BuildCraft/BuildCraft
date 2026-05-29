/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.item;

// Yarn 1.20.1: Item constructor now takes Item.Settings instead of no-arg.
// CreativeTabs / NonNullList dropped — Fabric creative tabs use ItemGroupEvents in ModInitializer.
import net.minecraft.item.Item;

public class ItemBC_Neptune extends Item implements IItemBuildCraft {
    /** The tag used to identify this item in the registry and TagManager. */
    public final String id;

    public ItemBC_Neptune(String id) {
        super(new Item.Settings());
        this.id = id;
        init();
    }

    @Override
    public String id() {
        return id;
    }

    // STUB(R.Chen): getSubItems / addSubItems dropped — Fabric creative tab population
    // is handled via ItemGroupEvents.modifyEntriesEvent in the module's ModInitializer (Phase 5).
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.item;

// Yarn 1.20.1: Item constructor now takes Item.Settings instead of no-arg.
// ItemGroup / DefaultedList dropped — Fabric creative tabs use ItemGroupEvents in ModInitializer.
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

    // ---- 1.12.2 compat no-ops ----
    /** 1.12.2 compat: max stack size is set via Item.Settings in 1.20.1. */
    protected void setMaxStackSize(int maxStackSize) { /* no-op: use Item.Settings.maxCount() */ }
    /** 1.12.2 compat: sub-items replaced by ItemGroupEvents. */
    protected void setHasSubtypes(boolean has) { /* no-op */ }
    /** 1.12.2 compat: max damage set via Item.Settings in 1.20.1. */
    protected void setMaxDamage(int maxDamage) { /* no-op: use Item.Settings.maxDamage() */ }
    /** 1.12.2 compat: alias for getRegistryName(). Returns registry ID. */
    public net.minecraft.util.Identifier getRegistryName() {
        return net.minecraft.registry.Registries.ITEM.getId(this);
    }
}

/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.lib.item;

import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemBC_Neptune extends Item {
    /** The tag used to identify this in the {@link TagManager} */
    public final String id;

    public ItemBC_Neptune(String id, Item.Properties p) {
    	super(p);
        this.id = id;
    }



    /** Identical to {@link #getSubItems(CreativeTabs, NonNullList)} in every way, EXCEPT that this is only called if
     * this is actually in the given creative tab.
     * 
     * @param tab The {@link CreativeTabs} to display the items in. This is provided just in case an item has multiple
     *            subtypes, split across different tabs */
    protected void addSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }
}

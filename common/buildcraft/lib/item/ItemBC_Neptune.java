/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import buildcraft.lib.registry.TagManager;

import java.util.List;

public class ItemBC_Neptune extends Item implements IItemBuildCraft {
    /** The tag used to identify this in the {@link TagManager} */
    public final String id;

    public ItemBC_Neptune(String id) {
        this.id = id;
        init();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        if (getCreativeTab() == tab) {
            addSubItems(tab, subItems);
        }
    }

    /** Identical to {@link #getSubItems(Item, CreativeTabs, List)} in every way, EXCEPT that this is only called if
     * this is actually in the given creative tab.
     * 
     * @param tab The {@link CreativeTabs} to display the items in. This is provided just in case an item has multiple
     *            subtypes, split across different tabs */
    protected void addSubItems(CreativeTabs tab, List<ItemStack> items) {
        items.add(new ItemStack(this));
    }
}

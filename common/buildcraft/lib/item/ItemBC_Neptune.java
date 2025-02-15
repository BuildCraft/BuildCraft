/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.item;

import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.TagManager;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;

public class ItemBC_Neptune extends Item implements IItemBuildCraft, IForgeItem {
    /** The tag used to identify this in the {@link TagManager} */
    public final String idBC;

    public ItemBC_Neptune(String idBC, Item.Properties properties) {
        super(properties);
        tab(CreativeTabManager.getTab(TagManager.getTag(idBC, TagManager.EnumTagType.CREATIVE_TAB)));
        this.idBC = idBC;
        init();
    }

    @Override
    public String getIdBC() {
        return idBC;
    }

    @Override
//    public final void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    public final void fillItemCategory(NonNullList<ItemStack> items) {
//        if (isInCreativeTab(tab))
        addSubItems(items);
    }

    /** Identical to {@link #fillItemCategory(NonNullList)} in every way, EXCEPT that this is only called if
     * this is actually in the given creative tab. */
//    protected void addSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    protected void addSubItems(NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }

    // Calen: from IItemBuildCraft#init
    // in 1.18.2 setUnlocalizedName setRegistryName are unvailable
    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.unlocalizedName;
    }

    protected String unlocalizedName;

    @Override
    public void setUnlocalizedName(String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
    }
}

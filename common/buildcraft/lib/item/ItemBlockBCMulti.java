/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import java.util.function.Function;

import net.minecraft.item.ItemStack;

import buildcraft.lib.block.BlockBCBase_Neptune;

/** Basically a copy of vanilla ItemMultiTexture, but extends ItemBC_Neptune */
public class ItemBlockBCMulti extends ItemBlockBC_Neptune {
    protected final Function<ItemStack, String> nameFunction;

    public ItemBlockBCMulti(BlockBCBase_Neptune block, Function<ItemStack, String> nameFunction) {
        super(block);
        this.nameFunction = nameFunction;
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    public ItemBlockBCMulti(BlockBCBase_Neptune block, final String[] namesByMeta) {
        this(block, stack -> {
            int meta = stack.getDamage();
            if (meta < 0 || meta >= namesByMeta.length) meta = 0;
            return namesByMeta[meta];
        });
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getMetadata(int damage) {
        return damage;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String getUnlocalizedName(ItemStack stack) {
        return super.getTranslationKey() + "." + this.nameFunction.apply(stack);
    }
}

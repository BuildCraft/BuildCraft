/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;


import buildcraft.lib.block.BlockBCBase_Neptune;
import net.minecraft.world.item.Item;

/** Basically a copy of {@link ItemMultiTexture}, but extends {@link ItemBC_Neptune} */
public class ItemBlockBCMulti extends ItemBlockBC_Neptune {
//    protected final Function<ItemStack, String> nameFunction;

    // public ItemBlockBCMulti(BlockBCBase_Neptune block, Function<ItemStack, String> nameFunction)
    public ItemBlockBCMulti(BlockBCBase_Neptune block, Item.Properties properties) {
        super(block, properties);
//        this.nameFunction = nameFunction;
//        setHasSubtypes(true);
//        setMaxDamage(0);
    }

//    public ItemBlockBCMulti(BlockBCBase_Neptune block, final String[] namesByMeta) {
//        this(block, stack -> {
//            int meta = stack.getMetadata();
//            if (meta < 0 || meta >= namesByMeta.length) meta = 0;
//            return namesByMeta[meta];
//        });
//    }

//    @Override
//    public int getMetadata(int damage) {
//        return damage;
//    }

    // Calen: just use block name
//    @Override
////    public String getUnlocalizedName(ItemStack stack)
//    public Component getName(ItemStack stack) {
////        return super.getUnlocalizedName() + "." + this.nameFunction.apply(stack);
//        return new TranslatableComponent(super.getName(stack).getString() + "." + this.nameFunction.apply(stack));
//    }
}

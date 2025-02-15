/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.item.ItemBlockBCMulti;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemBlockDecorated extends ItemBlockBCMulti {
    public final EnumDecoratedBlock TYPE;

    public ItemBlockDecorated(BlockBCBase_Neptune block, Item.Properties properties, EnumDecoratedBlock type) {
//        super(block, createNameArray());
        super(block, properties);
        this.TYPE = type;
    }

    private static String[] createNameArray() {
        String[] names = new String[EnumDecoratedBlock.VALUES.length];
        for (int i = 0; i < names.length; i++) {
//            names[i] = EnumDecoratedBlock.VALUES[i].getName();
            names[i] = EnumDecoratedBlock.VALUES[i].getSerializedName();
        }
        return names;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack).replace(".name", "") + "." + this.TYPE.getSerializedName() + ".name";
    }

    // Calen: not still useful in 1.18.2
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        for (EnumDecoratedBlock type : EnumDecoratedBlock.VALUES) {
//            addVariant(variants, type.ordinal(), type.getName());
//        }
//    }
}

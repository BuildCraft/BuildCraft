/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import buildcraft.api.core.IEngineType;
import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.item.ItemBlockBCMulti;
import net.minecraft.item.Item;

public class ItemEngine_BC8<E extends Enum<E> & IEngineType> extends ItemBlockBCMulti {
//    private final BlockEngineBase_BC8<E> engineBlock;

    public ItemEngine_BC8(BlockEngineBase_BC8<E> block, Item.Properties properties) {
//        super(block, (stack) -> block.getUnlocalizedName(block.getEngineType(stack.getItemDamage())));
        super(block, properties);
//        engineBlock = block;
    }

//    @Override
//    public String getUnlocalizedName(ItemStack stack)
//    public String getDescriptionId(ItemStack stack) {
//        IBlockState state = engineBlock.getStateFromMeta(stack == null ? 0 : stack.getItemDamage());
//        E engine = state.getValue(engineBlock.getEngineProperty());
//        return "tile." + engineBlock.getUnlocalizedName(engine);
//    }

    // Calen: not still useful in 1.18.2
//    @Override
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        for (E type : engineBlock.getEngineProperty().getAllowedValues()) {
//            int index = type.ordinal();
//            addVariant(variants, index, type.getItemModelLocation());
//        }
//    }
}

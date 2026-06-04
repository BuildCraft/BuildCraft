/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// TODO(R.Chen): blocked by lib.item.ItemBlockBCMulti + lib.engine.BlockEngineBase_BC8 (not yet migrated)
package buildcraft.core.item;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;

import buildcraft.api.core.IEngineType;

import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.item.ItemBlockBCMulti;

public class ItemEngine_BC8<E extends Enum<E> & IEngineType> extends ItemBlockBCMulti {
    private final BlockEngineBase_BC8<E> engineBlock;

    public ItemEngine_BC8(BlockEngineBase_BC8<E> block) {
        super(block, (stack) -> block.getUnlocalizedName(block.getEngineType(stack.getDamage())));
        engineBlock = block;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        BlockState state = engineBlock.getStateFromMeta(stack == null ? 0 : stack.getDamage());
        E engine = state.get(engineBlock.getEngineProperty());
        return "tile." + engineBlock.getUnlocalizedName(engine);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void addModelVariants(HashMap<Integer, ModelIdentifier> variants) {
        for (E type : engineBlock.getEngineProperty().getValues()) {
            int index = type.ordinal();
            addVariant(variants, index, type.getItemModelLocation());
        }
    }
}

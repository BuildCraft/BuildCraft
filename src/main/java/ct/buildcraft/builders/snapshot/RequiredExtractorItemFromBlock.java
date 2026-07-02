/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RequiredExtractorItemFromBlock extends RequiredExtractor {
    @Nonnull
    @Override
    public List<ItemStack> extractItemsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundTag tileNbt, Level level) {
    	if(tileNbt != null && !tileNbt.isEmpty()) {
    		BlockEntity tile = BlockEntity.loadStatic(BlockPos.ZERO, blockState, tileNbt);
    	}
    	ItemStack result ;
    	try {
    		result = blockState.getCloneItemStack(null, null, null, null);
    	}
    	catch (NullPointerException e) {//TODO : Find a better way
    		result = new ItemStack(
                    blockState.getBlock().asItem(),
                    1
                );
		}
        return Collections.singletonList(result);
    }
}

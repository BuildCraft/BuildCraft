/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RequiredExtractorItem extends RequiredExtractor {
    private NbtPath path = null;

    @Nonnull
    @Override
    public List<ItemStack> extractItemsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundTag tileNbt) {
        return Optional.ofNullable(path.get(tileNbt))
                .map(CompoundTag.class::cast)
//                .map(ItemStack::new)
                .map(ItemStack::of)
                .map(Collections::singletonList)
                .orElseGet(Collections::emptyList);
    }

    @Nonnull
    @Override
    public List<ItemStack> extractItemsFromEntity(@Nonnull CompoundTag entityNbt) {
        return Optional.ofNullable(path.get(entityNbt))
                .map(CompoundTag.class::cast)
//            .map(ItemStack::new)
                .map(ItemStack::of)
                .map(Collections::singletonList)
                .orElseGet(Collections::emptyList);
    }
}

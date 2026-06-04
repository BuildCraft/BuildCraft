/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonDeserializer;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import buildcraft.lib.compat.FluidStackBC;

@SuppressWarnings("WeakerAccess")
public abstract class RequiredExtractor {
    @Nonnull
    public List<ItemStack> extractItemsFromBlock(@Nonnull BlockState blockState, @Nullable NbtCompound tileNbt) {
        return Collections.emptyList();
    }

    @Nonnull
    public List<FluidStackBC> extractFluidsFromBlock(@Nonnull BlockState blockState, @Nullable NbtCompound tileNbt) {
        return Collections.emptyList();
    }

    @Nonnull
    public List<ItemStack> extractItemsFromEntity(@Nonnull NbtCompound entityNbt) {
        return Collections.emptyList();
    }

    @Nonnull
    public List<FluidStackBC> extractFluidsFromEntity(@Nonnull NbtCompound entityNbt) {
        return Collections.emptyList();
    }

    public enum EnumType {
        CONSTANT(RequiredExtractorConstant.class),
        ITEM_FROM_BLOCK(RequiredExtractorItemFromBlock.class),
        ITEM(RequiredExtractorItem.class),
        ITEMS_LIST(RequiredExtractorItemsList.class),
        TANK(RequiredExtractorTank.class);

        public final Class<? extends RequiredExtractor> clazz;

        EnumType(Class<? extends RequiredExtractor> clazz) {
            this.clazz = clazz;
        }

        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static EnumType byName(String name) {
            return Arrays.stream(values())
                .filter(type -> type.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Required extractor type not found"));
        }
    }

    public static final JsonDeserializer<RequiredExtractor> DESERIALIZER = (json, typeOfT, context) -> {
        EnumType type = EnumType.byName(json.getAsJsonObject().get("type").getAsString());
        json.getAsJsonObject().remove("type");
        return context.deserialize(json, type.clazz);
    };
}

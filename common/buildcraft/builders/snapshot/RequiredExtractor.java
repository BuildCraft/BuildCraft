/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import com.google.gson.JsonDeserializer;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public abstract class RequiredExtractor {
    @Nonnull
    public List<ItemStack> extractItemsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundNBT tileNbt) {
        return Collections.emptyList();
    }

    @Nonnull
    public List<FluidStack> extractFluidsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundNBT tileNbt) {
        return Collections.emptyList();
    }

    @Nonnull
    public List<ItemStack> extractItemsFromEntity(@Nonnull CompoundNBT entityNbt) {
        return Collections.emptyList();
    }

    @Nonnull
    public List<FluidStack> extractFluidsFromEntity(@Nonnull CompoundNBT entityNbt) {
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

    public static final JsonDeserializer<RequiredExtractor> DESERIALIZER = (json, typeOfT, context) ->
    {
        EnumType type = EnumType.byName(json.getAsJsonObject().get("type").getAsString());
        json.getAsJsonObject().remove("type");
        return context.deserialize(json, type.clazz);
    };
}

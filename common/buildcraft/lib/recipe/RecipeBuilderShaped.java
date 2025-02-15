/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import buildcraft.lib.misc.ItemUtil;
import buildcraft.lib.misc.StackUtil;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

// 1.18.2: use datagen
@Deprecated(forRemoval = true)
public class RecipeBuilderShaped {
    @Nonnull
    private ItemStack result = StackUtil.EMPTY;
    private final List<String> shape = new ArrayList<>();
    private final Char2ObjectOpenHashMap<Object> objects = new Char2ObjectOpenHashMap<>();

    public void clear() {
        result = StackUtil.EMPTY;
        shape.clear();
        objects.clear();
    }

    public RecipeBuilderShaped add(String row) {
        if (shape.size() > 0 && shape.get(0).length() != row.length()) {
            throw new IllegalArgumentException(
                    "Badly sized row! (Other rows = " + shape.get(0).length() + ", given row = " + row.length() + ")");
        }
        shape.add(row);
        return this;
    }

    public RecipeBuilderShaped map(char c, Object... values) {
        boolean put = false;
        for (Object v : values) {
            if (v != null && v != StackUtil.EMPTY) {
                if (v instanceof Item//
                        || v instanceof Block//
                        || v instanceof ItemStack//
                        || v instanceof String) {
                    if (!put) {
                        objects.put(c, v);
                        put = true;
                    }
                } else {
                    throw new IllegalArgumentException("Invalid " + v.getClass());
                }
            }
        }
        if (!put) {
            throw new IllegalArgumentException("Didn't find a non-null value!");
        }
        return this;
    }

    public RecipeBuilderShaped setResult(@Nonnull ItemStack result) {
        this.result = result;
        return this;
    }

    public Object[] createRecipeObjectArray() {
        Object[] objs = new Object[shape.size() + objects.size() * 2];
        int offset = 0;
        for (String s : shape) {
            objs[offset++] = s;
        }
        for (char c : objects.keySet()) {
            objs[offset++] = c;
            objs[offset++] = objects.get(c);
        }
        return objs;
    }

    public Object[] createRecipeObjectArrayNBT() {
        Object[] objs = new Object[shape.size() + objects.size() * 2];
        Object[] original = createRecipeObjectArray();
        for (int i = 0; i < objs.length; i++) {
            Object o = original[i];
            if (o instanceof ItemStack) {
                o = new IngredientNBTBC((ItemStack) o);
            }
            objs[i] = o;
        }
        return objs;
    }

//    public ShapedOreRecipe buildRotated() {
//        int fromRows = shape.size();
//        int toRows = shape.get(0).length();
//        StringBuilder[] strings = new StringBuilder[toRows];
//        for (int toRow = 0; toRow < toRows; toRow++) {
//            strings[toRow] = new StringBuilder();
//        }
//        for (String toAdd : shape) {
//            for (int toRow = 0; toRow < toRows; toRow++) {
//                strings[toRow].append(toAdd.charAt(toRow));
//            }
//        }
//        Object[] objs = new Object[toRows + objects.size() * 2];
//        int offset = 0;
//        for (StringBuilder string : strings) {
//            objs[offset++] = string.toString();
//        }
//        for (char c : objects.keys()) {
//            objs[offset++] = c;
//            objs[offset++] = objects.get(c);
//        }
//        return new ShapedOreRecipe(result.getItem().getRegistryName(), result, objs);
//    }

    private void ensureValid() {
        if (result.isEmpty()) {
            throw new IllegalStateException("Result hasn't been set yet!");
        }
    }

    public void register() {
        ensureValid();
        ResourceLocation name = ItemUtil.getRegistryName(result.getItem());
//        ShapedOreRecipe recipe = new ShapedOreRecipe(name, result, createRecipeObjectArray());
//        ForgeRegistries.RECIPES.register(recipe.setRegistryName(name));
    }

    public void registerNbtAware(String regName) {
        ensureValid();
////        ShapedOreRecipe recipe = new ShapedOreRecipe(result.getItem().getRegistryName(), result, createRecipeObjectArrayNBT());
//        ShapedRecipe recipe = new ShapedRecipe(result.getItem().getRegistryName(), regName, result, createRecipeObjectArrayNBT());
//        ForgeRegistries.RECIPES.register(recipe.setRegistryName(regName));
    }

//    public void registerRotated() {
//        ensureValid();
//        ForgeRegistries.RECIPES.register(buildRotated().setRegistryName(result.getItem().getRegistryName()));
//    }
}

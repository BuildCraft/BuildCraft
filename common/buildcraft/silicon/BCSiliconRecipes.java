/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.core.BCLog;
import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.AssemblyRecipeBasic;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.recipes.IntegrationRecipe;
import buildcraft.api.recipes.StackDefinition;

import buildcraft.lib.BCLib;
import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;

public class BCSiliconRecipes {
    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void init() {
        if (BCLib.DEV) {
            OreDictionary.registerOre("dyeYellow", Blocks.GOLD_BLOCK);
            OreDictionary.registerOre("dyeBlue", Blocks.LAPIS_BLOCK);
            OreDictionary.registerOre("dyeRed", Blocks.REDSTONE_BLOCK);

            StackDefinition target = ArrayStackFilter.definition(Items.POTATO);
            ImmutableList<IngredientStack> required = ImmutableList.of(new IngredientStack(CraftingHelper.getIngredient("dustRedstone")));
            ItemStack output = new ItemStack(Items.BAKED_POTATO, 4);
            IntegrationRecipeRegistry.INSTANCE.addRecipe(new IntegrationRecipe("potato-baker", 100 * MjAPI.MJ, target, required, output));
        }

        Loader.instance().getActiveModList().forEach((mod) -> {
            JsonContext ctx = new JsonContext(mod.getModId());
            CraftingHelper.findFiles(mod, "assets/" + mod.getModId() + "/assemblyRecipes", null,
                (root, file) -> {
                    String path = root.relativize(file).toString();
                    if (!FilenameUtils.getExtension(file.toString()).equals("json"))
                        return true;
                    String name = FilenameUtils.removeExtension(path).replaceAll("\\\\", "/");
                    ResourceLocation key = new ResourceLocation(mod.getModId(), name);
                    BufferedReader reader = null;
                    try {
                        reader = Files.newBufferedReader(file);
                        JsonObject json = JsonUtils.fromJson(GSON, reader, JsonObject.class);
                        if (json == null || json.isJsonNull())
                            throw new JsonSyntaxException("Json is null (empty file?)");

                        ItemStack output = CraftingHelper.getItemStack(json.getAsJsonObject("result"), ctx);
                        long powercost = json.get("MJ").getAsLong() * MjAPI.MJ;

                        ArrayList<IngredientStack> ingredients = new ArrayList<>();

                        json.getAsJsonArray("components").forEach(
                            element -> {
                                JsonObject object = element.getAsJsonObject();
                                ingredients.add(new IngredientStack(CraftingHelper.getIngredient(object.get("ingredient"), ctx), JsonUtils.getInt(object, "amount", 1)));
                            }
                        );

                        AssemblyRecipeRegistry.REGISTRY.register(new AssemblyRecipeBasic(key, powercost, ImmutableSet.copyOf(ingredients), output));


                    } catch (IOException e) {
                        BCLog.logger.error("Couldn't read recipe {} from {}", key, file, e);
                        return false;
                    } finally {
                        IOUtils.closeQuietly(reader);
                    }
                    return true;
                });
        });
    }

    private static void addChipsetAssembly(int multiplier, String additional, EnumRedstoneChipset type) {
        ItemStack output = type.getStack();
        ImmutableSet.Builder<IngredientStack> inputs = ImmutableSet.builder();
        inputs.add(new IngredientStack(CraftingHelper.getIngredient("dustRedstone")));
        if (additional != null) {
            inputs.add(new IngredientStack(CraftingHelper.getIngredient(additional)));
        }

        String name = String.format("chipset-%s", type);
        AssemblyRecipe recp = new AssemblyRecipeBasic(name, multiplier * 10_000L * MjAPI.MJ, inputs.build(), output);
        AssemblyRecipeRegistry.REGISTRY.register(recp);
    }
}

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

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.core.BCLog;
import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.AssemblyRecipeBasic;
import buildcraft.api.recipes.IngredientStack;

import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.IntegrationRecipeBasic;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;

@Mod.EventBusSubscriber(modid = BCSilicon.MODID)
public class BCSiliconRecipes {
    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
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

                        AssemblyRecipeRegistry.REGISTRY.put(key, new AssemblyRecipeBasic(key, powercost, ImmutableSet.copyOf(ingredients), output));


                    } catch (IOException e) {
                        BCLog.logger.error("Couldn't read recipe {} from {}", key, file, e);
                        return false;
                    } finally {
                        IOUtils.closeQuietly(reader);
                    }
                    return true;
                });

            CraftingHelper.findFiles(mod, "assets/" + mod.getModId() + "/integrationRecipes", null,
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
                        IngredientStack centerStack = IngredientStack.of(CraftingHelper.getIngredient(json.getAsJsonObject("centerStack"), ctx));
                        long powercost = json.get("MJ").getAsLong() * MjAPI.MJ;

                        ArrayList<IngredientStack> ingredients = new ArrayList<>();

                        json.getAsJsonArray("components").forEach(
                            element -> {
                                JsonObject object = element.getAsJsonObject();
                                ingredients.add(new IngredientStack(CraftingHelper.getIngredient(object.get("ingredient"), ctx), JsonUtils.getInt(object, "amount", 1)));
                            }
                        );

                        IntegrationRecipeRegistry.INSTANCE.addRecipe(new IntegrationRecipeBasic(key, powercost, centerStack, ingredients, output));


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
        AssemblyRecipeRegistry.REGISTRY.put(recp.getRegistryName(), recp);
    }
}

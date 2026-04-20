/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon;

import ct.buildcraft.lib.recipe.AssemblyRecipe;
import ct.buildcraft.lib.recipe.AssemblyRecipeBasic;
import ct.buildcraft.silicon.recipe.FacadeAssemblyRecipes;
import ct.buildcraft.silicon.recipe.FacadeSwapRecipe;
import ct.buildcraft.silicon.recipe.GateLogicChangeRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = BCSilicon.MODID)
public class BCSiliconRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, BCSilicon.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, BCSilicon.MODID);
    public static final RegistryObject<RecipeType<AssemblyRecipeBasic>> ASSEMBLY_TYPE = TYPES.register("assembly", () -> RecipeType.simple(new ResourceLocation("buildcraftsilicon:assembly")));
    public static final RegistryObject<RecipeSerializer<AssemblyRecipe>> ASSEMBLY_SERIALIZER = SERIALIZERS.register("assembly", AssemblyRecipe.Serializer::new);
    public static final RegistryObject<SimpleRecipeSerializer<GateLogicChangeRecipe>> GATE_CHANGE_SERIALIZER = SERIALIZERS.register("gate_logic_change", () -> new SimpleRecipeSerializer<GateLogicChangeRecipe>(GateLogicChangeRecipe::new));
    public static final RegistryObject<SimpleRecipeSerializer<FacadeAssemblyRecipes>> FACADE_SERIALIZER = SERIALIZERS.register("facade", () -> new SimpleRecipeSerializer<FacadeAssemblyRecipes>(FacadeAssemblyRecipes::getInstance)); 
    
	public static void preInit(IEventBus modEventBus) {
		SERIALIZERS.register("facade_swap_recipe_", () -> FacadeSwapRecipe.SERIALIZER);
        TYPES.register("facade_swap", () -> FacadeSwapRecipe.TYPE);
		TYPES.register(modEventBus);
		SERIALIZERS.register(modEventBus);
		
	}
    
    public static void registerRecipes() {

    }

}

/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft;

import buildcraft.api.fuels.IronEngineCoolant;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.core.recipes.RefineryRecipeManager;
import buildcraft.core.BlockIndex;
import buildcraft.core.BlockSpring;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.fluids.BCFluid;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.energy.BlockBuildcraftFluid;
import buildcraft.energy.BlockEngine;
import buildcraft.energy.BptBlockEngine;
import buildcraft.energy.EnergyProxy;
import buildcraft.energy.GuiHandler;
import buildcraft.energy.ItemBucketBuildcraft;
import buildcraft.energy.ItemEngine;
import buildcraft.energy.BucketHandler;
import buildcraft.energy.TileEngine.EnergyStage;
import buildcraft.energy.triggers.TriggerEngineHeat;
import buildcraft.energy.worldgen.BiomeGenOilDesert;
import buildcraft.energy.worldgen.BiomeGenOilOcean;
import buildcraft.energy.worldgen.BiomeInitializer;
import buildcraft.energy.worldgen.OilPopulate;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

@Mod(name = "BuildCraft Energy", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Energy", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftEnergy extends BuildCraftMod {

	public final static int ENERGY_REMOVE_BLOCK = 25;
	public final static int ENERGY_EXTRACT_ITEM = 2;
	public static boolean spawnOilSprings = true;
	public static BiomeGenOilDesert biomeOilDesert;
	public static BiomeGenOilOcean biomeOilOcean;
	public static BlockEngine engineBlock;
	private static Fluid buildcraftFluidOil;
	private static Fluid buildcraftFluidFuel;
	public static Fluid fluidOil;
	public static Fluid fluidFuel;
	public static Block blockOil;
	public static Block blockFuel;
	public static Item bucketOil;
	public static Item bucketFuel;
	public static Item fuel;
	public static boolean canOilBurn;
	public static double oilWellScalar = 1.0;
	public static TreeMap<BlockIndex, Integer> saturationStored = new TreeMap<BlockIndex, Integer>();
	public static BCTrigger triggerBlueEngineHeat = new TriggerEngineHeat(EnergyStage.BLUE);
	public static BCTrigger triggerGreenEngineHeat = new TriggerEngineHeat(EnergyStage.GREEN);
	public static BCTrigger triggerYellowEngineHeat = new TriggerEngineHeat(EnergyStage.YELLOW);
	public static BCTrigger triggerRedEngineHeat = new TriggerEngineHeat(EnergyStage.RED);
	@Instance("BuildCraft|Energy")
	public static BuildCraftEnergy instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		// Update oil tag
		int defaultOilId = DefaultProps.OIL_ID;
		if (BuildCraftCore.mainConfiguration.hasKey(Configuration.CATEGORY_BLOCK, "oilStill.id")) {
			defaultOilId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_BLOCK, "oilStill.id", defaultOilId).getInt(defaultOilId);
			BuildCraftCore.mainConfiguration.getCategory(Configuration.CATEGORY_BLOCK).remove("oilStill.id");
		}
		
		int oilDesertBiomeId = BuildCraftCore.mainConfiguration.get("biomes", "oilDesert", DefaultProps.BIOME_OIL_DESERT).getInt(DefaultProps.BIOME_OIL_DESERT);
		int oilOceanBiomeId = BuildCraftCore.mainConfiguration.get("biomes", "oilOcean", DefaultProps.BIOME_OIL_OCEAN).getInt(DefaultProps.BIOME_OIL_OCEAN);
		canOilBurn = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "burnOil", true, "Can oil burn?").getBoolean(true);
		oilWellScalar = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "oilWellGenerationRate", 1.0, "Probability of oil well generation").getDouble(1.0);

		double fuelOilMultiplier = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "fuel.oil.combustion", 1.0F, "adjust energy value of Oil in Combustion Engines").getDouble(1.0F);
		double fuelFuelMultiplier = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "fuel.fuel.combustion", 1.0F, "adjust energy value of Fuel in Combustion Engines").getDouble(1.0F);
		BuildCraftCore.mainConfiguration.save();

		class BiomeIdException extends RuntimeException {

			public BiomeIdException(String biome, int id) {
				super(String.format("You have a Biome Id conflict at %d for %s", id, biome));
			}
		}

		if (oilDesertBiomeId > 0) {
			if (BiomeGenBase.biomeList[oilDesertBiomeId] != null) {
				throw new BiomeIdException("oilDesert", oilDesertBiomeId);
			}
			biomeOilDesert = BiomeGenOilDesert.makeBiome(oilDesertBiomeId);
		}

		if (oilOceanBiomeId > 0) {
			if (BiomeGenBase.biomeList[oilOceanBiomeId] != null) {
				throw new BiomeIdException("oilOcean", oilOceanBiomeId);
			}
			biomeOilOcean = BiomeGenOilOcean.makeBiome(oilOceanBiomeId);
		}


		engineBlock = new BlockEngine();
		CoreProxy.proxy.registerBlock(engineBlock, ItemEngine.class);

		LanguageRegistry.addName(new ItemStack(engineBlock, 1, 0), "Redstone Engine");
		LanguageRegistry.addName(new ItemStack(engineBlock, 1, 1), "Steam Engine");
		LanguageRegistry.addName(new ItemStack(engineBlock, 1, 2), "Combustion Engine");


		// Oil and fuel
		buildcraftFluidOil = new BCFluid("oil").setDensity(800).setViscosity(1500);

		FluidRegistry.registerFluid(buildcraftFluidOil);
		fluidOil = FluidRegistry.getFluid("oil");

		buildcraftFluidFuel = new BCFluid("fuel");
		FluidRegistry.registerFluid(buildcraftFluidFuel);
		fluidFuel = FluidRegistry.getFluid("fuel");

		if (fluidOil.getBlock() == null) {
			blockOil = new BlockBuildcraftFluid(fluidOil, Material.water).setFlammable(canOilBurn).setFlammability(0);
			blockOil.setBlockName("blockOil");
			CoreProxy.proxy.addName(blockOil, "Oil");
			CoreProxy.proxy.registerBlock(blockOil);
			fluidOil.setBlock(blockOil);		
		} else {
			blockOil = fluidOil.getBlock();
		}

		if (blockOil != null) {
			spawnOilSprings = BuildCraftCore.mainConfiguration.get("worldgen", "oilSprings", true).getBoolean(true);
			BlockSpring.EnumSpring.OIL.canGen = spawnOilSprings;
			BlockSpring.EnumSpring.OIL.liquidBlock = blockOil;
		}

		if (fluidFuel.getBlock() == null) {
			blockFuel = new BlockBuildcraftFluid(fluidFuel, Material.water).setFlammable(true).setFlammability(5).setParticleColor(0.7F, 0.7F, 0.0F);
			blockFuel.setBlockName("blockFuel");
			CoreProxy.proxy.addName(blockFuel, "Fuel");
			CoreProxy.proxy.registerBlock(blockFuel);
			fluidFuel.setBlock(blockFuel);			
		} else {
			blockFuel = fluidFuel.getBlock();
		}

		// Buckets

		if (blockOil != null) {
			bucketOil = new ItemBucketBuildcraft(blockOil);
			bucketOil.setUnlocalizedName("bucketOil").setContainerItem(Items.bucket);
			LanguageRegistry.addName(bucketOil, "Oil Bucket");
			CoreProxy.proxy.registerItem(bucketOil);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("oil", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketOil), new ItemStack(Items.bucket));
		}

		if (blockFuel != null) {
			bucketFuel = new ItemBucketBuildcraft(blockFuel);
			bucketFuel.setUnlocalizedName("bucketFuel").setContainerItem(Items.bucket);
			LanguageRegistry.addName(bucketFuel, "Fuel Bucket");
			CoreProxy.proxy.registerItem(bucketFuel);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("fuel", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketFuel), new ItemStack(Items.bucket));
		}

		BucketHandler.INSTANCE.buckets.put(blockOil, bucketOil);
		BucketHandler.INSTANCE.buckets.put(blockFuel, bucketFuel);
		MinecraftForge.EVENT_BUS.register(BucketHandler.INSTANCE);

		BuildcraftRecipes.refinery.addRecipe(new FluidStack(fluidOil, 1), new FluidStack(fluidFuel, 1), 12, 1);

		// Iron Engine Fuels
//		IronEngineFuel.addFuel("lava", 1, 20000);
		IronEngineFuel.addFuel("oil", 3, (int) (5000 * fuelOilMultiplier));
		IronEngineFuel.addFuel("fuel", 6, (int) (25000 * fuelFuelMultiplier));

		// Iron Engine Coolants
		IronEngineCoolant.addCoolant(FluidRegistry.getFluid("water"), 0.0023F);
		IronEngineCoolant.addCoolant(Blocks.ice, 0, FluidRegistry.getFluidStack("water", FluidContainerRegistry.BUCKET_VOLUME * 2));

		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

		//new BptBlockEngine(engineBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
		EnergyProxy.proxy.registerBlockRenderers();
		EnergyProxy.proxy.registerTileEntities();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		if (BuildCraftCore.modifyWorld) {
			MinecraftForge.EVENT_BUS.register(OilPopulate.INSTANCE);
			MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeInitializer());
		}
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Post event) {
		if (event.map.textureType == 0) {
			buildcraftFluidOil.setIcons(blockOil.getBlockTextureFromSide(1), blockOil.getBlockTextureFromSide(2));
			buildcraftFluidFuel.setIcons(blockFuel.getBlockTextureFromSide(1), blockFuel.getBlockTextureFromSide(2));
		}
	}

	public static void loadRecipes() {
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 0),
				new Object[]{"www", " g ", "GpG", 'w', "plankWood", 'g', Blocks.glass, 'G',
			BuildCraftCore.woodenGearItem, 'p', Blocks.piston});
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 1), new Object[]{"www", " g ", "GpG", 'w', "cobblestone",
			'g', Blocks.glass, 'G', BuildCraftCore.stoneGearItem, 'p', Blocks.piston});
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 2), new Object[]{"www", " g ", "GpG", 'w', Items.iron_ingot,
			'g', Blocks.glass, 'G', BuildCraftCore.ironGearItem, 'p', Blocks.piston});
	}

	@EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}
}

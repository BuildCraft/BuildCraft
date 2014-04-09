/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.api.fuels.IronEngineCoolant;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.builders.filler.FillerRegistry;
import buildcraft.core.BlockIndex;
import buildcraft.core.BlockSpring;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.utils.StringUtils;
import buildcraft.energy.BlockBuildcraftFluid;
import buildcraft.energy.BlockEnergyEmitter;
import buildcraft.energy.BlockEnergyReceiver;
import buildcraft.energy.BlockEngine;
import buildcraft.energy.BucketHandler;
import buildcraft.energy.EnergyProxy;
import buildcraft.energy.GuiHandler;
import buildcraft.energy.ItemBucketBuildcraft;
import buildcraft.energy.ItemEngine;
import buildcraft.energy.SchematicEngine;
import buildcraft.energy.TileEnergyEmitter;
import buildcraft.energy.TileEnergyReceiver;
import buildcraft.energy.TileEngine.EnergyStage;
import buildcraft.energy.triggers.TriggerEngineHeat;
import buildcraft.energy.worldgen.BiomeGenOilDesert;
import buildcraft.energy.worldgen.BiomeGenOilOcean;
import buildcraft.energy.worldgen.BiomeInitializer;
import buildcraft.energy.worldgen.OilPopulate;
import buildcraft.transport.network.PacketHandlerTransport;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(name = "BuildCraft Energy", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Energy", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftEnergy extends BuildCraftMod {

	public final static int ENERGY_REMOVE_BLOCK = 25;
	public final static int ENERGY_EXTRACT_ITEM = 2;
	public static boolean spawnOilSprings = true;
	public static BiomeGenOilDesert biomeOilDesert;
	public static BiomeGenOilOcean biomeOilOcean;
	public static BlockEngine engineBlock;
	public static BlockEnergyEmitter emitterBlock;
	public static BlockEnergyReceiver receiverBlock;
	private static Fluid buildcraftFluidOil;
	private static Fluid buildcraftFluidFuel;
	private static Fluid buildcraftFluidRedPlasma;
	private static Fluid buildcraftFluidTar;
	private static Fluid buildcraftFluidClearOil;
	private static Fluid buildcraftFluidDirtyFuel;
	private static Fluid buildcraftFluidLPG;
	public static Fluid fluidOil;
	public static Fluid fluidFuel;
	public static Fluid fluidRedPlasma;
	public static Fluid fluidTar;
	public static Fluid fluidClearOil;
	public static Fluid fluidDirtyFuel;
	public static Fluid fluidLPG;
	public static Block blockOil;
	public static Block blockFuel;
	public static Block blockRedPlasma;
	public static Block blockTar;
	public static Block blockClearOil;
	public static Block blockDirtyFuel;
	public static Block blockLPG;
	public static Item bucketOil;
	public static Item bucketFuel;
	public static Item bucketTar;
	public static Item bucketRedPlasma;
	public static Item bucketClearOil;
	public static Item bucketDirtyFuel;
	public static Item bucketLPG;
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
		int oilDesertBiomeId = BuildCraftCore.mainConfiguration.get("biomes", "biomeOilDesert", DefaultProps.BIOME_OIL_DESERT).getInt(DefaultProps.BIOME_OIL_DESERT);
		int oilOceanBiomeId = BuildCraftCore.mainConfiguration.get("biomes", "biomeOilOcean", DefaultProps.BIOME_OIL_OCEAN).getInt(DefaultProps.BIOME_OIL_OCEAN);
		canOilBurn = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "burnOil", true, "Can oil burn?").getBoolean(true);
		oilWellScalar = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "oilWellGenerationRate", 1.0, "Probability of oil well generation").getDouble(1.0);

		double fuelOilMultiplier = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "fuel.oil.combustion", 1.0F, "adjust energy value of Oil in Combustion Engines").getDouble(1.0F);
		double fuelFuelMultiplier = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "fuel.fuel.combustion", 1.0F, "adjust energy value of Fuel in Combustion Engines").getDouble(1.0F);
		BuildCraftCore.mainConfiguration.save();

		if (oilDesertBiomeId > 0) {
			if (BiomeGenBase.getBiomeGenArray () [oilDesertBiomeId] != null) {
				oilDesertBiomeId = findUnusedBiomeID("oilDesert");
				// save changes to config file
				BuildCraftCore.mainConfiguration.get("biomes", "biomeOilDesert", oilDesertBiomeId).set(oilDesertBiomeId);
				BuildCraftCore.mainConfiguration.save();
			}
			biomeOilDesert = BiomeGenOilDesert.makeBiome(oilDesertBiomeId);
		}

		if (oilOceanBiomeId > 0) {
			if (BiomeGenBase.getBiomeGenArray () [oilOceanBiomeId] != null) {
				oilOceanBiomeId = findUnusedBiomeID("oilOcean");
				// save changes to config file
				BuildCraftCore.mainConfiguration.get("biomes", "biomeOilOcean", oilOceanBiomeId).set(oilOceanBiomeId);
				BuildCraftCore.mainConfiguration.save();
			}
			biomeOilOcean = BiomeGenOilOcean.makeBiome(oilOceanBiomeId);
		}

		engineBlock = new BlockEngine(CreativeTabBuildCraft.TIER_1);
		CoreProxy.proxy.registerBlock(engineBlock, ItemEngine.class);

		LanguageRegistry.addName(new ItemStack(engineBlock, 1, 0), "Redstone Engine");
		LanguageRegistry.addName(new ItemStack(engineBlock, 1, 1), "Steam Engine");
		LanguageRegistry.addName(new ItemStack(engineBlock, 1, 2), "Combustion Engine");


		// Oil and fuel
		buildcraftFluidOil = new Fluid("oil").setDensity(800).setViscosity(1500);
		FluidRegistry.registerFluid(buildcraftFluidOil);
		fluidOil = FluidRegistry.getFluid("oil");

		buildcraftFluidFuel = new Fluid("fuel");
		FluidRegistry.registerFluid(buildcraftFluidFuel);
		fluidFuel = FluidRegistry.getFluid("fuel");

		buildcraftFluidRedPlasma = new Fluid("redplasma").setDensity(10000).setViscosity(10000).setLuminosity(30);
		FluidRegistry.registerFluid(buildcraftFluidRedPlasma);
		fluidRedPlasma = FluidRegistry.getFluid("redplasma");
		
		buildcraftFluidTar = new Fluid("tar").setDensity(1000).setViscosity(1000);
		FluidRegistry.registerFluid(buildcraftFluidTar);
		fluidTar = FluidRegistry.getFluid("tar");
		
		buildcraftFluidClearOil = new Fluid("clearoil").setDensity(1000).setViscosity(1000);
		FluidRegistry.registerFluid(buildcraftFluidClearOil);
		fluidClearOil = FluidRegistry.getFluid("clearoil");
		
		buildcraftFluidDirtyFuel = new Fluid("dirtyfuel").setDensity(1000).setViscosity(1000);
		FluidRegistry.registerFluid(buildcraftFluidDirtyFuel);
		fluidDirtyFuel = FluidRegistry.getFluid("dirtyfuel");
		
		buildcraftFluidLPG = new Fluid("lpg").setDensity(1000).setViscosity(1000);
		FluidRegistry.registerFluid(buildcraftFluidLPG);
		fluidLPG = FluidRegistry.getFluid("lpg");

		if (fluidOil.getBlock() == null) {
			blockOil = new BlockBuildcraftFluid(fluidOil, Material.water).setFlammable(canOilBurn).setFlammability(0);
			blockOil.setBlockName("blockOil");
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
			CoreProxy.proxy.registerBlock(blockFuel);
			fluidFuel.setBlock(blockFuel);
		} else {
			blockFuel = fluidFuel.getBlock();
		}

		if (fluidRedPlasma.getBlock() == null) {
			blockRedPlasma = new BlockBuildcraftFluid(fluidRedPlasma, Material.water).setFlammable(false).setParticleColor(0.9F, 0, 0);
			blockRedPlasma.setBlockName("blockRedPlasma");
			CoreProxy.proxy.registerBlock(blockRedPlasma);
			fluidRedPlasma.setBlock(blockRedPlasma);
		} else {
			blockRedPlasma = fluidRedPlasma.getBlock();
		}
		
		if (fluidTar.getBlock() == null){
			blockTar = new BlockBuildcraftFluid(fluidTar, Material.water).setFlammable(true).setParticleColor(0.5F, 0.5F, 0.5F);
			blockTar.setBlockName("blockTar");
			CoreProxy.proxy.registerBlock(blockTar);
			fluidTar.setBlock(blockTar);
		} else {
			blockTar = fluidTar.getBlock();
		}
		
		if (fluidClearOil.getBlock() == null){
			blockClearOil = new BlockBuildcraftFluid(fluidClearOil, Material.water).setFlammable(true).setParticleColor(0.5F, 0.5F, 0.5F);
			blockClearOil.setBlockName("blockClearOil");
			CoreProxy.proxy.registerBlock(blockClearOil);
			fluidClearOil.setBlock(blockClearOil);
		} else {
			blockClearOil = fluidClearOil.getBlock();
		}
		
		if (fluidDirtyFuel.getBlock() == null){
			blockDirtyFuel = new BlockBuildcraftFluid(fluidDirtyFuel, Material.water).setFlammable(true).setParticleColor(0.5F, 0.5F, 0.5F);
			blockDirtyFuel.setBlockName("blockDirtyFuel");
			CoreProxy.proxy.registerBlock(blockDirtyFuel);
			fluidDirtyFuel.setBlock(blockDirtyFuel);
		} else {
			blockDirtyFuel = fluidDirtyFuel.getBlock();
		}
		
		if (fluidLPG.getBlock() == null){
			blockLPG = new BlockBuildcraftFluid(fluidLPG, Material.water).setFlammable(true).setParticleColor(0.5F, 0.5F, 0.5F);
			blockLPG.setBlockName("blockLPG");
			CoreProxy.proxy.registerBlock(blockLPG);
			fluidLPG.setBlock(blockLPG);
		} else {
			blockLPG = fluidLPG.getBlock();
		}

		// Buckets

		if (blockOil != null) {
			bucketOil = new ItemBucketBuildcraft(blockOil, CreativeTabBuildCraft.TIER_2);
			bucketOil.setUnlocalizedName("bucketOil").setContainerItem(Items.bucket);
			LanguageRegistry.addName(bucketOil, "Oil Bucket");
			CoreProxy.proxy.registerItem(bucketOil);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("oil", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketOil), new ItemStack(Items.bucket));
		}

		if (blockFuel != null) {
			bucketFuel = new ItemBucketBuildcraft(blockFuel, CreativeTabBuildCraft.TIER_2);
			bucketFuel.setUnlocalizedName("bucketFuel").setContainerItem(Items.bucket);
			LanguageRegistry.addName(bucketFuel, "Fuel Bucket");
			CoreProxy.proxy.registerItem(bucketFuel);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("fuel", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketFuel), new ItemStack(Items.bucket));
		}
		if (blockTar != null){
			bucketTar = new ItemBucketBuildcraft(blockTar, CreativeTabBuildCraft.TIER_2);
			bucketTar.setUnlocalizedName("bucketTar").setContainerItem(Items.bucket);
			LanguageRegistry.addName(bucketTar, "Tar Bucket");
			CoreProxy.proxy.registerItem(bucketTar);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("tar", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketTar), new ItemStack(Items.bucket));
		}
		
		if (blockClearOil != null){
			bucketClearOil = new ItemBucketBuildcraft(blockClearOil, CreativeTabBuildCraft.TIER_2);
			bucketClearOil.setUnlocalizedName("bucketClearOil").setContainerItem(Items.bucket);
			LanguageRegistry.addName(bucketClearOil, "Clear Oil Bucket");
			CoreProxy.proxy.registerItem(bucketClearOil);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("clearOil", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketClearOil), new ItemStack(Items.bucket));
		}
		
		if (blockDirtyFuel != null){
			bucketDirtyFuel = new ItemBucketBuildcraft(blockDirtyFuel, CreativeTabBuildCraft.TIER_2);
			bucketDirtyFuel.setUnlocalizedName("bucketDirtyFuel").setContainerItem(Items.bucket);
			LanguageRegistry.addName(bucketTar, "Dirty Fuel Bucket");
			CoreProxy.proxy.registerItem(bucketDirtyFuel);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("dirtyFuel", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketDirtyFuel), new ItemStack(Items.bucket));
		}
		
		if (blockLPG != null){
			bucketLPG = new ItemBucketBuildcraft(blockLPG, CreativeTabBuildCraft.TIER_2);
			bucketLPG.setUnlocalizedName("bucketLPG").setContainerItem(Items.bucket);
			LanguageRegistry.addName(bucketLPG, "LPG Bucket");
			CoreProxy.proxy.registerItem(bucketLPG);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("lpg", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketTar), new ItemStack(Items.bucket));
		}

		if (!BuildCraftCore.NEXTGEN_PREALPHA) {
			if (blockRedPlasma != null) {
				bucketRedPlasma = new ItemBucketBuildcraft(blockRedPlasma, CreativeTabBuildCraft.TIER_4);
				bucketRedPlasma.setUnlocalizedName("bucketRedPlasma").setContainerItem(Items.bucket);
				LanguageRegistry.addName(bucketRedPlasma, "Red Plasma Bucket");
				CoreProxy.proxy.registerItem(bucketRedPlasma);
				FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("redplasma", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketRedPlasma), new ItemStack(Items.bucket));
			}
		}

		// BucketHandler ensures empty buckets fill with the correct liquid.
		BucketHandler.INSTANCE.buckets.put(blockOil, bucketOil);
		BucketHandler.INSTANCE.buckets.put(blockFuel, bucketFuel);
		BucketHandler.INSTANCE.buckets.put(blockTar, bucketTar);
		MinecraftForge.EVENT_BUS.register(BucketHandler.INSTANCE);

		BuildcraftRecipes.refinery.addRecipe(new FluidStack(fluidOil, 1), new FluidStack(fluidFuel, 1), 12, 1);

		// Iron Engine Fuels
//		IronEngineFuel.addFuel("lava", 1, 20000);
		IronEngineFuel.addFuel("oil", 3, (int) (5000 * fuelOilMultiplier));
		IronEngineFuel.addFuel("fuel", 6, (int) (25000 * fuelFuelMultiplier));

		// Iron Engine Coolants
		IronEngineCoolant.addCoolant(FluidRegistry.getFluid("water"), 0.0023F);
		IronEngineCoolant.addCoolant(Blocks.ice, 0, FluidRegistry.getFluidStack("water", FluidContainerRegistry.BUCKET_VOLUME * 2));

		// Receiver / emitter

		if (!BuildCraftCore.NEXTGEN_PREALPHA) {
			emitterBlock = new BlockEnergyEmitter ();
			CoreProxy.proxy.registerBlock(emitterBlock.setBlockName("energyEmitterBlock"));
			CoreProxy.proxy.registerTileEntity(TileEnergyEmitter.class, "net.minecraft.src.builders.TileEnergyEmitter");

			receiverBlock = new BlockEnergyReceiver ();
			CoreProxy.proxy.registerBlock(receiverBlock.setBlockName("energyReceiverBlock"));
			CoreProxy.proxy.registerTileEntity(TileEnergyReceiver.class, "net.minecraft.src.builders.TileEnergyReceiver");
		}

		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-ENERGY", new PacketHandlerTransport());

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

		SchematicRegistry.registerSchematicBlock(engineBlock, SchematicEngine.class);

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

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Post event) {
		if (event.map.getTextureType() == 0) {
			buildcraftFluidOil.setIcons(blockOil.getBlockTextureFromSide(1), blockOil.getBlockTextureFromSide(2));
			buildcraftFluidFuel.setIcons(blockFuel.getBlockTextureFromSide(1), blockFuel.getBlockTextureFromSide(2));
			buildcraftFluidRedPlasma.setIcons(blockRedPlasma.getBlockTextureFromSide(1), blockRedPlasma.getBlockTextureFromSide(2));
			buildcraftFluidTar.setIcons(blockTar.getBlockTextureFromSide(1), blockTar.getBlockTextureFromSide(2));
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

	private int findUnusedBiomeID (String biomeName) {
		int freeBiomeID = 0;
		// code to find a free biome
		for (int i = 1; i < 256; i++) {
			if (BiomeGenBase.getBiomeGenArray()[i] == null) {
				freeBiomeID = i;
				return freeBiomeID;
			}
		}
		// failed to find any free biome IDs
		class BiomeIdLimitException extends RuntimeException {
			public BiomeIdLimitException(String biome) {
				super(String.format("You have a run out of free Biome Ids for %s", biome));
			}
		}

		throw new BiomeIdLimitException(biomeName);
	}

	@EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}
}

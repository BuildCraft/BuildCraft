/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.JavaTools;
import buildcraft.api.fuels.IronEngineCoolant;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.core.BlockIndex;
import buildcraft.core.BlockSpring;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.energy.BlockBuildcraftFluid;
import buildcraft.energy.BlockEnergyConverter;
import buildcraft.energy.BlockEnergyEmitter;
import buildcraft.energy.BlockEnergyReceiver;
import buildcraft.energy.BlockEngine;
import buildcraft.energy.BucketHandler;
import buildcraft.energy.EnergyProxy;
import buildcraft.energy.GuiHandler;
import buildcraft.energy.ItemBucketBuildcraft;
import buildcraft.energy.ItemEnergyConverter;
import buildcraft.energy.ItemEngine;
import buildcraft.energy.SchematicEngine;
import buildcraft.energy.TileEnergyConverter;
import buildcraft.energy.TileEnergyEmitter;
import buildcraft.energy.TileEnergyReceiver;
import buildcraft.energy.TileEngine;
import buildcraft.energy.TileEngine.EnergyStage;
import buildcraft.energy.triggers.TriggerEngineHeat;
import buildcraft.energy.worldgen.BiomeGenOilDesert;
import buildcraft.energy.worldgen.BiomeGenOilOcean;
import buildcraft.energy.worldgen.BiomeInitializer;
import buildcraft.energy.worldgen.OilPopulate;
import buildcraft.transport.network.PacketHandlerTransport;

@Mod(name = "BuildCraft Energy", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Energy", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftEnergy extends BuildCraftMod {

	public static final int ENERGY_REMOVE_BLOCK = 25;
	public static final int ENERGY_EXTRACT_ITEM = 2;
	public static boolean spawnOilSprings = true;
	public static BiomeGenOilDesert biomeOilDesert;
	public static BiomeGenOilOcean biomeOilOcean;
	public static BlockEngine engineBlock;
	public static BlockEnergyEmitter emitterBlock;
	public static BlockEnergyReceiver receiverBlock;
	public static Fluid fluidOil;
	public static Fluid fluidFuel;
	public static Fluid fluidRedPlasma;
	public static Block blockOil;
	public static Block blockFuel;
	public static Block blockRedPlasma;
	public static Block blockEnergyConverter;
	public static Item bucketOil;
	public static Item bucketFuel;
	public static Item bucketRedPlasma;
	public static Item fuel;
	public static boolean canOilBurn;
	public static double oilWellScalar = 1.0;
	public static Set<Integer> oilBiomeIDs = new HashSet<Integer>();
	public static Set<Integer> excessiveOilBiomeIDs = new HashSet<Integer>();
	public static Set<Integer> excludeOilBiomeIDs = new HashSet<Integer>();
	public static TreeMap<BlockIndex, Integer> saturationStored = new TreeMap<BlockIndex, Integer>();
	public static BCTrigger triggerBlueEngineHeat = new TriggerEngineHeat(EnergyStage.BLUE);
	public static BCTrigger triggerGreenEngineHeat = new TriggerEngineHeat(EnergyStage.GREEN);
	public static BCTrigger triggerYellowEngineHeat = new TriggerEngineHeat(EnergyStage.YELLOW);
	public static BCTrigger triggerRedEngineHeat = new TriggerEngineHeat(EnergyStage.RED);
	@Mod.Instance("BuildCraft|Energy")
	public static BuildCraftEnergy instance;

	private static Fluid buildcraftFluidOil;
	private static Fluid buildcraftFluidFuel;
	private static Fluid buildcraftFluidRedPlasma;


	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		SchematicRegistry.declareBlueprintSupport("BuildCraft|Energy");

		int oilDesertBiomeId = BuildCraftCore.mainConfiguration.get("biomes", "biomeOilDesert", DefaultProps.BIOME_OIL_DESERT).getInt(DefaultProps.BIOME_OIL_DESERT);
		int oilOceanBiomeId = BuildCraftCore.mainConfiguration.get("biomes", "biomeOilOcean", DefaultProps.BIOME_OIL_OCEAN).getInt(DefaultProps.BIOME_OIL_OCEAN);
		canOilBurn = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "burnOil", true, "Can oil burn?").getBoolean(true);
		oilWellScalar = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "oilWellGenerationRate", 1.0, "Probability of oil well generation").getDouble(1.0);

		setBiomeList(
				oilBiomeIDs,
				BuildCraftCore.mainConfiguration
						.get(Configuration.CATEGORY_GENERAL, "oil.increasedBiomeIDs",
								new String[] {BiomeDictionary.Type.SANDY.toString(), BiomeGenBase.taiga.biomeName},
								"IDs or Biome Types (e.g. SANDY,OCEAN) of biomes that should have increased oil generation rates."));

		setBiomeList(
				excessiveOilBiomeIDs,
				BuildCraftCore.mainConfiguration
				.get(Configuration.CATEGORY_GENERAL,
								"oil.excessiveBiomeIDs",
								new String[] {},
								"IDs or Biome Types (e.g. SANDY,OCEAN) of biomes that should have GREATLY increased oil generation rates."));

		setBiomeList(excludeOilBiomeIDs, BuildCraftCore.mainConfiguration
				.get(Configuration.CATEGORY_GENERAL, "oil.excludeBiomeIDs",
						new String[] {BiomeGenBase.sky.biomeName, BiomeGenBase.hell.biomeName},
						"IDs or Biome Types (e.g. SANDY,OCEAN) of biomes that are excluded from generating oil."));

		double fuelOilMultiplier = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "fuel.oil.combustion", 1.0F, "adjust energy value of Oil in Combustion Engines").getDouble(1.0F);
		double fuelFuelMultiplier = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "fuel.fuel.combustion", 1.0F, "adjust energy value of Fuel in Combustion Engines").getDouble(1.0F);
		BuildCraftCore.mainConfiguration.save();

		if (oilDesertBiomeId > 0) {
			if (BiomeGenBase.getBiomeGenArray()[oilDesertBiomeId] != null) {
				oilDesertBiomeId = findUnusedBiomeID("oilDesert");
				// save changes to config file
				BuildCraftCore.mainConfiguration.get("biomes", "biomeOilDesert", oilDesertBiomeId).set(oilDesertBiomeId);
				BuildCraftCore.mainConfiguration.save();
			}
			biomeOilDesert = BiomeGenOilDesert.makeBiome(oilDesertBiomeId);
		}

		if (oilOceanBiomeId > 0) {
			if (BiomeGenBase.getBiomeGenArray()[oilOceanBiomeId] != null) {
				oilOceanBiomeId = findUnusedBiomeID("oilOcean");
				// save changes to config file
				BuildCraftCore.mainConfiguration.get("biomes", "biomeOilOcean", oilOceanBiomeId).set(oilOceanBiomeId);
				BuildCraftCore.mainConfiguration.save();
			}
			biomeOilOcean = BiomeGenOilOcean.makeBiome(oilOceanBiomeId);
		}

		engineBlock = new BlockEngine();
		CoreProxy.proxy.registerBlock(engineBlock, ItemEngine.class);

		if (BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "energyConverter", true,
				"Set true for enable energy converter").getBoolean(true)) {
			blockEnergyConverter = new BlockEnergyConverter();
			CoreProxy.proxy.registerBlock(blockEnergyConverter, ItemEnergyConverter.class);
			CoreProxy.proxy.registerTileEntity(TileEnergyConverter.class, "EnergyConverter");
		}

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

		// Buckets

		if (blockOil != null) {
			bucketOil = new ItemBucketBuildcraft(blockOil);
			bucketOil.setUnlocalizedName("bucketOil").setContainerItem(Items.bucket);
			CoreProxy.proxy.registerItem(bucketOil);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("oil", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketOil), new ItemStack(Items.bucket));
		}

		if (blockFuel != null) {
			bucketFuel = new ItemBucketBuildcraft(blockFuel);
			bucketFuel.setUnlocalizedName("bucketFuel").setContainerItem(Items.bucket);
			CoreProxy.proxy.registerItem(bucketFuel);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("fuel", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketFuel), new ItemStack(Items.bucket));
		}

		if (!BuildCraftCore.NEXTGEN_PREALPHA) {
			if (blockRedPlasma != null) {
				bucketRedPlasma = new ItemBucketBuildcraft(blockRedPlasma);
				bucketRedPlasma.setUnlocalizedName("bucketRedPlasma").setContainerItem(Items.bucket);
				CoreProxy.proxy.registerItem(bucketRedPlasma);
				FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("redplasma", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketRedPlasma), new ItemStack(Items.bucket));
			}
		}

		// BucketHandler ensures empty buckets fill with the correct liquid.
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

		// Receiver / emitter

		if (!BuildCraftCore.NEXTGEN_PREALPHA) {
			emitterBlock = new BlockEnergyEmitter();
			CoreProxy.proxy.registerBlock(emitterBlock.setBlockName("energyEmitterBlock"));
			CoreProxy.proxy.registerTileEntity(TileEnergyEmitter.class, "net.minecraft.src.builders.TileEnergyEmitter");

			receiverBlock = new BlockEnergyReceiver();
			CoreProxy.proxy.registerBlock(receiverBlock.setBlockName("energyReceiverBlock"));
			CoreProxy.proxy.registerTileEntity(TileEnergyReceiver.class, "net.minecraft.src.builders.TileEnergyReceiver");
		}

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setBiomeList(Set<Integer> list, Property configuration) {
		for (String id : configuration.getStringList()) {
			String strippedId = JavaTools.stripSurroundingQuotes(id.trim());

			if (strippedId.length() > 0) {
				if (strippedId.matches("-?\\d+(\\.\\d+)?")) {
					try {
						list.add(Integer.parseInt(strippedId));
					} catch (NumberFormatException ex) {
						BCLog.logger.log
								(Level.WARNING,
										configuration.getName() + ": Could not find biome id: "
								+ strippedId + " ; Skipping!");
					}
				} else {
					boolean found = false;
					String biomeName = strippedId.toUpperCase();

					for (BiomeDictionary.Type t : BiomeDictionary.Type.values()) {
						String biomeType = t.name().toUpperCase();

						for (BiomeGenBase b : BiomeDictionary.getBiomesForType(t)) {
							if (b.biomeName.toUpperCase().equals(biomeName)
									|| biomeType.toUpperCase().equals(biomeName)) {
								list.add(b.biomeID);
								found = true;
							}
						}
					}


					if (!found) {
						BCLog.logger.log
								(Level.WARNING,
									 configuration.getName() + ": Could not find biome id: "
								+ strippedId + " ; Skipping!");
					}
				}
			}
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-ENERGY", new BuildCraftChannelHandler(),  new PacketHandlerTransport());

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

		SchematicRegistry.registerSchematicBlock(engineBlock, SchematicEngine.class);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
		EnergyProxy.proxy.registerBlockRenderers();
		EnergyProxy.proxy.registerTileEntities();
	}

	@Mod.EventHandler
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
		}
	}

	public static void loadRecipes() {
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 0),
				"www", " g ", "GpG", 'w', "plankWood", 'g', Blocks.glass, 'G',
				BuildCraftCore.woodenGearItem, 'p', Blocks.piston);
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 1), "www", " g ", "GpG", 'w', "cobblestone",
				'g', Blocks.glass, 'G', BuildCraftCore.stoneGearItem, 'p', Blocks.piston);
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 2), "www", " g ", "GpG", 'w', Items.iron_ingot,
				'g', Blocks.glass, 'G', BuildCraftCore.ironGearItem, 'p', Blocks.piston);

		if (blockEnergyConverter != null) {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(blockEnergyConverter, 1), "rcr", "sgs", "rcr",
					'r', Items.redstone, 'c', BuildCraftTransport.pipePowerCobblestone,
					's', BuildCraftTransport.pipePowerStone, 'g', Blocks.glass);
		}
	}

	private int findUnusedBiomeID(String biomeName) {
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
				super(String.format("You have run out of free Biome ID spaces for %s", biome));
			}
		}

		throw new BiomeIdLimitException(biomeName);
	}

	@Mod.EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@Mod.EventHandler
	public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileEngine.class.getCanonicalName());
	}
}

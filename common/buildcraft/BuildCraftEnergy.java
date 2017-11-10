/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.util.Set;

import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.world.biome.BiomeGenBase;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.JavaTools;
import buildcraft.api.core.StackKey;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.core.BCRegistry;
import buildcraft.core.BlockSpring;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.config.ConfigManager.RestartRequirement;
import buildcraft.core.lib.block.BlockBuildCraftFluid;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.lib.engines.TileEngineBase.EnergyStage;
import buildcraft.core.lib.network.ChannelHandler;
import buildcraft.core.lib.network.PacketHandler;
import buildcraft.energy.BucketHandler;
import buildcraft.energy.EnergyGuiHandler;
import buildcraft.energy.EnergyProxy;
import buildcraft.energy.IMCHandlerEnergy;
import buildcraft.energy.ItemBucketBuildcraft;
import buildcraft.energy.TileEngineCreative;
import buildcraft.energy.TileEngineIron;
import buildcraft.energy.TileEngineStone;
import buildcraft.energy.fuels.CoolantManager;
import buildcraft.energy.fuels.FuelManager;
import buildcraft.energy.statements.EnergyStatementProvider;
import buildcraft.energy.statements.TriggerCoolantBelowThreshold;
import buildcraft.energy.statements.TriggerEngineHeat;
import buildcraft.energy.statements.TriggerFuelBelowThreshold;
import buildcraft.energy.worldgen.BiomeGenOilDesert;
import buildcraft.energy.worldgen.BiomeGenOilOcean;
import buildcraft.energy.worldgen.BiomeInitializer;
import buildcraft.energy.worldgen.OilPopulate;

@Mod(name = "BuildCraft Energy", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Energy", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftEnergy extends BuildCraftMod {

	@Mod.Instance("BuildCraft|Energy")
	public static BuildCraftEnergy instance;

	public static boolean spawnOilSprings = true;
	public static BiomeGenOilDesert biomeOilDesert;
	public static BiomeGenOilOcean biomeOilOcean;
	public static Fluid fluidOil;
	public static Fluid fluidFuel;
	public static Fluid fluidRedPlasma;
	public static Block blockOil;
	public static Block blockFuel;
	public static Block blockRedPlasma;
	public static Item bucketOil;
	public static Item bucketFuel;
	public static Item bucketRedPlasma;
	public static Item fuel;

	public static Achievement engineAchievement2;
	public static Achievement engineAchievement3;

	public static boolean canOilBurn;
	public static boolean isOilDense;
	public static double oilWellScalar = 1.0;

	public static ITriggerExternal triggerBlueEngineHeat = new TriggerEngineHeat(EnergyStage.BLUE);
	public static ITriggerExternal triggerGreenEngineHeat = new TriggerEngineHeat(EnergyStage.GREEN);
	public static ITriggerExternal triggerYellowEngineHeat = new TriggerEngineHeat(EnergyStage.YELLOW);
	public static ITriggerExternal triggerRedEngineHeat = new TriggerEngineHeat(EnergyStage.RED);
	public static ITriggerExternal triggerEngineOverheat = new TriggerEngineHeat(EnergyStage.OVERHEAT);

	public static ITriggerExternal triggerFuelBelow25 = new TriggerFuelBelowThreshold(0.25F);
	public static ITriggerExternal triggerFuelBelow50 = new TriggerFuelBelowThreshold(0.50F);

	public static ITriggerExternal triggerCoolantBelow25 = new TriggerCoolantBelowThreshold(0.25F);
	public static ITriggerExternal triggerCoolantBelow50 = new TriggerCoolantBelowThreshold(0.50F);

	private static Fluid buildcraftFluidOil;
	private static Fluid buildcraftFluidFuel;
	private static Fluid buildcraftFluidRedPlasma;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		BuildcraftFuelRegistry.fuel = FuelManager.INSTANCE;
		BuildcraftFuelRegistry.coolant = CoolantManager.INSTANCE;

		int oilDesertBiomeId = BuildCraftCore.mainConfigManager.register("worldgen.biomes",
				"biomeOilDesert", DefaultProps.BIOME_OIL_DESERT, "The id for the Oil Desert biome",
				RestartRequirement.GAME).getInt();
		int oilOceanBiomeId = BuildCraftCore.mainConfigManager.register("worldgen.biomes",
				"biomeOilOcean", DefaultProps.BIOME_OIL_OCEAN, "The id for the Oil Ocean biome",
				RestartRequirement.GAME).getInt();

		BuildCraftCore.mainConfigManager.register("worldgen.spawnOilSprings", true, "Should I spawn oil springs?", ConfigManager.RestartRequirement.GAME);
		BuildCraftCore.mainConfigManager.register("worldgen.oilWellGenerationRate", 1.0D, "How high should be the probability of an oil well generating?", ConfigManager.RestartRequirement.NONE);

		setBiomeList(
				OilPopulate.INSTANCE.surfaceDepositBiomes,
				BuildCraftCore.mainConfigManager
						.register("worldgen.biomes", "increasedOilIDs",
								new String[]{BiomeDictionary.Type.SANDY.toString(), BiomeGenBase.taiga.biomeName},
								"IDs or Biome Types (e.g. SANDY,OCEAN) of biomes that should have increased oil generation rates.", RestartRequirement.GAME));

		setBiomeList(
				OilPopulate.INSTANCE.excessiveBiomes,
				BuildCraftCore.mainConfigManager
						.register("worldgen.biomes", "excessiveOilIDs",
								new String[]{},
								"IDs or Biome Types (e.g. SANDY,OCEAN) of biomes that should have GREATLY increased oil generation rates.", RestartRequirement.GAME));

		setBiomeList(OilPopulate.INSTANCE.excludedBiomes,
				BuildCraftCore.mainConfigManager
						.register("worldgen.biomes", "excludeOilIDs",
								new String[]{BiomeGenBase.sky.biomeName, BiomeGenBase.hell.biomeName},
								"IDs or Biome Types (e.g. SANDY,OCEAN) of biomes that are excluded from generating oil.", RestartRequirement.GAME));

		double fuelOilMultiplier = BuildCraftCore.mainConfigManager.register("general", "fuel.oil.combustion", 1.0F, "adjust energy value of Oil in Combustion Engines", RestartRequirement.GAME).getDouble();
		double fuelFuelMultiplier = BuildCraftCore.mainConfigManager.register("general", "fuel.fuel.combustion", 1.0F, "adjust energy value of Fuel in Combustion Engines", RestartRequirement.GAME).getDouble();

		int fuelOilEnergyOutput = BuildCraftCore.mainConfigManager.register("general", "fuel.oil.combustion.energyOutput", 30, "adjust output energy by Oil in Combustion Engines", RestartRequirement.GAME).getInt();
		int fuelFuelEnergyOutput = BuildCraftCore.mainConfigManager.register("general", "fuel.fuel.combustion.energyOutput", 60, "adjust output energy by Fuel in Combustion Engines", RestartRequirement.GAME).getInt();

		BuildCraftCore.mainConfiguration.save();

		BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();

		if (oilDesertBiomeId > 0) {
			if (oilDesertBiomeId >= biomeGenArray.length || biomeGenArray[oilDesertBiomeId] != null) {
				oilDesertBiomeId = findUnusedBiomeID("oilDesert");
				// save changes to config file
				BuildCraftCore.mainConfiguration.get("worldgen.biomes", "biomeOilDesert", oilDesertBiomeId).set(oilDesertBiomeId);
				BuildCraftCore.mainConfiguration.save();
			}
			biomeOilDesert = BiomeGenOilDesert.makeBiome(oilDesertBiomeId);
		}

		if (oilOceanBiomeId > 0) {
			if (oilOceanBiomeId >= biomeGenArray.length || biomeGenArray[oilOceanBiomeId] != null) {
				oilOceanBiomeId = findUnusedBiomeID("oilOcean");
				// save changes to config file
				BuildCraftCore.mainConfiguration.get("worldgen.biomes", "biomeOilOcean", oilOceanBiomeId).set(oilOceanBiomeId);
				BuildCraftCore.mainConfiguration.save();
			}
			biomeOilOcean = BiomeGenOilOcean.makeBiome(oilOceanBiomeId);
		}

		// Oil and fuel
		if (!FluidRegistry.isFluidRegistered("oil")) {
			buildcraftFluidOil = new Fluid("oil").setDensity(800).setViscosity(10000);
			FluidRegistry.registerFluid(buildcraftFluidOil);
		} else {
			BCLog.logger.warn("Not using BuildCraft oil - issues might occur!");
		}
		fluidOil = FluidRegistry.getFluid("oil");

		if (!FluidRegistry.isFluidRegistered("fuel")) {
			buildcraftFluidFuel = new Fluid("fuel");
			FluidRegistry.registerFluid(buildcraftFluidFuel);
		} else {
			BCLog.logger.warn("Not using BuildCraft fuel - issues might occur!");
		}
		fluidFuel = FluidRegistry.getFluid("fuel");

		if (!FluidRegistry.isFluidRegistered("redplasma")) {
			buildcraftFluidRedPlasma = new Fluid("redplasma").setDensity(10000).setViscosity(10000).setLuminosity(30);
			FluidRegistry.registerFluid(buildcraftFluidRedPlasma);
		} else {
			BCLog.logger.warn("Not using BuildCraft red plasma - issues might occur!");
		}
		fluidRedPlasma = FluidRegistry.getFluid("redplasma");

		if (fluidOil.getBlock() == null) {
			blockOil = new BlockBuildCraftFluid(fluidOil, Material.water, MapColor.blackColor).setFlammability(0);
			blockOil.setBlockName("blockOil").setLightOpacity(8);
			BCRegistry.INSTANCE.registerBlock(blockOil, true);
			fluidOil.setBlock(blockOil);

			BuildCraftCore.mainConfigManager.register("general.oilCanBurn", true, "Should oil burn when lit on fire?", ConfigManager.RestartRequirement.NONE);
			BuildCraftCore.mainConfigManager.register("general.oilIsDense", true, "Should oil be dense and drag entities down?", ConfigManager.RestartRequirement.NONE);
		} else {
			blockOil = fluidOil.getBlock();
		}

		reloadConfig(ConfigManager.RestartRequirement.GAME);

		if (blockOil != null) {
			spawnOilSprings = BuildCraftCore.mainConfigManager.get("worldgen.spawnOilSprings").getBoolean(true);
			BlockSpring.EnumSpring.OIL.canGen = spawnOilSprings;
			BlockSpring.EnumSpring.OIL.liquidBlock = blockOil;
		}

		if (fluidFuel.getBlock() == null) {
			blockFuel = new BlockBuildCraftFluid(fluidFuel, Material.water, MapColor.yellowColor).setFlammable(true).setFlammability(5).setParticleColor(0.7F, 0.7F, 0.0F);
			blockFuel.setBlockName("blockFuel").setLightOpacity(3);
			BCRegistry.INSTANCE.registerBlock(blockFuel, true);
			fluidFuel.setBlock(blockFuel);
		} else {
			blockFuel = fluidFuel.getBlock();
		}

		if (fluidRedPlasma.getBlock() == null) {
			blockRedPlasma = new BlockBuildCraftFluid(fluidRedPlasma, Material.water, MapColor.redColor).setFlammable(
					false).setParticleColor(0.9F, 0, 0);
			blockRedPlasma.setBlockName("blockRedPlasma");
			BCRegistry.INSTANCE.registerBlock(blockRedPlasma, true);
			fluidRedPlasma.setBlock(blockRedPlasma);
		} else {
			blockRedPlasma = fluidRedPlasma.getBlock();
		}

		// Buckets

		if (blockOil != null) {
			bucketOil = new ItemBucketBuildcraft(blockOil);
			bucketOil.setUnlocalizedName("bucketOil").setContainerItem(Items.bucket);
			BCRegistry.INSTANCE.registerItem(bucketOil, true);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("oil", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketOil), new ItemStack(Items.bucket));
		}

		if (blockFuel != null) {
			bucketFuel = new ItemBucketBuildcraft(blockFuel);
			bucketFuel.setUnlocalizedName("bucketFuel").setContainerItem(Items.bucket);
			BCRegistry.INSTANCE.registerItem(bucketFuel, true);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("fuel", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketFuel), new ItemStack(Items.bucket));
		}

		if (BuildCraftCore.DEVELOPER_MODE) {
			if (blockRedPlasma != null) {
				bucketRedPlasma = new ItemBucketBuildcraft(blockRedPlasma);
				bucketRedPlasma.setUnlocalizedName("bucketRedPlasma").setContainerItem(Items.bucket);
				BCRegistry.INSTANCE.registerItem(bucketRedPlasma, true);
				FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("redplasma", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketRedPlasma), new ItemStack(Items.bucket));
			}
		}

		// BucketHandler ensures empty buckets fill with the correct liquid.
		if (blockOil != null) {
			BucketHandler.INSTANCE.buckets.put(blockOil, bucketOil);
		}
		if (blockFuel != null) {
			BucketHandler.INSTANCE.buckets.put(blockFuel, bucketFuel);
		}
		MinecraftForge.EVENT_BUS.register(BucketHandler.INSTANCE);

		BuildcraftRecipeRegistry.refinery.addRecipe("buildcraft:fuel", new FluidStack(fluidOil, 1), new FluidStack(
				fluidFuel, 1), 120, 1);

		BuildcraftFuelRegistry.fuel.addFuel(fluidOil, fuelOilEnergyOutput, (int) (5000 * fuelOilMultiplier));
		BuildcraftFuelRegistry.fuel.addFuel(fluidFuel, fuelFuelEnergyOutput, (int) (25000 * fuelFuelMultiplier));

		BuildcraftFuelRegistry.coolant.addCoolant(FluidRegistry.WATER, 0.0023f);
		BuildcraftFuelRegistry.coolant.addSolidCoolant(StackKey.stack(Blocks.ice), StackKey.fluid(FluidRegistry.WATER), 1.5f);
		BuildcraftFuelRegistry.coolant.addSolidCoolant(StackKey.stack(Blocks.packed_ice), StackKey.fluid(FluidRegistry.WATER), 2.0f);

		BuildCraftCore.engineBlock.registerTile(TileEngineStone.class, 1, "tile.engineStone", "buildcraftenergy:engineStone");
		BuildCraftCore.engineBlock.registerTile(TileEngineIron.class, 2, "tile.engineIron", "buildcraftenergy:engineIron");
		BuildCraftCore.engineBlock.registerTile(TileEngineCreative.class, 3, "tile.engineCreative", "buildcraftenergy:engineCreative");

		InterModComms.registerHandler(new IMCHandlerEnergy());

		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void reloadConfig(ConfigManager.RestartRequirement restartType) {
		if (restartType == ConfigManager.RestartRequirement.GAME) {
			reloadConfig(ConfigManager.RestartRequirement.WORLD);
		} else if (restartType == ConfigManager.RestartRequirement.WORLD) {
			reloadConfig(ConfigManager.RestartRequirement.NONE);
		} else {
			oilWellScalar = BuildCraftCore.mainConfigManager.get("worldgen.oilWellGenerationRate").getDouble();

			if (blockOil instanceof BlockBuildCraftFluid) {
				canOilBurn = BuildCraftCore.mainConfigManager.get("general.oilCanBurn").getBoolean();
				isOilDense = BuildCraftCore.mainConfigManager.get("general.oilIsDense").getBoolean();
				((BlockBuildCraftFluid) blockOil).setFlammable(canOilBurn).setDense(isOilDense);
			}

			if (BuildCraftCore.mainConfiguration.hasChanged()) {
				BuildCraftCore.mainConfiguration.save();
			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent event) {
		if ("BuildCraft|Core".equals(event.modID)) {
			reloadConfig(event.isWorldRunning ? ConfigManager.RestartRequirement.NONE : ConfigManager.RestartRequirement.WORLD);
		}
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
								(Level.WARN,
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
								(Level.WARN,
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
				(DefaultProps.NET_CHANNEL_NAME + "-ENERGY", new ChannelHandler(), new PacketHandler());

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new EnergyGuiHandler());

		StatementManager.registerTriggerProvider(new EnergyStatementProvider());

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		EnergyProxy.proxy.registerBlockRenderers();
		EnergyProxy.proxy.registerTileEntities();

		engineAchievement2 = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.stirlingEngine", "engineAchievement2", 3, -2, new ItemStack(BuildCraftCore.engineBlock, 1, 1), BuildCraftCore.engineRedstoneAchievement));
		engineAchievement3 = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.combustionEngine", "engineAchievement3", 5, -2, new ItemStack(BuildCraftCore.engineBlock, 1, 2), engineAchievement2));
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
			if (buildcraftFluidOil != null) {
				buildcraftFluidOil.setIcons(blockOil.getBlockTextureFromSide(1), blockOil.getBlockTextureFromSide(2));
			}
			if (buildcraftFluidFuel != null) {
				buildcraftFluidFuel.setIcons(blockFuel.getBlockTextureFromSide(1), blockFuel.getBlockTextureFromSide(2));
			}
			if (buildcraftFluidRedPlasma != null) {
				buildcraftFluidRedPlasma.setIcons(blockRedPlasma.getBlockTextureFromSide(1), blockRedPlasma.getBlockTextureFromSide(2));
			}
		}
	}

	public static void loadRecipes() {
		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(BuildCraftCore.engineBlock, 1, 1),
				"www", " g ", "GpG", 'w', "cobblestone",
				'g', "blockGlass", 'G', "gearStone", 'p', Blocks.piston);
		BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(BuildCraftCore.engineBlock, 1, 2),
				"www", " g ", "GpG", 'w', "ingotIron",
				'g', "blockGlass", 'G', "gearIron", 'p', Blocks.piston);
	}

	private int findUnusedBiomeID(String biomeName) {
		int freeBiomeID;
		// code to find a free biome
		for (int i = 1; i < 256; i++) {
			if (BiomeGenBase.getBiomeGenArray()[i] == null) {
				freeBiomeID = i;
				return freeBiomeID;
			}
		}
		// failed to find any free biome IDs
		class BiomeIdLimitException extends RuntimeException {
			private static final long serialVersionUID = 1L;

			public BiomeIdLimitException(String biome) {
				super(String.format("You have run out of free Biome ID spaces for %s - free more Biome IDs or disable the biome by setting the ID to 0!", biome));
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
				TileEngineBase.class.getCanonicalName());
	}

	@Mod.EventHandler
	public void remap(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping : event.get()) {
			if (mapping.name.equals("BuildCraft|Energy:engineBlock")) {
				if (mapping.type == GameRegistry.Type.BLOCK) {
					mapping.remap(BuildCraftCore.engineBlock);
				} else if (mapping.type == GameRegistry.Type.ITEM) {
					mapping.remap(Item.getItemFromBlock(BuildCraftCore.engineBlock));
				}
			}
		}
	}
}

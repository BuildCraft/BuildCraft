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
import buildcraft.api.recipes.RefineryRecipes;
import buildcraft.core.BlockIndex;
import buildcraft.core.BlockSpring;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.fluids.BCFluid;
import buildcraft.core.network.PacketHandler;
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
import buildcraft.energy.TriggerEngineHeat;
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
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.TreeMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

@Mod(name = "BuildCraft Energy", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Energy", dependencies = DefaultProps.DEPENDENCY_CORE)
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
public class BuildCraftEnergy {

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
	public static double oilWellScalar;
	public static TreeMap<BlockIndex, Integer> saturationStored = new TreeMap<BlockIndex, Integer>();
	public static BCTrigger triggerBlueEngineHeat = new TriggerEngineHeat(DefaultProps.TRIGGER_BLUE_ENGINE_HEAT, EnergyStage.BLUE, "buildcraft.engine.stage.blue");
	public static BCTrigger triggerGreenEngineHeat = new TriggerEngineHeat(DefaultProps.TRIGGER_GREEN_ENGINE_HEAT, EnergyStage.GREEN, "buildcraft.engine.stage.green");
	public static BCTrigger triggerYellowEngineHeat = new TriggerEngineHeat(DefaultProps.TRIGGER_YELLOW_ENGINE_HEAT, EnergyStage.YELLOW, "buildcraft.engine.stage.yellow");
	public static BCTrigger triggerRedEngineHeat = new TriggerEngineHeat(DefaultProps.TRIGGER_RED_ENGINE_HEAT, EnergyStage.RED, "buildcraft.engine.stage.red");
	@Instance("BuildCraft|Energy")
	public static BuildCraftEnergy instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		Property engineId = BuildCraftCore.mainConfiguration.getBlock("engine.id", DefaultProps.ENGINE_ID);

		// Update oil tag
		int defaultOilId = DefaultProps.OIL_ID;
		if (BuildCraftCore.mainConfiguration.hasKey(Configuration.CATEGORY_BLOCK, "oilStill.id")) {
			defaultOilId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_BLOCK, "oilStill.id", defaultOilId).getInt(defaultOilId);
			BuildCraftCore.mainConfiguration.getCategory(Configuration.CATEGORY_BLOCK).remove("oilStill.id");
		}
		int blockOilId = BuildCraftCore.mainConfiguration.getBlock("oil.id", defaultOilId).getInt(defaultOilId);

		int blockFuelId = BuildCraftCore.mainConfiguration.getBlock("fuel.id", DefaultProps.FUEL_ID).getInt(DefaultProps.FUEL_ID);
		int bucketOilId = BuildCraftCore.mainConfiguration.getItem("bucketOil.id", DefaultProps.BUCKET_OIL_ID).getInt(DefaultProps.BUCKET_OIL_ID);
		int bucketFuelId = BuildCraftCore.mainConfiguration.getItem("bucketFuel.id", DefaultProps.BUCKET_FUEL_ID).getInt(DefaultProps.BUCKET_FUEL_ID);
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


		engineBlock = new BlockEngine(engineId.getInt(DefaultProps.ENGINE_ID));
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

		if (fluidOil.getBlockID() == -1) {
			if (blockOilId > 0) {
				blockOil = new BlockBuildcraftFluid(blockOilId, fluidOil, Material.water).setFlammable(canOilBurn).setFlammability(0);
				blockOil.setUnlocalizedName("blockOil");
				CoreProxy.proxy.addName(blockOil, "Oil");
				CoreProxy.proxy.registerBlock(blockOil);
				fluidOil.setBlockID(blockOil);
			}
		} else {
			blockOil = Block.blocksList[fluidOil.getBlockID()];
		}

		if (blockOil != null) {
			Property oilSpringsProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "oilSprings", true);
			spawnOilSprings = oilSpringsProp.getBoolean(true);
			BlockSpring.EnumSpring.OIL.canGen = spawnOilSprings;
			BlockSpring.EnumSpring.OIL.liquidBlock = blockOil;
		}

		if (fluidFuel.getBlockID() == -1) {
			if (blockFuelId > 0) {
				blockFuel = new BlockBuildcraftFluid(blockFuelId, fluidFuel, Material.water).setFlammable(true).setFlammability(5).setParticleColor(0.7F, 0.7F, 0.0F);
				blockFuel.setUnlocalizedName("blockFuel");
				CoreProxy.proxy.addName(blockFuel, "Fuel");
				CoreProxy.proxy.registerBlock(blockFuel);
				fluidFuel.setBlockID(blockFuel);
			}
		} else {
			blockFuel = Block.blocksList[fluidFuel.getBlockID()];
		}

		// Buckets

		if (blockOil != null && bucketOilId > 0) {
			bucketOil = new ItemBucketBuildcraft(bucketOilId, blockOil.blockID);
			bucketOil.setUnlocalizedName("bucketOil").setContainerItem(Item.bucketEmpty);
			LanguageRegistry.addName(bucketOil, "Oil Bucket");
			CoreProxy.proxy.registerItem(bucketOil);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("oil", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketOil), new ItemStack(Item.bucketEmpty));
		}

		if (blockFuel != null && bucketFuelId > 0) {
			bucketFuel = new ItemBucketBuildcraft(bucketFuelId, blockFuel.blockID);
			bucketFuel.setUnlocalizedName("bucketFuel").setContainerItem(Item.bucketEmpty);
			LanguageRegistry.addName(bucketFuel, "Fuel Bucket");
			CoreProxy.proxy.registerItem(bucketFuel);
			FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluidStack("fuel", FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucketFuel), new ItemStack(Item.bucketEmpty));
		}

		BucketHandler.INSTANCE.buckets.put(blockOil, bucketOil);
		BucketHandler.INSTANCE.buckets.put(blockFuel, bucketFuel);
		MinecraftForge.EVENT_BUS.register(BucketHandler.INSTANCE);

		RefineryRecipes.addRecipe(new FluidStack(fluidOil, 1), new FluidStack(fluidFuel, 1), 12, 1);

		// Iron Engine Fuels
//		IronEngineFuel.addFuel("lava", 1, 20000);
		IronEngineFuel.addFuel("oil", 3, (int) (5000 * fuelOilMultiplier));
		IronEngineFuel.addFuel("fuel", 6, (int) (25000 * fuelFuelMultiplier));

		// Iron Engine Coolants
		IronEngineCoolant.addCoolant(FluidRegistry.getFluid("water"), 0.0023F);
		IronEngineCoolant.addCoolant(Block.ice.blockID, 0, FluidRegistry.getFluidStack("water", FluidContainerRegistry.BUCKET_VOLUME * 2));

		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());

		new BptBlockEngine(engineBlock.blockID);

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
				new Object[]{"www", " g ", "GpG", 'w', "plankWood", 'g', Block.glass, 'G',
			BuildCraftCore.woodenGearItem, 'p', Block.pistonBase});
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 1), new Object[]{"www", " g ", "GpG", 'w', "cobblestone",
			'g', Block.glass, 'G', BuildCraftCore.stoneGearItem, 'p', Block.pistonBase});
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 2), new Object[]{"www", " g ", "GpG", 'w', Item.ingotIron,
			'g', Block.glass, 'G', BuildCraftCore.ironGearItem, 'p', Block.pistonBase});
	}

	@EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}
	// public static int createPollution (World world, int i, int j, int k, int
	// saturation) {
	// int remainingSaturation = saturation;
	//
	// if (world.rand.nextFloat() > 0.7) {
	// // Try to place an item on the sides
	//
	// LinkedList<BlockIndex> orientations = new LinkedList<BlockIndex>();
	//
	// for (int id = -1; id <= 1; id += 2) {
	// for (int kd = -1; kd <= 1; kd += 2) {
	// if (canPollute(world, i + id, j, k + kd)) {
	// orientations.add(new BlockIndex(i + id, j, k + kd));
	// }
	// }
	// }
	//
	// if (orientations.size() > 0) {
	// BlockIndex toPollute =
	// orientations.get(world.rand.nextInt(orientations.size()));
	//
	// int x = toPollute.i;
	// int y = toPollute.j;
	// int z = toPollute.k;
	//
	// if (world.getBlockId(x, y, z) == 0) {
	// world.setBlock(x, y, z,
	// BuildCraftEnergy.pollution.blockID,
	// saturation * 16 / 100);
	//
	// saturationStored.put(new BlockIndex(x, y, z), new Integer(
	// saturation));
	// remainingSaturation = 0;
	// } else if (world.getBlockTileEntity(z, y, z) instanceof TilePollution) {
	// remainingSaturation = updateExitingPollution(world, x, y, z, saturation);
	// }
	// }
	// }
	//
	// if (remainingSaturation > 0) {
	// if (world.getBlockId(i, j + 1, k) == 0) {
	// if (j + 1 < 128) {
	// world.setBlock(i, j + 1, k,
	// BuildCraftEnergy.pollution.blockID,
	// saturation * 16 / 100);
	// saturationStored.put(new BlockIndex(i, j + 1, k),
	// new Integer(remainingSaturation));
	// }
	//
	// remainingSaturation = 0;
	// } else if (world.getBlockTileEntity(i, j + 1, k) instanceof
	// TilePollution) {
	// remainingSaturation = updateExitingPollution(world, i, j + 1,
	// k, remainingSaturation);
	// }
	// }
	//
	// if (remainingSaturation == 0) {
	// System.out.println ("EXIT 1");
	// return 0;
	// } else if (remainingSaturation == saturation) {
	// System.out.println ("EXIT 2");
	// return saturation;
	// } else {
	// System.out.println ("EXIT 3");
	// return createPollution (world, i, j, k, remainingSaturation);
	// }
	// }
	//
	// private static int updateExitingPollution (World world, int i, int j, int
	// k, int saturation) {
	// int remainingSaturation = saturation;
	//
	// TilePollution tile = (TilePollution) world.getBlockTileEntity(
	// i, j, k);
	//
	// if (tile.saturation + saturation <= 100) {
	// remainingSaturation = 0;
	// tile.saturation += saturation;
	// } else {
	// remainingSaturation = (tile.saturation + saturation) - 100;
	// tile.saturation += saturation - remainingSaturation;
	// }
	//
	// world.setBlockMetadata(i, j, k, saturation * 16 / 100);
	// world.markBlockNeedsUpdate(i, j, k);
	//
	// return remainingSaturation;
	// }
	//
	// private static boolean canPollute (World world, int i, int j, int k) {
	// if (world.getBlockId(i, j, k) == 0) {
	// return true;
	// } else {
	// TileEntity tile = world.getBlockTileEntity(i, j, k);
	//
	// return (tile instanceof TilePollution && ((TilePollution)
	// tile).saturation < 100);
	// }
	// }
}

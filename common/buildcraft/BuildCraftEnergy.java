/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft;

import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.liquids.LiquidContainerData;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.api.fuels.IronEngineCoolant;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.recipes.RefineryRecipe;
import buildcraft.core.BlockIndex;
import buildcraft.core.BlockSpring;
import buildcraft.core.DefaultProps;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.Version;
import buildcraft.core.network.PacketHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.energy.BlockEngine;
import buildcraft.energy.BlockOilFlowing;
import buildcraft.energy.BlockOilStill;
import buildcraft.energy.BptBlockEngine;
import buildcraft.energy.EnergyProxy;
import buildcraft.energy.Engine.EnergyStage;
import buildcraft.energy.GuiHandler;
import buildcraft.energy.ItemBucketOil;
import buildcraft.energy.ItemEngine;
import buildcraft.energy.OilBucketHandler;
import buildcraft.energy.worldgen.BiomeGenOilDesert;
import buildcraft.energy.worldgen.OilPopulate;
import buildcraft.energy.TriggerEngineHeat;
import buildcraft.energy.worldgen.BiomeGenOilOcean;
import buildcraft.energy.worldgen.BiomeInitializer;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(name = "BuildCraft Energy", version = Version.VERSION_CONSTANT, useMetadata = false, modid = "BuildCraft|Energy", dependencies = DefaultProps.DEPENDENCY_CORE)
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
public class BuildCraftEnergy {

	public final static int ENERGY_REMOVE_BLOCK = 25;
	public final static int ENERGY_EXTRACT_ITEM = 2;
	public static boolean spawnOilSprings = true;
	public static BiomeGenOilDesert biomeOilDesert;
	public static BiomeGenOilOcean biomeOilOcean;
	public static BlockEngine engineBlock;
	public static Block oilMoving;
	public static Block oilStill;
	public static Item bucketOil;
	public static Item bucketFuel;
	public static Item fuel;
	public static LiquidStack oilLiquid;
	public static LiquidStack fuelLiquid;
	public static boolean canOilBurn;
	public static TreeMap<BlockIndex, Integer> saturationStored = new TreeMap<BlockIndex, Integer>();
	public static BCTrigger triggerBlueEngineHeat = new TriggerEngineHeat(DefaultProps.TRIGGER_BLUE_ENGINE_HEAT, EnergyStage.Blue);
	public static BCTrigger triggerGreenEngineHeat = new TriggerEngineHeat(DefaultProps.TRIGGER_GREEN_ENGINE_HEAT, EnergyStage.Green);
	public static BCTrigger triggerYellowEngineHeat = new TriggerEngineHeat(DefaultProps.TRIGGER_YELLOW_ENGINE_HEAT, EnergyStage.Yellow);
	public static BCTrigger triggerRedEngineHeat = new TriggerEngineHeat(DefaultProps.TRIGGER_RED_ENGINE_HEAT, EnergyStage.Red);
	@Instance("BuildCraft|Energy")
	public static BuildCraftEnergy instance;

	@PreInit
	public void preInit(FMLPreInitializationEvent evt) {
		Property engineId = BuildCraftCore.mainConfiguration.getBlock("engine.id", DefaultProps.ENGINE_ID);
		Property oilStillId = BuildCraftCore.mainConfiguration.getBlock("oilStill.id", DefaultProps.OIL_STILL_ID);
		Property oilMovingId = BuildCraftCore.mainConfiguration.getBlock("oilMoving.id", DefaultProps.OIL_MOVING_ID);
		Property bucketOilId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "bucketOil.id", DefaultProps.BUCKET_OIL_ID);
		Property bucketFuelId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "bucketFuel.id", DefaultProps.BUCKET_FUEL_ID);
		Property itemFuelId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "fuel.id", DefaultProps.FUEL_ID);
		Property oilDesertBiomeId = BuildCraftCore.mainConfiguration.get("biomes", "oilDesert", 160);
		Property oilOceanBiomeId = BuildCraftCore.mainConfiguration.get("biomes", "oilOcean", 161);
		canOilBurn = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "burnOil", true, "Can oil burn?").getBoolean(true);
		BuildCraftCore.mainConfiguration.save();

		class BiomeIdException extends RuntimeException {

			public BiomeIdException(String biome, int id) {
				super(String.format("You have a Biome Id conflict at %d for %s", id, biome));
			}
		}

		int oilDesertId = oilDesertBiomeId.getInt();
		if (oilDesertId > 0) {
			if (BiomeGenBase.biomeList[oilDesertId] != null) {
				throw new BiomeIdException("oilDesert", oilDesertId);
			}
			biomeOilDesert = BiomeGenOilDesert.makeBiome(oilDesertId);
		}

		int oilOceanId = oilOceanBiomeId.getInt();
		if (oilOceanId > 0) {
			if (BiomeGenBase.biomeList[oilOceanId] != null) {
				throw new BiomeIdException("oilOcean", oilOceanId);
			}
			biomeOilOcean = BiomeGenOilOcean.makeBiome(oilOceanId);
		}


		engineBlock = new BlockEngine(engineId.getInt(DefaultProps.ENGINE_ID));
		CoreProxy.proxy.registerBlock(engineBlock, ItemEngine.class);

		LanguageRegistry.addName(new ItemStack(engineBlock, 1, 0), "Redstone Engine");
		LanguageRegistry.addName(new ItemStack(engineBlock, 1, 1), "Steam Engine");
		LanguageRegistry.addName(new ItemStack(engineBlock, 1, 2), "Combustion Engine");

		oilStill = (new BlockOilStill(oilStillId.getInt(DefaultProps.OIL_STILL_ID), Material.water)).setUnlocalizedName("oil");
		CoreProxy.proxy.addName(oilStill.setUnlocalizedName("oilStill"), "Oil");
		CoreProxy.proxy.registerBlock(oilStill);
		Property oilSpringsProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "oilSprings", true);
		spawnOilSprings = oilSpringsProp.getBoolean(true);
		BlockSpring.EnumSpring.OIL.canGen = spawnOilSprings;
		BlockSpring.EnumSpring.OIL.liquidBlock = oilStill;

		oilMoving = (new BlockOilFlowing(oilMovingId.getInt(DefaultProps.OIL_MOVING_ID), Material.water)).setUnlocalizedName("oil");
		CoreProxy.proxy.addName(oilMoving.setUnlocalizedName("oilMoving"), "Oil");
		CoreProxy.proxy.registerBlock(oilMoving);

		// Oil and fuel
		if (oilMoving.blockID + 1 != oilStill.blockID) {
			throw new RuntimeException("Oil Still id must be Oil Moving id + 1");
		}

		fuel = new ItemBuildCraft(itemFuelId.getInt(DefaultProps.FUEL_ID)).setUnlocalizedName("fuel");
		LanguageRegistry.addName(fuel, "Fuel");

		MinecraftForge.EVENT_BUS.register(new OilBucketHandler());

		bucketOil = (new ItemBucketOil(bucketOilId.getInt(DefaultProps.BUCKET_OIL_ID))).setUnlocalizedName("bucketOil").setContainerItem(Item.bucketEmpty);
		LanguageRegistry.addName(bucketOil, "Oil Bucket");

		bucketFuel = new ItemBuildCraft(bucketFuelId.getInt()).setUnlocalizedName("bucketFuel").setContainerItem(Item.bucketEmpty);
		bucketFuel.setMaxStackSize(1);
		LanguageRegistry.addName(bucketFuel, "Fuel Bucket");

		oilLiquid = LiquidDictionary.getOrCreateLiquid("Oil", new LiquidStack(oilStill, 1));
		fuelLiquid = LiquidDictionary.getOrCreateLiquid("Fuel", new LiquidStack(fuel, 1));

		RefineryRecipe.registerRefineryRecipe(new RefineryRecipe(LiquidDictionary.getLiquid("Oil", 1), null, LiquidDictionary.getLiquid("Fuel", 1), 12, 1));

		// Iron Engine Fuels
		IronEngineFuel.fuels.add(new IronEngineFuel(Block.lavaStill.blockID, 1, 20000));
		IronEngineFuel.fuels.add(new IronEngineFuel(LiquidDictionary.getLiquid("Oil", LiquidContainerRegistry.BUCKET_VOLUME), 3, 20000));
		IronEngineFuel.fuels.add(new IronEngineFuel(LiquidDictionary.getLiquid("Fuel", LiquidContainerRegistry.BUCKET_VOLUME), 6, 100000));

		// Iron Engine Coolants
		IronEngineCoolant.coolants.add(new IronEngineCoolant(new LiquidStack(Block.waterStill, LiquidContainerRegistry.BUCKET_VOLUME), 1.0f));

		LiquidContainerRegistry.registerLiquid(new LiquidContainerData(LiquidDictionary.getLiquid("Oil", LiquidContainerRegistry.BUCKET_VOLUME), new ItemStack(
				bucketOil), new ItemStack(Item.bucketEmpty)));
		LiquidContainerRegistry.registerLiquid(new LiquidContainerData(LiquidDictionary.getLiquid("Fuel", LiquidContainerRegistry.BUCKET_VOLUME),
				new ItemStack(bucketFuel), new ItemStack(Item.bucketEmpty)));

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Init
	public void init(FMLInitializationEvent evt) {
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());

		new BptBlockEngine(engineBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
		EnergyProxy.proxy.registerBlockRenderers();
		EnergyProxy.proxy.registerTileEntities();
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent evt) {
		if (BuildCraftCore.modifyWorld) {
			MinecraftForge.EVENT_BUS.register(OilPopulate.INSTANCE);
			MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeInitializer());
		}
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Post event) {
		if (event.map == Minecraft.getMinecraft().renderEngine.textureMapItems) {
			LiquidDictionary.getCanonicalLiquid("Fuel").setRenderingIcon(fuel.getIconFromDamage(0)).setTextureSheet("/gui/items.png");
		} else {
			LiquidDictionary.getCanonicalLiquid("Oil").setRenderingIcon(oilStill.getBlockTextureFromSide(1)).setTextureSheet("/terrain.png");
		}
	}

	public static void loadRecipes() {
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 0),
				new Object[]{"www", " g ", "GpG", Character.valueOf('w'), "plankWood", Character.valueOf('g'), Block.glass, Character.valueOf('G'),
			BuildCraftCore.woodenGearItem, Character.valueOf('p'), Block.pistonBase});
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 1), new Object[]{"www", " g ", "GpG", Character.valueOf('w'), Block.cobblestone,
			Character.valueOf('g'), Block.glass, Character.valueOf('G'), BuildCraftCore.stoneGearItem, Character.valueOf('p'), Block.pistonBase});
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 2), new Object[]{"www", " g ", "GpG", Character.valueOf('w'), Item.ingotIron,
			Character.valueOf('g'), Block.glass, Character.valueOf('G'), BuildCraftCore.ironGearItem, Character.valueOf('p'), Block.pistonBase});
	}

	@Mod.IMCCallback
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		for (FMLInterModComms.IMCMessage m : event.getMessages()) {
			if (m.key.equals("oil-lake-biome")) {
				try {
					String biomeID = m.getStringValue().trim();
					int id = Integer.valueOf(biomeID);
					if (id >= BiomeGenBase.biomeList.length) {
						throw new IllegalArgumentException("Biome ID must be less than " + BiomeGenBase.biomeList.length);
					}
					OilPopulate.INSTANCE.surfaceDepositBiomes.add(id);
				} catch (Exception ex) {
					Logger.getLogger("Buildcraft").log(Level.WARNING,
							String.format("Received an invalid oil-lake-biome request %s from mod %s", m.getStringValue(), m.getSender()));
				}
				Logger.getLogger("Buildcraft").log(Level.INFO,
						String.format("Received an successfull oil-lake-biome request %s from mod %s", m.getStringValue(), m.getSender()));
			} else if (m.key.equals("oil-gen-exclude")) {
				try {
					String biomeID = m.getStringValue().trim();
					int id = Integer.valueOf(biomeID);
					if (id >= BiomeGenBase.biomeList.length) {
						throw new IllegalArgumentException("Biome ID must be less than " + BiomeGenBase.biomeList.length);
					}
					OilPopulate.INSTANCE.excludedBiomes.add(id);
				} catch (Exception ex) {
					Logger.getLogger("Buildcraft").log(Level.WARNING,
							String.format("Received an invalid oil-gen-exclude request %s from mod %s", m.getStringValue(), m.getSender()));
				}
				Logger.getLogger("Buildcraft").log(Level.INFO,
						String.format("Received an successfull oil-gen-exclude request %s from mod %s", m.getStringValue(), m.getSender()));
			}
		}
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

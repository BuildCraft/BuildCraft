/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft;

import java.util.Random;
import java.util.TreeMap;

import cpw.mods.fml.common.network.NetworkRegistry;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.fuels.IronEngineCoolant;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.gates.Trigger;
import buildcraft.api.liquids.LiquidData;
import buildcraft.api.liquids.LiquidManager;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.recipes.RefineryRecipe;
import buildcraft.core.BlockIndex;
import buildcraft.core.DefaultProps;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.ProxyCore;
import buildcraft.energy.BlockEngine;
import buildcraft.energy.BlockOilFlowing;
import buildcraft.energy.BlockOilStill;
import buildcraft.energy.BptBlockEngine;
import buildcraft.energy.GuiHandler;
import buildcraft.energy.ItemBucketOil;
import buildcraft.energy.ItemEngine;
import buildcraft.energy.OilBucketHandler;
import buildcraft.energy.OilPopulate;
import buildcraft.energy.TriggerEngineHeat;
import buildcraft.energy.Engine.EnergyStage;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;

public class BuildCraftEnergy {

	public final static int ENERGY_REMOVE_BLOCK = 25;
	public final static int ENERGY_EXTRACT_ITEM = 2;

	public static BlockEngine engineBlock;

	public static Block oilMoving;
	public static Block oilStill;
	public static Item bucketOil;
	public static Item bucketFuel;

	public static Item fuel;

	public static TreeMap<BlockIndex, Integer> saturationStored = new TreeMap<BlockIndex, Integer>();

	private static boolean initialized = false;

	public static Trigger triggerBlueEngineHeat = new TriggerEngineHeat(DefaultProps.TRIGGER_BLUE_ENGINE_HEAT, EnergyStage.Blue);
	public static Trigger triggerGreenEngineHeat = new TriggerEngineHeat(DefaultProps.TRIGGER_GREEN_ENGINE_HEAT,
			EnergyStage.Green);
	public static Trigger triggerYellowEngineHeat = new TriggerEngineHeat(DefaultProps.TRIGGER_YELLOW_ENGINE_HEAT,
			EnergyStage.Yellow);
	public static Trigger triggerRedEngineHeat = new TriggerEngineHeat(DefaultProps.TRIGGER_RED_ENGINE_HEAT, EnergyStage.Red);

	public static void load() {
		// Register gui handler
		NetworkRegistry.instance().registerGuiHandler(mod_BuildCraftBuilders.instance, new GuiHandler());

		// MinecraftForge.registerEntity(EntityMechanicalArm.class,
		// mod_BuildCraftEnergy.instance, EntityIds.MECHANICAL_ARM, 50, 10,
		// true);
	}

	public static void initialize() {
		if (initialized)
			return;
		else
			initialized = true;

		BuildCraftCore.initialize();

		Property engineId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("engine.id", DefaultProps.ENGINE_ID);
		Property oilStillId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("oilStill.id",
				DefaultProps.OIL_STILL_ID);
		Property oilMovingId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("oilMoving.id",
				DefaultProps.OIL_MOVING_ID);
		Property bucketOilId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("bucketOil.id",
				Configuration.CATEGORY_ITEM, DefaultProps.BUCKET_OIL_ID);
		Property bucketFuelId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("bucketFuel.id",
				Configuration.CATEGORY_ITEM, DefaultProps.BUCKET_FUEL_ID);
		Property itemFuelId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("fuel.id", Configuration.CATEGORY_ITEM,
				DefaultProps.FUEL_ID);

		BuildCraftCore.mainConfiguration.save();

		engineBlock = new BlockEngine(Integer.parseInt(engineId.value));

		Item.itemsList[engineBlock.blockID] = null;
		Item.itemsList[engineBlock.blockID] = (new ItemEngine(engineBlock.blockID - 256));

		ProxyCore.proxy.addName(new ItemStack(engineBlock, 1, 0), "Redstone Engine");
		ProxyCore.proxy.addName(new ItemStack(engineBlock, 1, 1), "Steam Engine");
		ProxyCore.proxy.addName(new ItemStack(engineBlock, 1, 2), "Combustion Engine");

		oilMoving = (new BlockOilFlowing(Integer.parseInt(oilMovingId.value), Material.water)).setBlockName("oil");
		ProxyCore.proxy.addName(oilMoving.setBlockName("oilMoving"), "Oil");
		ProxyCore.proxy.registerBlock(oilMoving);

		oilStill = (new BlockOilStill(Integer.parseInt(oilStillId.value), Material.water)).setBlockName("oil");
		ProxyCore.proxy.addName(oilStill.setBlockName("oilStill"), "Oil");
		ProxyCore.proxy.registerBlock(oilStill);

		// Oil and fuel
		if (oilMoving.blockID + 1 != oilStill.blockID)
			throw new RuntimeException("Oil Still id must be Oil Moving id + 1");

		MinecraftForge.EVENT_BUS.register(new OilBucketHandler());

		bucketOil = (new ItemBucketOil(Integer.parseInt(bucketOilId.value))).setItemName("bucketOil").setContainerItem(
				Item.bucketEmpty);
		ProxyCore.proxy.addName(bucketOil, "Oil Bucket");

		fuel = new ItemBuildCraft(Integer.parseInt(itemFuelId.value)).setItemName("fuel");
		ProxyCore.proxy.addName(fuel, "Fuel");

		bucketFuel = new ItemBuildCraft(Integer.parseInt(bucketFuelId.value)).setIconIndex(0 * 16 + 3).setItemName("bucketFuel")
				.setMaxStackSize(1).setContainerItem(Item.bucketEmpty);
		ProxyCore.proxy.addName(bucketFuel, "Fuel Bucket");

		RefineryRecipe.registerRefineryRecipe(new RefineryRecipe(new LiquidStack(oilStill.blockID, 1, 0), null, new LiquidStack(fuel.shiftedIndex, 1, 0), 10, 1));

		// Iron Engine Fuels
		IronEngineFuel.fuels.add(new IronEngineFuel(Block.lavaStill.blockID, 1, 20000));
		IronEngineFuel.fuels.add(new IronEngineFuel(oilStill.blockID, 2, 10000));
		IronEngineFuel.fuels.add(new IronEngineFuel(fuel.shiftedIndex, 5, 50000));

		// Iron Engine Coolants
		IronEngineCoolant.coolants.add(new IronEngineCoolant(new LiquidStack(Block.waterStill, BuildCraftAPI.BUCKET_VOLUME), 1.0f));
		
		LiquidManager.liquids.add(new LiquidData(new LiquidStack(oilStill, BuildCraftAPI.BUCKET_VOLUME), new LiquidStack(oilMoving, BuildCraftAPI.BUCKET_VOLUME), new ItemStack(bucketOil), new ItemStack(Item.bucketEmpty)));
		LiquidManager.liquids.add(new LiquidData(new LiquidStack(fuel, BuildCraftAPI.BUCKET_VOLUME), new LiquidStack(fuel, BuildCraftAPI.BUCKET_VOLUME), new ItemStack(bucketFuel), new ItemStack(Item.bucketEmpty)));

		BuildCraftAPI.softBlocks[oilMoving.blockID] = true;
		BuildCraftAPI.softBlocks[oilStill.blockID] = true;

		new BptBlockEngine(engineBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes)
			loadRecipes();
	}

	public static void loadRecipes() {

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 0), new Object[] { "www", " g ", "GpG", Character.valueOf('w'),
				Block.planks, Character.valueOf('g'), Block.glass, Character.valueOf('G'), BuildCraftCore.woodenGearItem,
				Character.valueOf('p'), Block.pistonBase });
		ProxyCore.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 1), new Object[] { "www", " g ", "GpG", Character.valueOf('w'),
				Block.cobblestone, Character.valueOf('g'), Block.glass, Character.valueOf('G'), BuildCraftCore.stoneGearItem,
				Character.valueOf('p'), Block.pistonBase });
		ProxyCore.proxy.addCraftingRecipe(new ItemStack(engineBlock, 1, 2), new Object[] { "www", " g ", "GpG", Character.valueOf('w'),
				Item.ingotIron, Character.valueOf('g'), Block.glass, Character.valueOf('G'), BuildCraftCore.ironGearItem,
				Character.valueOf('p'), Block.pistonBase });
	}

	public static void generateSurface(World world, Random random, int i, int j) {
		OilPopulate.doPopulate(world, i, j);

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
	// world.setBlockAndMetadataWithNotify(x, y, z,
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
	// world.setBlockAndMetadataWithNotify(i, j + 1, k,
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

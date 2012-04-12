/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src;

import java.util.Random;
import java.util.TreeMap;

import net.minecraft.src.buildcraft.api.API;
import net.minecraft.src.buildcraft.api.IronEngineFuel;
import net.minecraft.src.buildcraft.api.LiquidData;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.ItemBuildCraftTexture;
import net.minecraft.src.buildcraft.energy.BlockEngine;
import net.minecraft.src.buildcraft.energy.BlockOilFlowing;
import net.minecraft.src.buildcraft.energy.BlockOilStill;
import net.minecraft.src.buildcraft.energy.GuiHandler;
import net.minecraft.src.buildcraft.energy.ItemBucketOil;
import net.minecraft.src.buildcraft.energy.ItemEngine;
import net.minecraft.src.buildcraft.energy.ItemFuel;
import net.minecraft.src.buildcraft.energy.OilBucketHandler;
import net.minecraft.src.buildcraft.energy.OilPopulate;
import net.minecraft.src.buildcraft.factory.RefineryRecipe;
import net.minecraft.src.buildcraft.factory.TileRefinery;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.Property;

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
	
	public static void load() {
		// Register gui handler
		MinecraftForge.setGuiHandler(mod_BuildCraftEnergy.instance, new GuiHandler());
	}
	
	public static void initialize () {
		if (initialized) {
			return;
		} else {
			initialized = true;
		}
		
		BuildCraftCore.initialize();
		
		Property engineId = BuildCraftCore.mainConfiguration
		.getOrCreateBlockIdProperty("engine.id",
				DefaultProps.ENGINE_ID);
		Property oilStillId = BuildCraftCore.mainConfiguration
		.getOrCreateBlockIdProperty("oilStill.id", DefaultProps.OIL_STILL_ID);
		Property oilMovingId = BuildCraftCore.mainConfiguration
		.getOrCreateBlockIdProperty("oilMoving.id", DefaultProps.OIL_MOVING_ID);
		Property bucketOilId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("bucketOil.id",
						Configuration.CATEGORY_ITEM, DefaultProps.BUCKET_OIL_ID);
		Property bucketFuelId = BuildCraftCore.mainConfiguration
		.getOrCreateIntProperty("bucketFuel.id",
				Configuration.CATEGORY_ITEM, DefaultProps.BUCKET_FUEL_ID);
		Property itemFuelId = BuildCraftCore.mainConfiguration
		.getOrCreateIntProperty("fuel.id",
				Configuration.CATEGORY_ITEM, DefaultProps.FUEL_ID);
		
		BuildCraftCore.mainConfiguration.save();
		
		engineBlock = new BlockEngine(Integer.parseInt(engineId.value));
		ModLoader.registerBlock(engineBlock);
		
		Item.itemsList[engineBlock.blockID] = null;
		Item.itemsList[engineBlock.blockID] = (new ItemEngine(
				engineBlock.blockID - 256));
		
		CoreProxy.addName(new ItemStack (engineBlock, 1, 0), "Redstone Engine");
		CoreProxy.addName(new ItemStack (engineBlock, 1, 1), "Steam Engine");
		CoreProxy.addName(new ItemStack (engineBlock, 1, 2), "Combustion Engine");
		        
		oilMoving = (new BlockOilFlowing(Integer.parseInt(oilMovingId.value),
				Material.water)).setHardness(100F).setLightOpacity(3)
				.setBlockName("oil");
		CoreProxy.addName(oilMoving.setBlockName("oilMoving"), "Oil");
		ModLoader.registerBlock(oilMoving);
		
		oilStill = (new BlockOilStill(Integer.parseInt(oilStillId.value),
				Material.water)).setHardness(100F).setLightOpacity(3)
				.setBlockName("oil");
		CoreProxy.addName(oilStill.setBlockName("oilStill"), "Oil");
		ModLoader.registerBlock(oilStill);
		
		if (oilMoving.blockID + 1 != oilStill.blockID) {
			throw new RuntimeException("Oil Still id must be Oil Moving id + 1");
		}
		
        MinecraftForge.registerCustomBucketHandler(new OilBucketHandler());
         
		bucketOil = (new ItemBucketOil(Integer.parseInt(bucketOilId.value)))
				.setItemName("bucketOil").setContainerItem(Item.bucketEmpty);
		CoreProxy.addName(bucketOil, "Oil Bucket");
		
		fuel = new ItemFuel (Integer.parseInt(itemFuelId.value));
		bucketFuel = new ItemBuildCraftTexture(Integer.parseInt(bucketFuelId.value))
				.setIconIndex(0 * 16 + 3).setItemName("bucketFuel")
				.setMaxStackSize(1).setContainerItem(Item.bucketEmpty);
		CoreProxy.addName(bucketFuel, "Fuel Bucket");
		
		TileRefinery.addRecipe(new RefineryRecipe(oilStill.blockID, 1, 0, 0,
				10, fuel.shiftedIndex, 1, 1));

		API.ironEngineFuel.put(Block.lavaStill.blockID, new IronEngineFuel(
				oilStill.blockID, 1, 20000));
		API.ironEngineFuel.put(oilStill.blockID, new IronEngineFuel(
				oilStill.blockID, 2, 10000));
		API.ironEngineFuel.put(fuel.shiftedIndex, new IronEngineFuel(
				fuel.shiftedIndex, 5, 50000));
		
		API.liquids.add(new LiquidData(oilStill.blockID, 
				bucketOil.shiftedIndex));
		API.liquids.add(new LiquidData(fuel.shiftedIndex,
				bucketFuel.shiftedIndex));
		
		API.softBlocks [oilMoving.blockID] = true;
		API.softBlocks [oilStill.blockID] = true;
		
		BuildCraftCore.refineryInput = oilStill.blockID;
		
		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
	}
	
	public static void loadRecipes () {
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		craftingmanager.addRecipe(
				new ItemStack(engineBlock, 1, 0),
				new Object[] { "www", " g ", "GpG", 
					Character.valueOf('w'),	Block.planks,
					Character.valueOf('g'), Block.glass,
					Character.valueOf('G'), BuildCraftCore.woodenGearItem,
					Character.valueOf('p'), Block.pistonBase});
		craftingmanager.addRecipe(
				new ItemStack(engineBlock, 1, 1),
				new Object[] { "www", " g ", "GpG", 
					Character.valueOf('w'),	Block.cobblestone,
					Character.valueOf('g'), Block.glass,
					Character.valueOf('G'), BuildCraftCore.stoneGearItem,
					Character.valueOf('p'), Block.pistonBase});
		craftingmanager.addRecipe(
				new ItemStack(engineBlock, 1, 2),
				new Object[] { "www", " g ", "GpG", 
					Character.valueOf('w'),	Item.ingotIron,
					Character.valueOf('g'), Block.glass,
					Character.valueOf('G'), BuildCraftCore.ironGearItem,
					Character.valueOf('p'), Block.pistonBase});		
	}

	public static void generateSurface(World world, Random random, int i, int j) {
		OilPopulate.doPopulate(world, i, j);
		
	}
	
//	public static int createPollution (World world, int i, int j, int k, int saturation) {
//		int remainingSaturation = saturation;
//		
//		if (world.rand.nextFloat() > 0.7) {
//			// Try to place an item on the sides
//			
//			LinkedList<BlockIndex> orientations = new LinkedList<BlockIndex>();
//			
//			for (int id = -1; id <= 1; id += 2) {
//				for (int kd = -1; kd <= 1; kd += 2) {
//					if (canPollute(world, i + id, j, k + kd)) {
//						orientations.add(new BlockIndex(i + id, j, k + kd));
//					}
//				}
//			}
//			
//			if (orientations.size() > 0) {
//				BlockIndex toPollute = orientations.get(world.rand.nextInt(orientations.size()));
//				
//				int x = toPollute.i;
//				int y = toPollute.j;
//				int z = toPollute.k;
//									
//				if (world.getBlockId(x, y, z) == 0) {			
//					world.setBlockAndMetadataWithNotify(x, y, z,
//							BuildCraftEnergy.pollution.blockID,
//							saturation * 16 / 100);
//					
//					saturationStored.put(new BlockIndex(x, y, z), new Integer(
//							saturation));
//					remainingSaturation = 0;
//				} else if (world.getBlockTileEntity(z, y, z) instanceof TilePollution) {
//					remainingSaturation = updateExitingPollution(world, x, y, z, saturation);
//				}
//			}
//		} 
//		
//		if (remainingSaturation > 0) {
//			if (world.getBlockId(i, j + 1, k) == 0) {				
//				if (j + 1 < 128) {
//					world.setBlockAndMetadataWithNotify(i, j + 1, k,
//							BuildCraftEnergy.pollution.blockID,
//							saturation * 16 / 100);
//					saturationStored.put(new BlockIndex(i, j + 1, k),
//							new Integer(remainingSaturation));					
//				}
//				
//				remainingSaturation = 0;
//			} else if (world.getBlockTileEntity(i, j + 1, k) instanceof TilePollution) {
//				remainingSaturation = updateExitingPollution(world, i, j + 1,
//						k, remainingSaturation);
//			}
//		}
//		
//		if (remainingSaturation == 0) {
//			System.out.println ("EXIT 1");
//			return 0;
//		} else if (remainingSaturation == saturation) {
//			System.out.println ("EXIT 2");
//			return saturation;
//		} else {
//			System.out.println ("EXIT 3");
//			return createPollution (world, i, j, k, remainingSaturation);
//		}
//	}
//
//	private static int updateExitingPollution (World world, int i, int j, int k, int saturation) {
//		int remainingSaturation = saturation;
//		
//		TilePollution tile = (TilePollution) world.getBlockTileEntity(
//				i, j, k);
//		
//		if (tile.saturation + saturation <= 100) {			
//			remainingSaturation = 0;
//			tile.saturation += saturation;
//		} else {
//			remainingSaturation = (tile.saturation + saturation) - 100;
//			tile.saturation += saturation - remainingSaturation;
//		}
//		
//		world.setBlockMetadata(i, j, k, saturation * 16 / 100);		
//		world.markBlockNeedsUpdate(i, j, k);
//		
//		return remainingSaturation;
//	}
//	
//	private static boolean canPollute (World world, int i, int j, int k) {
//		if (world.getBlockId(i, j, k) == 0) {
//			return true;
//		} else {
//			TileEntity tile = world.getBlockTileEntity(i, j, k);
//			
//			return (tile instanceof TilePollution && ((TilePollution) tile).saturation < 100);
//		}
//	}
}

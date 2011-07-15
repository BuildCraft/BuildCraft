package net.minecraft.src;

import java.util.LinkedList;
import java.util.TreeMap;

import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.energy.BlockEngine;
import net.minecraft.src.buildcraft.energy.BlockOilFlowing;
import net.minecraft.src.buildcraft.energy.BlockOilStill;
import net.minecraft.src.buildcraft.energy.BlockPollution;
import net.minecraft.src.buildcraft.energy.ItemEngine;
import net.minecraft.src.buildcraft.energy.OilBucketHandler;
import net.minecraft.src.buildcraft.energy.TilePollution;
import net.minecraft.src.forge.MinecraftForge;

public class BuildCraftEnergy {
	public static BlockEngine engineBlock;
	
	public static Block oilMoving;
	public static Block oilStill;
	public static Item bucketOil;
	
	public static TreeMap<BlockIndex, Integer> saturationStored = new TreeMap<BlockIndex, Integer>();
	
	public static void ModsLoaded () {		
		engineBlock = new BlockEngine(200);
		ModLoader.RegisterBlock(engineBlock);
		
		Item.itemsList[engineBlock.blockID] = (new ItemEngine(
				engineBlock.blockID - 256));
		
		oilMoving = (new BlockOilFlowing(201, Material.water)).setHardness(100F).setLightOpacity(3).setBlockName("oil");
		CoreProxy.addName(oilMoving.setBlockName("oilMoving"), "Oil");
		ModLoader.RegisterBlock(oilMoving);
        
		oilStill = (new BlockOilStill(202, Material.water)).setHardness(100F).setLightOpacity(3).setBlockName("oil");
        CoreProxy.addName(oilStill.setBlockName("oilStill"), "Oil");
        ModLoader.RegisterBlock(oilStill);        
		
        MinecraftForge.registerCustomBucketHander(new OilBucketHandler());
         
		bucketOil = (new ItemBucket(71, oilMoving.blockID))
				.setIconIndex(
						ModLoader
								.addOverride("/gui/items.png",
										"/net/minecraft/src/buildcraft/energy/gui/oil_bucket.png"))
				.setItemName("bucketOil").setContainerItem(Item.bucketEmpty);
		CoreProxy.addName(bucketOil, "Oil Bucket");
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

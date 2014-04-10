package buildcraft.core.utils;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftTransport;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.pipes.PipeFluidsGold;

public class MultiBlockCheck {
	
	
	public static boolean isPartOfAMultiBlock(String type, int x, int y, int z, World world){
		if (type=="refinery"){
			return  north (x, y, z, world) || east (x, y, z, world) || south (x, y, z, world) || west(x, y, z, world);
		}
		return false;
	}
	
	public static boolean north(int x, int y, int z, World world){
		boolean partOfMultiBlock = true;
		for (int teller=-1; teller<=1; teller++){
			for (int teller2 = 0; teller2 <=5; teller2++){
				for (int teller3 = 0; teller3>=-2; teller3--){
					if (teller == 0 && teller3 == -1){
						if (teller2 == 0 || teller2 == 5){
							if (world.getBlock(x+teller, y+teller2, z+teller3) != BuildCraftFactory.blockRefineryValve){
								partOfMultiBlock = false;
							} 
						} else {
							if (!world.isAirBlock(x+teller, y+teller2, z+teller3)){
								partOfMultiBlock = false;
							}
						}
					} else {
						if (teller == 0 && teller2 == 0 && teller3 == 0){
						} else {
							if (world.getBlock(x+teller, y+teller2, z+teller3) != BuildCraftFactory.blockRefineryHeater){
								partOfMultiBlock = false;
							}
						}
					}
				}
			}
		}
		return partOfMultiBlock;
	}
	
	public static boolean east (int x, int y, int z, World world){
		boolean partOfMultiBlock = true;
		for (int teller=0; teller<=2; teller++){
			for (int teller2 = 0; teller2 <=5; teller2++){
				for (int teller3 = -1; teller3<=1; teller3++){
					if (teller == 1 && teller3 == 0){
						if (teller2 == 0 || teller2 == 5){
							if (world.getBlock(x+teller, y+teller2, z+teller3) != BuildCraftFactory.blockRefineryValve){
								partOfMultiBlock = false;
							} 
						} else {
							if (!world.isAirBlock(x+teller, y+teller2, z+teller3)){
								partOfMultiBlock = false;
							}
						}
					} else {
						if (teller == 0 && teller2 == 0 && teller3 == 0){
						} else {
							if (world.getBlock(x+teller, y+teller2, z+teller3) != BuildCraftFactory.blockRefineryHeater){
								partOfMultiBlock = false;
							}
						}
					}
				}
			}
		}
		return partOfMultiBlock;
	}
	
	public static boolean south(int x, int y, int z, World world){
		boolean partOfMultiBlock = true;
		for (int teller=-1; teller<=1; teller++){
			for (int teller2 = 0; teller2 <=5; teller2++){
				for (int teller3 = 0; teller3<=2; teller3++){
					if (teller3 == 1 && teller == 0){
						if (teller2 == 0 || teller2 == 5){
							if (world.getBlock(x+teller, y+teller2, z+teller3) != BuildCraftFactory.blockRefineryValve){
								partOfMultiBlock = false;
							} 
						} else {
							if (!world.isAirBlock(x+teller, y+teller2, z+teller3)){
								partOfMultiBlock = false;
							}
						}
					} else {
						if (teller == 0 && teller2 == 0 && teller3 == 0){
						} else {
							if (world.getBlock(x+teller, y+teller2, z+teller3) != BuildCraftFactory.blockRefineryHeater){
								partOfMultiBlock = false;
							}
						}
					}
				}
			}
		}
		return partOfMultiBlock;
	}
	
	public static boolean west(int x, int y, int z, World world){
		boolean partOfMultiBlock = true;
		for (int teller=0; teller>=-2; teller--){
			for (int teller2 = 0; teller2 <=5; teller2++){
				for (int teller3 = -1; teller3<=1; teller3++){
					if (teller == -1 && teller3 == 0){
						if (teller2 == 0 || teller2 == 5){
							if (world.getBlock(x+teller, y+teller2, z+teller3) != BuildCraftFactory.blockRefineryValve){
								partOfMultiBlock = false;
							} 
						} else {
							if (!world.isAirBlock(x+teller, y+teller2, z+teller3)){
								partOfMultiBlock = false;
							}
						}
					} else {
						if (teller == 0 && teller2 == 0 && teller3 == 0){
						} else {
							if (world.getBlock(x+teller, y+teller2, z+teller3) != BuildCraftFactory.blockRefineryHeater){
								partOfMultiBlock = false;
							}
						}
					}
				}
			}
		}
		return partOfMultiBlock;
	}
}

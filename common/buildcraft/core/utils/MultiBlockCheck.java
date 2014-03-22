package buildcraft.core.utils;

import net.minecraft.item.Item;
import net.minecraft.world.World;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftTransport;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.pipes.PipeFluidsGold;

public class MultiBlockCheck {
	
	
	public static boolean isPartOfAMultiBlock(String type, int x, int y, int z, World world){
		boolean partOfMultiBlock = true;
		for (int teller=-4; teller<=4; teller++){
			for (int teller2 = 0; teller2 <=2; teller2++){
				for (int teller3 = 0; teller3<=2; teller3++){
					if (teller2 == 1 && teller3 == 1){
						if (teller == -4 || teller == 4){
							if (world.getBlock(x+teller, y+teller2, z+teller3)!=BuildCraftFactory.floodGateBlock){
								partOfMultiBlock=false;
							}
						}
					} else {
						if (world.getBlock(x+teller, y+teller2, z+teller3)!=BuildCraftFactory.blockRefineryHeater){
							if (!(x == x+teller && y == y+teller2 && z == z+teller3)){
								partOfMultiBlock=false;	
							}
						}
					}
				}
			}
		}
		return partOfMultiBlock;
	}

}

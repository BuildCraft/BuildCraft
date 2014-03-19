package buildcraft.core.utils;

import net.minecraft.item.Item;
import net.minecraft.world.World;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftTransport;

public class MultiBlockCheck {
	
	
	public static boolean isPartOfAMultiBlock(String type, int x, int y, int z, World world){
		if (!world.isRemote){
			if (type=="refinery"){
				boolean multiBlock = true;
				for (int t = -5; t <= 5; t ++){
					for (int u = -1; u <=1; u++){
						for (int v = 0; u<=2; v++){
							if (world.getBlock(x+t, y+u, z+v)!=BuildCraftFactory.blockRefineryHeater
									&& world.getBlock(x+t, y+u, z+v)!=BuildCraftFactory.blockRefineryControl){
								if (z!=0 && Item.getItemFromBlock(world.getBlock(x+t,y+u,z))!=BuildCraftTransport.pipeFluidsCobblestone){
									multiBlock=false;
								}
								
							}
						}
					}
				}
				return multiBlock;
			}
		}
		return false;
	}

}

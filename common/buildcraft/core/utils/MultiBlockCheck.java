package buildcraft.core.utils;

import net.minecraft.world.World;
import buildcraft.BuildCraftFactory;

public class MultiBlockCheck {
	
	public static boolean isPartOfAMultiBlock(String type, int x, int y, int z, World world){
		if (!world.isRemote){
			if (type=="refinery"){
				if (world.getBlock(x+1, y, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y, z+1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x+1, y, z+1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y+1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x+1, y+1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y+1, z+1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x+1, y+1, z+1)==BuildCraftFactory.blockMultiRefinery){
					return true;
				}
				if (world.getBlock(x-1, y, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y, z+1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x-1, y, z+1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y+1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x-1, y+1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y+1, z+1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x-1, y+1, z+1)==BuildCraftFactory.blockMultiRefinery){
					return true;
				}
				if (world.getBlock(x+1, y, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y, z+1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x+1, y, z+1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y-1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x+1, y-1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y-1, z+1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x+1, y-1, z+1)==BuildCraftFactory.blockMultiRefinery){
					return true;
				}
				if (world.getBlock(x+1, y, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y, z-1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x+1, y, z-1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y+1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x+1, y+1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y+1, z-1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x+1, y+1, z-1)==BuildCraftFactory.blockMultiRefinery){
					return true;
				}
				if (world.getBlock(x-1, y, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y, z+1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x-1, y, z+1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y-1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x-1, y-1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y-1, z+1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x-1, y-1, z+1)==BuildCraftFactory.blockMultiRefinery){
					return true;
				}
				if (world.getBlock(x-1, y, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y, z-1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x-1, y, z-1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y+1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x-1, y+1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y+1, z-1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x-1, y+1, z-1)==BuildCraftFactory.blockMultiRefinery){
					return true;
				}
				if (world.getBlock(x+1, y, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y, z-1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x+1, y, z-1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y-1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x+1, y-1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y-1, z-1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x+1, y-1, z-1)==BuildCraftFactory.blockMultiRefinery){
					return true;
				}
				if (world.getBlock(x-1, y, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y, z-1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x-1, y, z-1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y-1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x-1, y-1, z)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x, y-1, z-1)==BuildCraftFactory.blockMultiRefinery
						&& world.getBlock(x-1, y-1, z-1)==BuildCraftFactory.blockMultiRefinery){
					return true;
				}
			}
		}
		return false;
	}

}

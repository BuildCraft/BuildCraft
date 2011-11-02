/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src;

public class BuildCraftBlockUtil {

    public static int damageDropped(World world, int i, int j, int k) {		
		Block block = Block.blocksList[world.getBlockId(i, j, k)];

		return block.damageDropped(world.getBlockMetadata(i, j, k));
    }
    
    public static ItemStack getItemStackFromBlock(World world, int i, int j, int k)
    {
    	Block block = Block.blocksList[world.getBlockId(i, j, k)];
    
    	if (block == null) {
    		return null;
    	}
    	
    	int meta = world.getBlockMetadata(i, j, k);
    	int id = block.idDropped(meta, world.rand);
    	int qty = block.quantityDropped(world.rand);
    	int dmg = block.damageDropped(meta);
    	
    	if (id <= 0 || qty == 0) {
    		return null;
    	} else {    	
    		return new ItemStack(id, qty, dmg);
    	}
    }
}
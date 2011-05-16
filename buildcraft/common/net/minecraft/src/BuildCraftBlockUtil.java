package net.minecraft.src;

public class BuildCraftBlockUtil extends Block
{

    public BuildCraftBlockUtil(int i, int j)
    {
        super(i, j, Material.rock);
    }

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
    	
    	if (id == 0 || qty == 0) {
    		return null;
    	} else {    	
    		return new ItemStack(id, qty, dmg);
    	}
    }
}
package net.minecraft.src;

public class BuildCraftBlockUtil extends Block
{

    public BuildCraftBlockUtil(int i, int j)
    {
        super(i, j, Material.rock);
    }

    public static int damageDropped(World world, int i, int j, int k) {		
		Block block = Block.blocksList[world.getBlockId(i, j, k)];

		return block.damageDropped(block.blockID);
    }
    
    public static ItemStack getItemStackFromBlock(World world, int i, int j, int k)
    {
    	Block block = Block.blocksList[world.getBlockId(i, j, k)];
    			        
		return new ItemStack(block.idDropped(block.blockID, world.rand),
				block.quantityDropped(world.rand),
				block.damageDropped(block.blockID));
    }
}
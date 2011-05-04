package net.minecraft.src.buildcraft;

import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;

public class BlockUtil extends Block {

	public Block baseBlock = Block.sand;
	
	public BlockUtil(int i) {
		super(i, Material.sand);
		// TODO Auto-generated constructor stub
	}
	
    public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l)
    {
        return baseBlock.getBlockTexture(iblockaccess, i, j, k, l);
    }

    public int getBlockTextureFromSideAndMetadata(int i, int j)
    {
        return baseBlock.getBlockTextureFromSideAndMetadata(i, j);
    }

    public int getBlockTextureFromSide(int i)
    {
        return baseBlock.getBlockTextureFromSide (i);
    }
    
    public boolean isOpaqueCube()
    {
        return false;
    }
    
    public boolean renderAsNormalBlock()
    {
        return false;
    }

}

package net.minecraft.src.buildcraft.factory;

import java.util.Random;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.buildcraft.api.IBlockPipe;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.Utils;

public class BlockPlainPipe extends Block implements IPipeConnection, IBlockPipe {	
	
	public BlockPlainPipe(int i) {
		super(i, Material.glass);
		
		blockIndexInTexture = 16 * 2 + 0;
		
		minX = Utils.pipeMinSize;
		minY = 0.0;
		minZ = Utils.pipeMinSize;
		
		maxX = Utils.pipeMaxSize;
		maxY = 1.0;
		maxZ = Utils.pipeMaxSize;
	}
    
    public boolean isOpaqueCube()
    {
        return false;
    }
    
    public boolean renderAsNormalBlock()
    {
        return false;
    }
    
    public boolean func_28025_b () {
    	return false;
    }    
    
    public int idDropped(int i, Random random)
    {
        return 0;
    }

	@Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x, int y,
			int z) {

		return false;
	}
	
	
    public int getRenderType()
    {
        return BuildCraftCore.customTextureModel;
    }

	@Override
	public int getTextureForConnection(Orientations connection, int metadata) {		
		return blockIndexInTexture;
	}
	
    public float getHeightInPipe () {
    	return 0.4F;
    }    
}

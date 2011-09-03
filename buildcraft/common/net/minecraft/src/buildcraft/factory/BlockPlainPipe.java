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
import net.minecraft.src.forge.ITextureProvider;

public class BlockPlainPipe extends Block implements IPipeConnection,
		IBlockPipe, ITextureProvider {	
	
	public BlockPlainPipe(int i) {
		super(i, Material.glass);
		
		blockIndexInTexture = 16 * 2 + 0;
		
		minX = Utils.pipeMinPos;
		minY = 0.0;
		minZ = Utils.pipeMinPos;
		
		maxX = Utils.pipeMaxPos;
		maxY = 1.0;
		maxZ = Utils.pipeMaxPos;
	}
    
    public boolean isOpaqueCube()
    {
        return false;
    }
    
    public boolean renderAsNormalBlock()
    {
        return false;
    }
    
    public boolean isACube () {
    	return false;
    }    
    
    public int idDropped(int i, Random random)
    {
        return 0;
    }

	@Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2) {

		return false;
	}
    
    @Override
	public String getTextureFile() {	
		return BuildCraftCore.customBuildCraftTexture;
	}
	
    public float getHeightInPipe () {
    	return 0.5F;
    }

	@Override
	public int getTextureForConnection(IBlockAccess blockAccess, int i, int j,
			int k, Orientations connection) {
		return blockIndexInTexture;
	}    
}

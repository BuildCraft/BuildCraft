package net.minecraft.src.buildcraft.factory;

import java.util.Random;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.IBlockPipe;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;

public class BlockFrame extends Block implements IPipeConnection, IBlockPipe, ITextureProvider {	
	
	public BlockFrame(int i) {
		super(i, Material.glass);
		
		blockIndexInTexture = 16 * 2 + 2; 
		
		setHardness(0.5F);
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
        return -1;
    }
    
    public int getRenderType()
    {
        return BuildCraftCore.pipeModel;
    }
    
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k)
    {
   		float xMin = Utils.pipeMinPos, xMax = Utils.pipeMaxPos, 
   		yMin = Utils.pipeMinPos, yMax = Utils.pipeMaxPos, 
   		zMin = Utils.pipeMinPos, zMax = Utils.pipeMaxPos;

   		if (Utils.checkPipesConnections(world, i, j, k, i - 1, j, k)) {
   			xMin = 0.0F;
   		}

   		if (Utils.checkPipesConnections(world, i, j, k, i + 1, j, k)) {
   			xMax = 1.0F;
   		}

   		if (Utils.checkPipesConnections(world, i, j, k, i, j - 1, k)) {
   			yMin = 0.0F;
   		}

   		if (Utils.checkPipesConnections(world, i, j, k, i, j + 1, k)) {
   			yMax = 1.0F;
   		}

   		if (Utils.checkPipesConnections(world, i, j, k, i, j, k - 1)) {
   			zMin = 0.0F;
   		}

   		if (Utils.checkPipesConnections(world, i, j, k, i, j, k + 1)) {
   			zMax = 1.0F;
   		}
       	
       	    
   		return AxisAlignedBB.getBoundingBoxFromPool((double) i + xMin,
   				(double) j + yMin, (double) k + zMin, (double) i + xMax,
   				(double) j + yMax, (double) k + zMax);
       }
   	
       public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int i, int j, int k)
       {
           return getCollisionBoundingBoxFromPool (world, i, j, k);
       }

	@Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2) {
		return blockAccess.getBlockId(x2, y2, z2) == blockID;
	}
	
    public float getHeightInPipe () {
    	return 0.5F;
    }    
    
    @Override
	public String getTextureFile() {	
		return BuildCraftCore.customBuildCraftTexture;
	}

	@Override
	public void prepareTextureFor(IBlockAccess blockAccess, int i, int j,
			int k, Orientations connection) {
		// TODO Auto-generated method stub
		
	}
}

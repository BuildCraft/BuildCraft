package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.IInventory;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IBlockPipe;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.ICustomTextureBlock;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.Utils;

public abstract class BlockPipe extends BlockContainer implements
		IPipeConnection, IBlockPipe, ICustomTextureBlock {
	
	public BlockPipe(int i, Material material) {
		super(i, material);

		setHardness(0.5F);
	}
	
    public int getRenderType()
    {
        return BuildCraftCore.pipeModel;
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
    
	@Override
	protected abstract TileEntity getBlockEntity();
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
		float xMin = Utils.pipeMinSize, xMax = Utils.pipeMaxSize, 
		yMin = Utils.pipeMinSize, yMax = Utils.pipeMaxSize, 
		zMin = Utils.pipeMinSize, zMax = Utils.pipeMaxSize;

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
    
    public boolean isPipeConnected (IBlockAccess blockAccess, int x, int y, int z) {
    	TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x, y, z);
    	
    	return tile instanceof IPipeEntry
			|| tile instanceof IInventory
			|| tile instanceof IMachine;
    }
    
    public void onBlockRemoval(World world, int i, int j, int k) {    	
		Utils.preDestroyBlock(world, i, j, k);
		
    	super.onBlockRemoval(world, i, j, k);
    }
    
    @Override
    public int getTextureForConnection (Orientations connection, int metadata) {
    	return blockIndexInTexture;
    }
    
    public float getHeightInPipe () {
    	return 0.4F;
    }
    
	@Override
	public String getTextureFile() {	
		return BuildCraftCore.customBuildCraftTexture;
	}
}

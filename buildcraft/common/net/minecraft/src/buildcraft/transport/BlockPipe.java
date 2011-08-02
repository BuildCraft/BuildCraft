package net.minecraft.src.buildcraft.transport;

import java.util.ArrayList;

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
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;

public abstract class BlockPipe extends BlockContainer implements
		IPipeConnection, IBlockPipe, ITextureProvider {
	
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
	public void getCollidingBoundingBoxes(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, ArrayList arraylist) {
		setBlockBounds(Utils.pipeMinSize, Utils.pipeMinSize, Utils.pipeMinSize,
				Utils.pipeMaxSize, Utils.pipeMaxSize, Utils.pipeMaxSize);
		super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);
		
		if (Utils.checkPipesConnections(world, i, j, k, i - 1, j, k)) {
			setBlockBounds(0.0F, Utils.pipeMinSize, Utils.pipeMinSize,
					Utils.pipeMaxSize, Utils.pipeMaxSize, Utils.pipeMaxSize);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);	
		}

		if (Utils.checkPipesConnections(world, i, j, k, i + 1, j, k)) {
			setBlockBounds(Utils.pipeMinSize, Utils.pipeMinSize, Utils.pipeMinSize,
					1.0F, Utils.pipeMaxSize, Utils.pipeMaxSize);			
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);	
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j - 1, k)) {
			setBlockBounds(Utils.pipeMinSize, 0.0F, Utils.pipeMinSize,
					Utils.pipeMaxSize, Utils.pipeMaxSize, Utils.pipeMaxSize);			
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);	
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j + 1, k)) {
			setBlockBounds(Utils.pipeMinSize, Utils.pipeMinSize, Utils.pipeMinSize,
					Utils.pipeMaxSize, 1.0F, Utils.pipeMaxSize);			
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);	
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j, k - 1)) {
			setBlockBounds(Utils.pipeMinSize, Utils.pipeMinSize, 0.0F,
					Utils.pipeMaxSize, Utils.pipeMaxSize, Utils.pipeMaxSize);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);	
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j, k + 1)) {
			setBlockBounds(Utils.pipeMinSize, Utils.pipeMinSize, Utils.pipeMinSize,
					Utils.pipeMaxSize, Utils.pipeMaxSize, 1.0F);			
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);	
		}
		
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}	 
	
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
    
    @Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2) {
    	TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x2, y2, z2);
    	
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

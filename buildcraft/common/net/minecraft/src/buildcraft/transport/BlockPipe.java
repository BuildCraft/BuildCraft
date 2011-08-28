package net.minecraft.src.buildcraft.transport;

import java.util.ArrayList;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.IInventory;
import net.minecraft.src.Material;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Vec3D;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.IBlockPipe;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.ILiquidContainer;
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
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public void getCollidingBoundingBoxes(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, ArrayList arraylist) {
		setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos,
				Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
		super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);
		
		if (Utils.checkPipesConnections(world, i, j, k, i - 1, j, k)) {
			setBlockBounds(0.0F, Utils.pipeMinPos, Utils.pipeMinPos,
					Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);	
		}

		if (Utils.checkPipesConnections(world, i, j, k, i + 1, j, k)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos,
					1.0F, Utils.pipeMaxPos, Utils.pipeMaxPos);			
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);	
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j - 1, k)) {
			setBlockBounds(Utils.pipeMinPos, 0.0F, Utils.pipeMinPos,
					Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);			
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);	
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j + 1, k)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos,
					Utils.pipeMaxPos, 1.0F, Utils.pipeMaxPos);			
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);	
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j, k - 1)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, 0.0F,
					Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);	
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j, k + 1)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos,
					Utils.pipeMaxPos, Utils.pipeMaxPos, 1.0F);			
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb, arraylist);	
		}
		
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}	 
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
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
    
	public MovingObjectPosition collisionRayTrace(World world, int i, int j,
			int k, Vec3D vec3d, Vec3D vec3d1) {
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
		
		setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);

		MovingObjectPosition r = super.collisionRayTrace(world, i, j, k, vec3d,
				vec3d1);

		setBlockBounds(0, 0, 0, 1, 1, 1);

		return r;
	}
    
    @Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2) {
    	TileEntity tile = blockAccess.getBlockTileEntity(x2, y2, z2);
    	
    	return tile instanceof IPipeEntry
			|| tile instanceof IInventory
			|| tile instanceof IMachine
			|| tile instanceof ILiquidContainer;
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
    	return 0.5F;
    }
    
	@Override
	public String getTextureFile() {	
		return BuildCraftCore.customBuildCraftTexture;
	}
	
	public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
		((TilePipe) world.getBlockTileEntity(i, j, k)).neighborChange ();
	}
}

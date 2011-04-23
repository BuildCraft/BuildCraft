package net.minecraft.src.buildcraft;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class BlockExtractor extends BlockContainer {
	
	public int texture;
	
	public BlockExtractor(int i) {
		super(i, Material.glass);
		
		texture = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/extractor.png");		
	}
    
    public int getRenderType()
    {
        return mod_BuildCraft.getInstance().pipeModel;
    }
	
    public boolean isOpaqueCube()
    {
        return false;
    }
    
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    public int getBlockTextureFromSide(int i) {
    	return texture;
    }
    
	@Override
	protected TileEntity getBlockEntity() {
		return new TileExtractor ();
	}
	
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k)
    {
		float xMin = Utils.pipeMinSize, xMax = Utils.pipeMaxSize, 
		yMin = Utils.pipeMinSize, yMax = Utils.pipeMaxSize, 
		zMin = Utils.pipeMinSize, zMax = Utils.pipeMaxSize;
		
		if (Utils.isPipeConnected (world.getBlockId(i - 1, j, k))) {
			xMin = 0.0F;
		}

		if (Utils.isPipeConnected (world.getBlockId(i + 1, j, k))) {
			xMax = 1.0F;
		}

		if (Utils.isPipeConnected (world.getBlockId(i, j - 1, k))) {
			yMin = 0.0F;
		}

		if (Utils.isPipeConnected (world.getBlockId(i, j + 1, k))) {
			yMax = 1.0F;
		}

		if (Utils.isPipeConnected (world.getBlockId(i, j, k - 1))) {
			zMin = 0.0F;
		}

		if (Utils.isPipeConnected (world.getBlockId(i, j, k + 1))) {
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
    
    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
    	TileExtractor tile = (TileExtractor) world.getBlockTileEntity(i, j, k);
    	
    	if (tile == null) {
    		tile = new TileExtractor();
    		world.setBlockTileEntity(i, j, k, tile);
    	}
    	
    	tile.extract();
    	
        return false;
    }
    
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
    	TileExtractor tile = (TileExtractor) world.getBlockTileEntity(i, j, k);
    	
    	if (tile == null) {
    		tile = new TileExtractor();
    		world.setBlockTileEntity(i, j, k, tile);
    	}
    	
		tile.checkPower();
    }
    
}

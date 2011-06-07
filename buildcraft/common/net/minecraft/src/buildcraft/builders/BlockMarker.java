package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.IBlockPipe;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.Orientations;

public class BlockMarker extends BlockContainer implements IPipeConnection, IBlockPipe {

	public BlockMarker(int i) {
		super(i, Material.iron);
		
		setHardness(0.5F);
		
		blockIndexInTexture = 3 * 16 + 1; 
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
    
    public boolean func_28025_b () {
    	return false;
    }    

	@Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x, int y, int z) {
		return true;
	}

	@Override
	protected TileEntity getBlockEntity() {
		return new TileMarker();
	}

    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		((TileMarker) world.getBlockTileEntity(i, j, k)).tryConnection();
        return true;
    }
    
    public void onBlockRemoval(World world, int i, int j, int k) {
    	((TileMarker) world.getBlockTileEntity(i, j, k)).destroy();
        super.onBlockRemoval(world, i, j, k);       
    }
    
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
    	((TileMarker) world.getBlockTileEntity(i, j, k)).switchSignals();
    }

	@Override
	public int getTextureForConnection(Orientations connection, int metadata) {	
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

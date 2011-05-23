package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.IPipeConnection;

public class BlockMarker extends BlockContainer implements IPipeConnection {

	public BlockMarker(int i) {
		super(i, Material.iron);
		
		setHardness(0.5F);
		
		blockIndexInTexture = ModLoader.addOverride("/terrain.png",
		"/net/minecraft/src/buildcraft/builders/gui/marker.png");
	}
	
    public int getRenderType()
    {
        return BuildCraftTransport.pipeModel;
    }
    
    public boolean isOpaqueCube()
    {
        return false;
    }
    
    public boolean renderAsNormalBlock()
    {
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

    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer)
    {
		((TileMarker) world.getBlockTileEntity(i, j, k)).tryConnection();
        return true;
    }
    
    public void onBlockRemoval(World world, int i, int j, int k)
    {
    	((TileMarker) world.getBlockTileEntity(i, j, k)).destroy();
        super.onBlockRemoval(world, i, j, k);       
    }
    
    public void onNeighborBlockChange(World world, int i, int j, int k, int l)
    {
    	((TileMarker) world.getBlockTileEntity(i, j, k)).switchSignals();
    }
}

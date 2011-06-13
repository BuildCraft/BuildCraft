package net.minecraft.src.buildcraft.builders;

import java.util.Random;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Vec3D;
import net.minecraft.src.World;

public class BlockMarker extends BlockContainer {

	public BlockMarker(int i) {
		super(i, Material.circuits);		
		
		blockIndexInTexture = ModLoader.addOverride("/terrain.png",
				"/net/minecraft/src/buildcraft/builders/gui/marker.png");
	}
	
    public int getRenderType() {
        return BuildCraftCore.markerModel;
    }
    
    public boolean func_28025_b () {
    	return false;
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
    
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
        return Block.torchWood.getCollisionBoundingBoxFromPool(world, i, j, k);
    }

    public boolean isOpaqueCube() {
        return Block.torchWood.isOpaqueCube ();
    }

    public boolean renderAsNormalBlock() {
        return false;
    }
    
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
    	((TileMarker) world.getBlockTileEntity(i, j, k)).switchSignals();
    	
    	Block.torchWood.onNeighborBlockChange(world, i, j, k, l);
    }

    public MovingObjectPosition collisionRayTrace(World world, int i, int j, int k, Vec3D vec3d, Vec3D vec3d1) {
    	return Block.torchWood.collisionRayTrace(world, i, j, k, vec3d, vec3d1);
    }
    
    public boolean canPlaceBlockAt(World world, int i, int j, int k) {
    	return Block.torchWood.canPlaceBlockAt(world, i, j, k);
    }

    public void onBlockPlaced(World world, int i, int j, int k, int l) {
    	super.onBlockPlaced(world, i, j, k, l);
    	Block.torchWood.onBlockPlaced(world, i, j, k, l);
    }
    

    public void onBlockAdded(World world, int i, int j, int k) {
    	super.onBlockAdded(world, i, j, k);
    	Block.torchWood.onBlockAdded(world, i, j, k);
    }
    
    public void dropBlockAsItem(World world, int i, int j, int k, int l) {
    	super.dropBlockAsItem(world, i, j, k, blockID);
    }
}

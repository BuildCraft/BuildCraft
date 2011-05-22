package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;

public class BlockMiningWell extends BlockMachineRoot {

	int textureFront, textureSides, textureBack, textureTop;
	
	public BlockMiningWell(int i) {
		super(i, Material.ground);
		
		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);
		
		textureFront = ModLoader.addOverride("/terrain.png",
		"/net/minecraft/src/buildcraft/factory/gui/mining_machine_front.png");
		textureSides = ModLoader.addOverride("/terrain.png",
		"/net/minecraft/src/buildcraft/factory/gui/mining_machine_side.png");
		textureBack = ModLoader.addOverride("/terrain.png",
		"/net/minecraft/src/buildcraft/factory/gui/mining_machine_back.png");
		textureTop = ModLoader.addOverride("/terrain.png",
		"/net/minecraft/src/buildcraft/factory/gui/mining_machine_top.png");

	}
	
    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		TileMiningWell tile = (TileMiningWell) world.getBlockTileEntity(i, j, k);
    	
    	tile.dig();
    	
        return false;
    }
    
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {    	    	    	
    	TileMiningWell tile = (TileMiningWell) world.getBlockTileEntity(i, j, k);
    	
		tile.checkPower();
    }
    
    public int getBlockTextureFromSideAndMetadata(int i, int j)
    {
    	if (j == 0 && i == 3) {
    		return textureFront;
    	}
    	
    	if (i == 1) {
    		return textureTop;
    	} else if (i == 0) {
    		return textureBack;
    	} else if (i == j) {
    		return textureFront;
    	} else if (Orientations.values()[j].reverse().ordinal() == i) {
    		return textureBack;
    	} else {
    		return textureSides;
    	}
    }
    
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving)
    {
		Orientations orientation = Utils.get2dOrientation(new Position(entityliving.posX,
				entityliving.posY, entityliving.posZ), new Position(i, j, k));    	
    	
    	world.setBlockMetadataWithNotify(i, j, k, orientation.reverse().ordinal()); 	
    }
    

	@Override
	protected TileEntity getBlockEntity() {		
		return new TileMiningWell();
	}
	
}

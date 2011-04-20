package net.minecraft.src.buildcraft;

import java.util.LinkedList;
import java.util.TreeMap;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class BlockMachine extends BlockContainer implements ITickListener {
	
	int textureTop;
	int textureFront;
	int textureSide;
		
	TreeMap <BlockIndex, TileMachine> workingMachines = new TreeMap <BlockIndex, TileMachine> ();
	
	public BlockMachine(int i, int j) {
		super(i, j, Material.rock);
		
		setHardness(1.5F);
		setResistance(10F);
		setLightValue(0.9375F);
		setStepSound(soundStoneFootstep);
		
		textureSide = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/machine_side.png");
		textureFront = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/machine_front.png");
		textureTop = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/mining_machine_top.png");
		mod_BuildCraft.getInstance().registerTicksListener(this, 40);	
		
		
	}
	
	public float getBlockBrightness	(IBlockAccess iblockaccess, int i, int j, int k)
    {
        return 10;
    }
    
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving)
    {
		Orientations orientation = Utils.get2dOrientation(new Position(entityliving.posX,
				entityliving.posY, entityliving.posZ), new Position(i, j, k));    	
    	
    	world.setBlockMetadataWithNotify(i, j, k, orientation.reverse().ordinal());    	
    	
    	registerMachine(world, i, j, k, orientation);
    }
    
    
    
    public void registerMachine (World world, int i, int j, int k, Orientations orientation) {
    	
    	TileMachine newTile = new TileMachine(i, j, k, orientation);
		workingMachines.put(new BlockIndex(i, j, k),
				newTile);
		
    	world.setBlockTileEntity(i, j, k, newTile);
		
		Position p = new Position (i, j, k, orientation);
		
		p.moveForwards(1);
		p.moveLeft(2);
		
		for (int h = 0; h < 2; ++h) {
			for (int s = 0; s < 4; ++s) {
				p.moveRight(1);
				world.setBlockWithNotify((int) p.i, (int) p.j, (int) p.k, Block.fence.blockID);
			}

			for (int s = 0; s < 4; ++s) {
				p.moveForwards(1);
				world.setBlockWithNotify((int) p.i, (int) p.j, (int) p.k, Block.fence.blockID);
			}

			for (int s = 0; s < 4; ++s) {
				p.moveLeft(1);
				world.setBlockWithNotify((int) p.i, (int) p.j, (int) p.k, Block.fence.blockID);
			}

			for (int s = 0; s < 4; ++s) {
				p.moveBackwards(1);
				world.setBlockWithNotify((int) p.i, (int) p.j, (int) p.k, Block.fence.blockID);
			}
			
			p.j++;
		}
    }

	@Override
	// TODO: move that in the tile
	public void tick(Minecraft minecraft) {
		LinkedList<TileMachine> toRemove = new LinkedList<TileMachine>();
		
		for (TileMachine w : workingMachines.values()) {
			w.work (minecraft);
			
			if (!w.isDigging) {
				toRemove.add(w);
			}
		}
		
		for (TileMachine w : toRemove) {
			workingMachines.remove(new BlockIndex (w.i, w.j, w.k));
		}				
	}	   

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		// If no metadata is set, then this is an icon.
		if (j == 0 && i == 3) {
			return textureFront;
		}
		
		if (i == j) {
			return textureFront;
		}

		switch (i) {
		case 1:
			return textureTop;
		default:
			return textureSide;
		}
	}
	
    public void onBlockDestroyedByPlayer(World world, int i, int j, int k, int l)
    {
    	if (workingMachines.containsKey(new BlockIndex (i, j, k))) {
    		workingMachines.remove(new BlockIndex (i, j, k));
    	}
    }

	@Override
	protected TileEntity getBlockEntity() {		
		return new TileMachine(0, 0, 0, Orientations.Unknown);
	}
}

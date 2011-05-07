package net.minecraft.src.buildcraft;

import java.util.LinkedList;
import java.util.TreeMap;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class BlockMachine extends BlockContainer {
	
	public static final BluePrint bluePrint;
	
	public static final int MINING_FIELD_SIZE = 9; 
	
	int textureTop;
	int textureFront;
	int textureSide;
		
	TreeMap <BlockIndex, TileMachine> workingMachines = new TreeMap <BlockIndex, TileMachine> ();
	
	public BlockMachine(int i) {
		super(i, Material.rock);
		
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
		Position p = new Position (i, j, k, orientation);
				
		p.moveForwards(1);
		p.moveLeft((MINING_FIELD_SIZE - 1) / 2 + 1);
		
		double xMin = Integer.MAX_VALUE, zMin = Integer.MAX_VALUE;

		for (int s = 0; s < MINING_FIELD_SIZE + 1; ++s) {
			p.moveRight(1);

			if (p.i < xMin) {
				xMin = p.i;
			}

			if (p.k < zMin) {
				zMin = p.k;
			}
		}

		for (int s = 0; s < MINING_FIELD_SIZE + 1; ++s) {
			p.moveForwards(1);

			if (p.i < xMin) {
				xMin = p.i;
			}

			if (p.k < zMin) {
				zMin = p.k;
			}
		}

		for (int s = 0; s < MINING_FIELD_SIZE + 1; ++s) {
			p.moveLeft(1);

			if (p.i < xMin) {
				xMin = p.i;
			}

			if (p.k < zMin) {
				zMin = p.k;
			}
		}

		for (int s = 0; s < MINING_FIELD_SIZE + 1; ++s) {
			p.moveBackwards(1);

			if (p.i < xMin) {
				xMin = p.i;
			}

			if (p.k < zMin) {
				zMin = p.k;
			}
		}
		
		TileMachine newTile = new TileMachine((int) xMin, (int) zMin);
		workingMachines.put(new BlockIndex(i, j, k),
				newTile);
		
    	world.setBlockTileEntity(i, j, k, newTile);
    }
    
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {    	    	    	
    	TileMachine tile = Utils.getSafeTile(world, i, j, k,
    			TileMachine.class);
    	
    	if (tile == null) {
    		tile = new TileMachine();
    		world.setBlockTileEntity(i, j, k, tile);
    	}
    	
		tile.checkPower();    	        
    }

    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
    	TileMachine tile = Utils.getSafeTile(world, i, j, k,
    			TileMachine.class);
    	
    	if (tile == null) {
    		tile = new TileMachine();
    		world.setBlockTileEntity(i, j, k, tile);
    	}
    	
		tile.work();
    	
        return false;
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
		return new TileMachine(0, 0);
	}
	
	static {
		bluePrint = new BluePrint (MINING_FIELD_SIZE + 2, 5, MINING_FIELD_SIZE + 2);
		
		for (int i = 0; i < MINING_FIELD_SIZE + 2; ++i) {
			for (int j = 0; j < 5; ++j) {
				for (int k = 0; k < MINING_FIELD_SIZE + 2; ++k) {
					bluePrint.setBlockId(i, j, k, 0);
				}
			}
		}
		
		for (int j = 0; j < 5; j += 4) {
			for (int i = 0; i < MINING_FIELD_SIZE + 2; ++i) {
				bluePrint.setBlockId(i, j, 0,
						mod_BuildCraft.getInstance().frameBlock.blockID);
				bluePrint.setBlockId(i, j, MINING_FIELD_SIZE + 1,
						mod_BuildCraft.getInstance().frameBlock.blockID);
			}
			
			for (int k = 0; k < MINING_FIELD_SIZE + 2; ++k) {
				bluePrint.setBlockId(0, j, k,
						mod_BuildCraft.getInstance().frameBlock.blockID);
				bluePrint.setBlockId(MINING_FIELD_SIZE + 1, j, k,
						mod_BuildCraft.getInstance().frameBlock.blockID);

			}
		}
		
		for (int h = 1; h < 4; ++h) {
			bluePrint.setBlockId(0, h, 0,
					mod_BuildCraft.getInstance().frameBlock.blockID);
			bluePrint.setBlockId(0, h, MINING_FIELD_SIZE + 1,
					mod_BuildCraft.getInstance().frameBlock.blockID);
			bluePrint.setBlockId(MINING_FIELD_SIZE + 1, h, 0,
					mod_BuildCraft.getInstance().frameBlock.blockID);
			bluePrint.setBlockId(MINING_FIELD_SIZE + 1, h, MINING_FIELD_SIZE + 1,
					mod_BuildCraft.getInstance().frameBlock.blockID);
		}
	}	
}

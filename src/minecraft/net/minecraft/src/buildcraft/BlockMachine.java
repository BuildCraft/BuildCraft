package net.minecraft.src.buildcraft;

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
	
	public BlockMachine(int i) {
		super(i, Material.iron);
		
		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);
		
		textureSide = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/quary_side.png");
		textureFront = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/quary_front.png");
		textureTop = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/quary_top.png");	
		
	}
	
	public float getBlockBrightness	(IBlockAccess iblockaccess, int i, int j, int k)
    {	
		for (int x = i - 1; x <= i + 1; ++x)
			for (int y = j - 1; y <= j + 1; ++y)
				for (int z = k - 1; z <= k + 1; ++z) {
					TileEntity tile = iblockaccess.getBlockTileEntity(x, y, z);		
					
					if (tile instanceof TileMachine && ((TileMachine)tile).isDigging) {
						return super.getBlockBrightness(iblockaccess, i, j, k) + 0.5F;
					} 
				}
		
		return super.getBlockBrightness(iblockaccess, i, j, k);
    }
    
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving)
    {
    	super.onBlockPlacedBy(world, i, j, k, entityliving);
    	
		Orientations orientation = Utils.get2dOrientation(new Position(entityliving.posX,
				entityliving.posY, entityliving.posZ), new Position(i, j, k));    	
    	
    	world.setBlockMetadataWithNotify(i, j, k, orientation.reverse().ordinal());    	  	
    	
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
		
		((TileMachine) world.getBlockTileEntity(i, j, k)).setMinPos((int) xMin,
				(int) zMin);
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
    
	@Override
	protected TileEntity getBlockEntity() {		
		return new TileMachine();
	}
	

	public void onBlockRemoval(World world, int i, int j, int k) {
		((TileMachine) world.getBlockTileEntity(i, j, k)).delete();
		
		super.onBlockRemoval(world, i, j, k);
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

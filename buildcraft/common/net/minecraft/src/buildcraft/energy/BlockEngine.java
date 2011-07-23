package net.minecraft.src.buildcraft.energy;

import java.util.Random;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.ICustomHeightInPipe;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.Orientations;

public class BlockEngine extends BlockContainer implements ICustomHeightInPipe,
		IPipeConnection {
	
	public BlockEngine(int i) {
		super(i, Material.wood);
		
		setLightValue(0.6F);

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

    public int getRenderType()
    {
    	return BuildCraftCore.blockByEntityModel;
    }

	@Override
	protected TileEntity getBlockEntity() {
		return new TileEngine();
	}
	
	 public void onBlockRemoval(World world, int i, int j, int k) {
		 ((TileEngine) world.getBlockTileEntity(i, j, k)).delete();
		 super.onBlockRemoval(world, i, j, k);
	 }
	 
	public boolean blockActivated(World world, int i, int j, int k,
			EntityPlayer entityplayer) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
		
		if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftCore.wrenchItem) {
			tile.switchOrientation();
			return true;
		} else {
			if (tile.entity instanceof EngineStone) {
				EnergyProxy.displayGUISteamEngine(entityplayer, tile);
				return true;
			} else if (tile.entity instanceof EngineIron) {
				EnergyProxy.displayGUICombustionEngine(entityplayer, tile);
				return true;
			}
		}
		
		return true;
	}
	
	public void onBlockPlaced(World world, int i, int j, int k, int l) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
		tile.orientation = Orientations.YPos.ordinal();
		tile.switchOrientation();		
	}
    
	protected int damageDropped(int i) {
		return i;
	}
	
	public void randomDisplayTick(World world, int i, int j, int k, Random random) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
		
		if (!tile.isBurning()) {
			return;
		}
		
        float f = (float)i + 0.5F;
        float f1 = (float)j + 0.0F + (random.nextFloat() * 6F) / 16F;
        float f2 = (float)k + 0.5F;
        float f3 = 0.52F;
        float f4 = random.nextFloat() * 0.6F - 0.3F;
        
        world.spawnParticle("reddust", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
        world.spawnParticle("reddust", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
        world.spawnParticle("reddust", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
        world.spawnParticle("reddust", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
    }

	@Override
	public float getHeightInPipe() {		
		return 0.4F;
	}

	@Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2) {
		TileEngine tile = (TileEngine) blockAccess.getBlockTileEntity(x1, y1, z1);
		
		switch (tile.entity.orientation) {
		case YPos:
			return y1 - y2 != -1;
		case YNeg:
			return y1 - y2 != 1;
		case ZPos:
			return z1 - z2 != -1;
		case ZNeg:
			return z1 - z2 != 1;
		case XPos:
			return x1 - x2 != -1;
		case XNeg:
			return x1 - x2 != 1;
		}
		
		return true;
	}
}

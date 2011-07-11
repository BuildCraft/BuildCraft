package net.minecraft.src.buildcraft.energy;

import java.util.Random;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.ICustomHeightInPipe;
import net.minecraft.src.buildcraft.api.Orientations;

public class BlockEngine extends BlockContainer implements ICustomHeightInPipe {
	
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
		
		if (tile.entity instanceof EngineStone) {
			EnergyProxy.displayGUISteamEngine(entityplayer, tile);
			return true;
		} else if (tile.entity instanceof EngineIron) {
			EnergyProxy.displayGUICombustionEngine(entityplayer, tile);
			return true;
		} else {
			tile.switchOrientation();
		}

		return false;
	}
	
	public void onBlockPlaced(World world, int i, int j, int k, int l) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);
		tile.orientation = Orientations.YPos.ordinal();
		tile.switchOrientation();		
	}
    
	protected int damageDropped(int i) {
		return i;
	}
	
	public void randomDisplayTick(World world, int i, int j, int k, Random random)
    {

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
}

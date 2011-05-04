package net.minecraft.src.buildcraft;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockMiningWell extends BlockContainer {

	public BlockMiningWell(int i) {
		super(i, Material.ground);
		
		setHardness(1.5F);
		setResistance(10F);
		setLightValue(0.9375F);
		setStepSound(soundStoneFootstep);
		
		blockIndexInTexture = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/mining_machine.png");
	}
	
	public float getBlockBrightness	(IBlockAccess iblockaccess, int i, int j, int k)
    {
        return 10;
    }
	
    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		TileMiningWell tile = Utils.getSafeTile(world, i, j, k,
				TileMiningWell.class);
    	
    	if (tile == null) {
    		tile = new TileMiningWell();
    		world.setBlockTileEntity(i, j, k, tile);
    	}
    	
    	tile.dig();
    	
        return false;
    }
    
    public void onNeighborBlockChange(World world, int i, int j, int k, int l) {    	    	    	
    	TileMiningWell tile = Utils.getSafeTile(world, i, j, k,
    			TileMiningWell.class);
    	
    	if (tile == null) {
    		tile = new TileMiningWell();
    		world.setBlockTileEntity(i, j, k, tile);
    	}
    	
		tile.checkPower();
    }

	@Override
	protected TileEntity getBlockEntity() {		
		return new TileMiningWell();
	}
	
}

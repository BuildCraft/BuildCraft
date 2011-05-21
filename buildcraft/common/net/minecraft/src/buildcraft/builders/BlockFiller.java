package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockFiller extends BlockContainer {

	public BlockFiller(int i) {
		super(i, Material.iron);
		
		blockIndexInTexture = ModLoader.addOverride("/terrain.png",
		"/net/minecraft/src/buildcraft/builders/gui/filler.png");
	}
	
	public boolean blockActivated(World world, int i, int j, int k,
			EntityPlayer entityplayer) {
		
		entityplayer.displayGUIChest(((IInventory) (world.getBlockTileEntity(i,
				j, k))));
		
		return true;
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileFiller();
	}

}

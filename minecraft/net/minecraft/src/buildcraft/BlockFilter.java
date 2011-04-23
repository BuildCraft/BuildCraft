package net.minecraft.src.buildcraft;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockFilter extends BlockContainer {
	
	int textures [];
	
	public BlockFilter(int i) {
		super(i, Material.glass);
		
		setHardness(1.5F);
		setResistance(10F);
		textures = new int [6];
		
		textures [0] = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/filter_0.png");
		textures [1] = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/filter_1.png");
		textures [2] = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/filter_2.png");
		textures [3] = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/filter_3.png");
		textures [4] = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/filter_4.png");
		textures [5] = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/filter_5.png");

	}
	
	@Override
	public int getBlockTextureFromSide (int i) {
		return textures [Orientations.values()[i].ordinal()];
	}

	
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		TileRooter tileRooter = null;
		
		if (world.getBlockTileEntity(i, j, k) == null) {
			tileRooter = new TileRooter();
			world.setBlockTileEntity(i, j, k, tileRooter);
		} else {
			tileRooter = (TileRooter) world.getBlockTileEntity(i, j, k); 
		}
		
		ModLoader.getMinecraftInstance().displayGuiScreen(
				new GuiFilter(entityplayer.inventory, tileRooter));
		
		return true;
	}

	@Override
	protected TileEntity getBlockEntity() {
		return new TileRooter();
	}
	    
}

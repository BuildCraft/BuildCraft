package net.minecraft.src.buildcraft;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockRooter extends BlockContainer {
	
	int textures [];
	
	public BlockRooter(int i, int j) {
		super(i, j, Material.glass);
		
		setHardness(1.5F);
		setResistance(10F);
		textures = new int [6];
		
		textures [0] = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/rooter_0.png");
		textures [1] = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/rooter_1.png");
		textures [2] = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/rooter_2.png");
		textures [3] = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/rooter_3.png");
		textures [4] = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/rooter_4.png");
		textures [5] = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/rooter_5.png");

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
				new GuiRooter(entityplayer.inventory, tileRooter));
		
		return true;
	}

	@Override
	protected TileEntity getBlockEntity() {
		return new TileRooter();
	}
	    
}

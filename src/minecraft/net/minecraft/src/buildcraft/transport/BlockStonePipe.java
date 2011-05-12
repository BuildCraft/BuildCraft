package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;

public class BlockStonePipe extends BlockPipe {
	
	
	public BlockStonePipe(int i) {
		super(i, Material.rock);

		blockIndexInTexture = ModLoader.addOverride("/terrain.png",
		"/net/minecraft/src/buildcraft/transport/gui/stone_pipe.png");
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileStonePipe ();
	}
	
}

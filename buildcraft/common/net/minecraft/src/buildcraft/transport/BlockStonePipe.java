package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;

public class BlockStonePipe extends BlockPipe {
	
	
	public BlockStonePipe(int i) {
		super(i, Material.rock);

		blockIndexInTexture = 1 * 16 + 1;
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileStonePipe ();
	}
	
}

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;

public class BlockGoldenPipe extends BlockPipe {
	
	
	public BlockGoldenPipe(int i) {
		super(i, Material.iron);

		blockIndexInTexture = 1 * 16 + 4;
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileGoldenPipe ();
	}

	
}

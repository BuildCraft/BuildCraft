package net.minecraft.src.buildcraft;

import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;

public class BlockGoldenPipe extends BlockPipe {
	
	
	public BlockGoldenPipe(int i) {
		super(i, Material.iron);

		blockIndexInTexture = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/golden_pipe.png");
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileGoldenPipe ();
	}

	
}

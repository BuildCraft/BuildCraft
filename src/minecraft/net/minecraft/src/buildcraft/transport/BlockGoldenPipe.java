package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;

public class BlockGoldenPipe extends BlockPipe {
	
	
	public BlockGoldenPipe(int i) {
		super(i, Material.iron);

		blockIndexInTexture = ModLoader.addOverride("/terrain.png",
		"/net/minecraft/src/buildcraft/transport/gui/golden_pipe.png");
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new TileGoldenPipe ();
	}

	
}

package net.minecraft.src.buildcraft;

import net.minecraft.src.ModLoader;

public class BlockWoodenPipe extends BlockPipe {
	
	
	public BlockWoodenPipe(int i) {
		super(i);

		blockIndexInTexture = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/wooden_pipe.png");
	}
	
}

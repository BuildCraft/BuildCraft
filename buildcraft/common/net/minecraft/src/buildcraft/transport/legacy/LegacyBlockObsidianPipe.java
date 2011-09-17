package net.minecraft.src.buildcraft.transport.legacy;

import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.buildcraft.transport.BlockGenericPipe;

public class LegacyBlockObsidianPipe extends LegacyBlock {
	
	public LegacyBlockObsidianPipe(int i) {
		super(i, BuildCraftTransport.pipeItemsObsidian.shiftedIndex);
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new LegacyTileObsidianPipe ();
	}
	
}

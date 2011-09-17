package net.minecraft.src.buildcraft.transport.legacy;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BlockLadder;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.transport.BlockGenericPipe;

public class LegacyBlockDiamondPipe extends LegacyBlock {
	
	public LegacyBlockDiamondPipe(int i) {
		super(i, BuildCraftTransport.pipeItemsDiamond.shiftedIndex);
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new LegacyTileDiamondPipe ();
	}
	
}

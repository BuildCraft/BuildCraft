package net.minecraft.src.buildcraft.transport.legacy;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.transport.BlockGenericPipe;

public class LegacyBlockIronPipe extends LegacyBlock {
	
	public LegacyBlockIronPipe(int i) {
		super(i, BuildCraftTransport.pipeItemsIron.shiftedIndex);
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new LegacyTileIronPipe ();
	}
	
  
}

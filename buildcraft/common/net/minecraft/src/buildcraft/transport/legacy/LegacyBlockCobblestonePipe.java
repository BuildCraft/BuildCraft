package net.minecraft.src.buildcraft.transport.legacy;

import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.TileEntity;

public class LegacyBlockCobblestonePipe extends LegacyBlock {
	
	public LegacyBlockCobblestonePipe(int i) {
		super(i, BuildCraftTransport.pipeItemsCobblestone.shiftedIndex);	
	}
	
	@Override
	protected TileEntity getBlockEntity() {
		return new LegacyTileCobblestonePipe ();
	}
	

}

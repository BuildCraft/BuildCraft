package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;

public class LegacyBlock extends BlockContainer {
	
	public int newPipeId;
	
	public LegacyBlock (int itemId, int newPipeId) {
		super (itemId, Material.glass);
		this.newPipeId = newPipeId;
	}

	@Override
	protected TileEntity getBlockEntity() {		
		return new LegacyTile();
	}

}

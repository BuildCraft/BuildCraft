package net.minecraft.src.buildcraft.transport.legacy;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.transport.BlockGenericPipe;

public abstract class LegacyBlock extends BlockContainer {
	
	int newPipeId;
	
	public LegacyBlock (int itemId, int newPipeId) {
		super (itemId, Material.glass);
		this.newPipeId = newPipeId;
	}
	
    public void onBlockAdded(World world, int i, int j, int k) {
		world.setBlock(i, j, k,
				BuildCraftTransport.genericPipeBlock.blockID);
		BlockGenericPipe.createPipe(i, j, k, newPipeId);
    }

}

package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicIron;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;

public class PipeItemsIron extends Pipe {
	
	private int baseTexture = 1 * 16 + 2;
	private int plainTexture = 1 * 16 + 3;
	private int nextTexture = baseTexture;

	public PipeItemsIron(int itemID) {
		super(new PipeTransportItems(), new PipeLogicIron(), itemID);
	}
	
	@Override
	public void prepareTextureFor(Orientations connection) {
		if (connection == Orientations.Unknown) {
			nextTexture = baseTexture;
		} else {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == connection.ordinal()) {
				nextTexture = baseTexture;
			} else {
				nextTexture = plainTexture;
			}
		}
	}
	
	@Override
	public int getBlockTexture() {
		return nextTexture;
	}

}

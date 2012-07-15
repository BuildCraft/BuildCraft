package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicCobblestone;
import net.minecraft.src.buildcraft.transport.PipeTransportStructure;

public class PipeStructureCobblestone extends Pipe {

	public PipeStructureCobblestone(int itemID) {
		super(new PipeTransportStructure(), new PipeLogicCobblestone(), itemID);

	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}
	
	@Override
	public int getTextureIndex(Orientations direction) {
		return 7 * 16 + 13;
	}


}

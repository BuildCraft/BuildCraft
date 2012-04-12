package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.GuiIds;
import net.minecraft.src.forge.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {

		if(!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(!(tile instanceof TileGenericPipe))
			return null;

		TileGenericPipe pipe = (TileGenericPipe)tile;

		switch(ID) {

		case GuiIds.PIPE_DIAMOND:
			return new GuiDiamondPipe(player.inventory, pipe);

		default:
			return null;
		}
	}

}

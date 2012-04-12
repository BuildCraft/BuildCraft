package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
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

		switch(ID) {

		case GuiIds.AUTO_CRAFTING_TABLE:
			if(!(tile instanceof TileAutoWorkbench))
				return null;
			return new ContainerAutoWorkbench(player.inventory, (TileAutoWorkbench)tile);

		default:
			return null;
		}
	}

}

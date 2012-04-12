package net.minecraft.src.buildcraft.factory;

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

		switch(ID) {

		case GuiIds.AUTO_CRAFTING_TABLE:
			if(!(tile instanceof TileAutoWorkbench))
				return null;
			return new GuiAutoCrafting(player.inventory, world, (TileAutoWorkbench)tile);

		default:
			return null;
		}
	}

}

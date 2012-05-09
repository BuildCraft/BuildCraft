package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.EntityPlayer;
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

		case GuiIds.ARCHITECT_TABLE:
			if(!(tile instanceof TileArchitect))
				return null;
			return new GuiTemplate(player.inventory, (TileArchitect)tile);

		case GuiIds.BLUEPRINT_LIBRARY:
			if(!(tile instanceof TileBlueprintLibrary))
				return null;
			return new GuiBlueprintLibrary(player, (TileBlueprintLibrary)tile);

		case GuiIds.BUILDER:
			if(!(tile instanceof TileBuilder))
				return null;
			return new GuiBuilder(player.inventory, (TileBuilder)tile);

		case GuiIds.FILLER:
			if(!(tile instanceof TileFiller))
				return null;
			return new GuiFiller(player.inventory, (TileFiller)tile);

		default:
			return null;
		}

	}

}

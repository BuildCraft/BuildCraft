package buildcraft.builders;

import buildcraft.builders.gui.ContainerBlueprintLibrary;
import buildcraft.builders.gui.ContainerBuilder;
import buildcraft.builders.gui.ContainerFiller;
import buildcraft.builders.gui.ContainerTemplate;
import buildcraft.builders.gui.GuiBlueprintLibrary;
import buildcraft.builders.gui.GuiBuilder;
import buildcraft.builders.gui.GuiFiller;
import buildcraft.builders.gui.GuiTemplate;
import buildcraft.core.GuiIds;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);

		switch (ID) {

		case GuiIds.ARCHITECT_TABLE:
			if (!(tile instanceof TileArchitect))
				return null;
			return new GuiTemplate(player.inventory, (TileArchitect) tile);

		case GuiIds.BLUEPRINT_LIBRARY:
			if (!(tile instanceof TileBlueprintLibrary))
				return null;
			return new GuiBlueprintLibrary(player, (TileBlueprintLibrary) tile);

		case GuiIds.BUILDER:
			if (!(tile instanceof TileBuilder))
				return null;
			return new GuiBuilder(player.inventory, (TileBuilder) tile);

		case GuiIds.FILLER:
			if (!(tile instanceof TileFiller))
				return null;
			return new GuiFiller(player.inventory, (TileFiller) tile);

		default:
			return null;
		}

	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);

		switch (ID) {

		case GuiIds.ARCHITECT_TABLE:
			if (!(tile instanceof TileArchitect))
				return null;
			return new ContainerTemplate(player.inventory, (TileArchitect) tile);

		case GuiIds.BLUEPRINT_LIBRARY:
			if (!(tile instanceof TileBlueprintLibrary))
				return null;
			return new ContainerBlueprintLibrary(player, (TileBlueprintLibrary) tile);

		case GuiIds.BUILDER:
			if (!(tile instanceof TileBuilder))
				return null;
			return new ContainerBuilder(player.inventory, (TileBuilder) tile);

		case GuiIds.FILLER:
			if (!(tile instanceof TileFiller))
				return null;
			return new ContainerFiller(player.inventory, (TileFiller) tile);

		default:
			return null;
		}
	}

}

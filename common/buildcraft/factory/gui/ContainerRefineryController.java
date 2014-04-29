package buildcraft.factory.gui;

import buildcraft.factory.TileRefineryController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerRefineryController extends Container {

	private final EntityPlayer player;

	private final TileRefineryController tile;

	public ContainerRefineryController(EntityPlayer player, TileRefineryController tile) {
		this.player = player;
		this.tile = tile;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

}

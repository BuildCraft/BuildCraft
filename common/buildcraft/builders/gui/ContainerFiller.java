/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.gui;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.builders.TileFiller;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.GuiBuildCraft;
import buildcraft.core.lib.gui.widgets.Widget;

public class ContainerFiller extends BuildCraftContainer {

	IInventory playerIInventory;
	TileFiller tile;

	private class PatternWidget extends Widget {

		public PatternWidget() {
			super(38, 30, 0, 0, 16, 16);
		}

		@SideOnly(Side.CLIENT)
		@Override
		public void draw(GuiBuildCraft gui, int guiX, int guiY, int mouseX, int mouseY) {
			gui.bindTexture(TextureMap.locationItemsTexture);
			gui.drawTexturedModelRectFromIcon(guiX + x, guiY + y, tile.currentPattern.getIcon(), 16, 16);
		}
	}

	public ContainerFiller(IInventory playerInventory, TileFiller tile) {
		super(tile.getSizeInventory());
		this.playerIInventory = playerInventory;
		this.tile = tile;

		addWidget(new PatternWidget());

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				addSlotToContainer(new Slot(tile, x + y * 9, 8 + x * 18, 85 + y * 18));
			}
		}

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 153 + y * 18));
			}

		}

		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 211));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return tile.isUseableByPlayer(entityplayer);
	}
}

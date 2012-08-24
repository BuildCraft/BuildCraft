/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.core.BptBase;
import buildcraft.core.BptPlayerIndex;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.utils.StringUtil;

public class GuiBlueprintLibrary extends GuiBuildCraft {

	EntityPlayer player;
	TileBlueprintLibrary library;

	int highlighted;

	ContainerBlueprintLibrary container;

	boolean computeInput;

	BptPlayerIndex index;

	public GuiBlueprintLibrary(EntityPlayer player, TileBlueprintLibrary library) {
		super(new ContainerBlueprintLibrary(player, library), library);
		this.player = player;
		xSize = 176;
		ySize = 225;

		this.library = library;
		container = (ContainerBlueprintLibrary) inventorySlots;

		container.contents = library.getNextPage(null);
		index = BuildCraftBuilders.getPlayerIndex(player.username);
	}

	private GuiButton nextPageButton;
	private GuiButton prevPageButton;
	private GuiButton lockButton;
	private GuiButton deleteButton;

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		prevPageButton = new GuiButton(0, j + 100, k + 23, 20, 20, "<");
		nextPageButton = new GuiButton(1, j + 122, k + 23, 20, 20, ">");

		controlList.add(prevPageButton);
		controlList.add(nextPageButton);

		// if (library.owner.equals(player.username)) {
		deleteButton = new GuiButton(2, j + 100, k + 114, 25, 20, StringUtil.localize("gui.del"));
		controlList.add(deleteButton);

		lockButton = new GuiButton(3, j + 127, k + 114, 40, 20, StringUtil.localize("gui.lock"));
		controlList.add(lockButton);

		if (library.locked)
			lockButton.displayString = StringUtil.localize("gui.unlock");
		else
			lockButton.displayString = StringUtil.localize("gui.lock");
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		// fontRenderer.drawString(library.owner + "'s Library", 6, 6,
		// 0x404040);
		String title = StringUtil.localize("tile.libraryBlock");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);

		int c = 0;
		for (BptBase bpt : container.contents) {
			if (bpt == library.selected) {
				int l1 = 8;
				int i2 = 24;
				drawGradientRect(l1, i2 + 9 * c, l1 + 88, i2 + 9 * (c + 1), 0x80ffffff, 0x80ffffff);
			}
			String name = bpt.getName();

			while (fontRenderer.getStringWidth(name) > BuildCraftBuilders.MAX_BLUEPRINTS_NAME_SIZE)
				name = name.substring(0, name.length() - 1);

			fontRenderer.drawString(name, 9, 25 + 9 * c, 0x404040);
			c++;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = 0;
		// if (library.owner.equals(player.username)) {
		i = mc.renderEngine.getTexture(DefaultProps.TEXTURE_PATH_GUI + "/library_rw.png");
		// } else {
		// i = mc.renderEngine
		// .getTexture("/net/minecraft/src/buildcraft/builders/gui/library_r.png");
		// }

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		int inP = (int) (library.progressIn / 100.0 * 22.0);
		int outP = (int) (library.progressOut / 100.0 * 22.0);

		if (inP != 0)
			computeInput = true;
		else if (computeInput) {
			// In this case, there was a store computation that has finished.
			if (container.contents.size() == 0)
				container.contents = library.getNextPage(null);
			else
				container.contents = library.getNextPage(index.prevBpt(container.contents.getFirst().file.getName()));

			computeInput = false;
		}

		drawTexturedModalRect(j + 128 + 22 - inP, k + 61, 176 + 22 - inP, 16, inP, 16);
		drawTexturedModalRect(j + 128, k + 78, 176, 0, outP, 16);
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);

		Minecraft client = FMLClientHandler.instance().getClient();
		int xMin = (width - xSize) / 2;
		int yMin = (height - ySize) / 2;

		int x = i - xMin;
		int y = j - yMin;

		if (x >= 8 && x <= 88) {
			int ySlot = (y - 24) / 9;

			if (ySlot >= 0 && ySlot <= 11)
				if (ySlot < container.contents.size())
					library.selected = container.contents.get(ySlot);
		} else if (nextPageButton.mousePressed(client, i, j)) {
			if (container.contents.size() > 0)
				container.contents = library.getNextPage(container.contents.getLast().file.getName());
			else
				container.contents = library.getNextPage(null);
		} else if (prevPageButton.mousePressed(client, i, j)) {
			if (container.contents.size() > 0)
				container.contents = library.getPrevPage(container.contents.getFirst().file.getName());
			else
				container.contents = library.getNextPage(null);
		} else if (lockButton != null && lockButton.mousePressed(client, i, j)) {
			library.locked = !library.locked;

			if (library.locked)
				lockButton.displayString = StringUtil.localize("gui.unlock");
			else
				lockButton.displayString = StringUtil.localize("gui.lock");
		} else if (deleteButton != null && deleteButton.mousePressed(client, i, j))
			if (library.selected != null) {
				index.deleteBluePrint(library.selected.file.getName());
				if (container.contents.size() > 0)
					container.contents = library.getNextPage(index.prevBpt(container.contents.getFirst().file.getName()));
				else
					container.contents = library.getNextPage(null);

				library.selected = null;
			}
	}
}

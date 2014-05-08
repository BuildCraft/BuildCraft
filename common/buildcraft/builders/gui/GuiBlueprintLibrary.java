/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.builders.blueprints.BlueprintId;
import buildcraft.builders.blueprints.BlueprintId.Kind;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.utils.StringUtils;

public class GuiBlueprintLibrary extends GuiBuildCraft {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/library_rw.png");
	private GuiButton nextPageButton;
	private GuiButton prevPageButton;
	private GuiButton lockButton;
	private GuiButton deleteButton;
	private EntityPlayer player;
	private TileBlueprintLibrary library;
	private ContainerBlueprintLibrary container;
	private boolean computeInput;

	public GuiBlueprintLibrary(EntityPlayer player, TileBlueprintLibrary library) {
		super(new ContainerBlueprintLibrary(player, library), library, TEXTURE);
		this.player = player;
		xSize = 234;
		ySize = 225;

		this.library = library;
		container = (ContainerBlueprintLibrary) inventorySlots;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		prevPageButton = new GuiButton(0, j + 158, k + 23, 20, 20, "<");
		nextPageButton = new GuiButton(1, j + 180, k + 23, 20, 20, ">");

		buttonList.add(prevPageButton);
		buttonList.add(nextPageButton);

		deleteButton = new GuiButton(2, j + 158, k + 114, 25, 20, StringUtils.localize("gui.del"));
		buttonList.add(deleteButton);

		checkDelete();
		checkPages();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("tile.libraryBlock.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);

		int c = 0;
		for (BlueprintId bpt : library.currentPage) {
			String name = bpt.name;

			if (name.length() > BuildCraftBuilders.MAX_BLUEPRINTS_NAME_SIZE) {
				name = name.substring(0, BuildCraftBuilders.MAX_BLUEPRINTS_NAME_SIZE);
			}

			if (c == library.selected) {
				int l1 = 8;
				int i2 = 24;

				if (bpt.kind == Kind.Blueprint) {
					drawGradientRect(l1, i2 + 9 * c, l1 + 146, i2 + 9 * (c + 1), 0xFFA0A0FF, 0xFFA0A0FF);
				} else {
					drawGradientRect(l1, i2 + 9 * c, l1 + 146, i2 + 9 * (c + 1), 0x80ffffff, 0x80ffffff);
				}
			}

			if (bpt.kind == Kind.Blueprint) {
				fontRendererObj.drawString(name, 9, 25 + 9 * c, 0x1000FF);
			} else {
				fontRendererObj.drawString(name, 9, 25 + 9 * c, 0x000000);
			}

			c++;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);

		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		int inP = (int) (library.progressIn / 100.0 * 22.0);
		int outP = (int) (library.progressOut / 100.0 * 22.0);

		drawTexturedModalRect(j + 186 + 22 - inP, k + 61, 234 + 22 - inP, 16, inP, 16);
		drawTexturedModalRect(j + 186, k + 78, 234, 0, outP, 16);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button == nextPageButton) {
			library.pageNext();
		} else if (button == prevPageButton) {
			library.pagePrev();
		} else if (deleteButton != null && button == deleteButton) {
			library.deleteSelectedBpt();
		}
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);

		int xMin = (width - xSize) / 2;
		int yMin = (height - ySize) / 2;

		int x = i - xMin;
		int y = j - yMin;

		if (x >= 8 && x <= 88) {
			int ySlot = (y - 24) / 9;

			if (ySlot >= 0 && ySlot <= 11) {
				if (ySlot < library.currentPage.size()) {
					library.selectBlueprint(ySlot);
				}
			}
		}

		checkDelete();
		checkPages();
	}

	protected void checkDelete() {
		if (library.selected != -1) {
			deleteButton.enabled = true;
		} else {
			deleteButton.enabled = false;
		}
	}

	protected void checkPages() {
		if (library.pageId != 0) {
			prevPageButton.enabled = true;
		} else {
			prevPageButton.enabled = false;
		}

		if (library.pageId < BuildCraftBuilders.clientDB.getPageNumber() - 1) {
			nextPageButton.enabled = true;
		} else {
			nextPageButton.enabled = false;
		}
	}
}

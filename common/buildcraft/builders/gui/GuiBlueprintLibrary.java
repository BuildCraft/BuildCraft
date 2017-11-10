/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.library.LibraryAPI;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.core.DefaultProps;
import buildcraft.core.blueprints.LibraryId;
import buildcraft.core.lib.gui.GuiBuildCraft;
import buildcraft.core.lib.utils.StringUtils;

public class GuiBlueprintLibrary extends GuiBuildCraft {
	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftbuilders:textures/gui/library_rw.png");
	private GuiButton deleteButton;
	private TileBlueprintLibrary library;

	public GuiBlueprintLibrary(EntityPlayer player, TileBlueprintLibrary library) {
		super(new ContainerBlueprintLibrary(player, library), library, TEXTURE);
		xSize = 244;
		ySize = 220;

		this.library = library;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		deleteButton = new GuiButton(2, guiLeft + 174, guiTop + 109, 25, 20, StringUtils.localize("gui.del"));
		buttonList.add(deleteButton);

		library.refresh();

		checkDelete();
	}

	private ContainerBlueprintLibrary getLibraryContainer() {
		return (ContainerBlueprintLibrary) getContainer();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("tile.libraryBlock.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);

		int off = getLibraryContainer().scrollbarWidget.getPosition();
		for (int i = off; i < (off + 12); i++) {
			if (i >= library.entries.size()) {
				break;
			}
			LibraryId bpt = library.entries.get(i);
			String name = bpt.name;

			if (name.length() > DefaultProps.MAX_NAME_SIZE) {
				name = name.substring(0, DefaultProps.MAX_NAME_SIZE);
			}

			if (i == library.selected) {
				int l1 = 8;
				int i2 = 22;

				drawGradientRect(l1, i2 + 9 * (i - off), l1 + 146, i2 + 9 * (i - off + 1), 0x80ffffff, 0x80ffffff);
			}

			while (fontRendererObj.getStringWidth(name) > (160 - 9)) {
				name = name.substring(0, name.length() - 1);
			}

			fontRendererObj.drawString(name, 9, 23 + 9 * (i - off), LibraryAPI.getHandlerFor(bpt.extension).getTextColor());
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);

		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		getLibraryContainer().scrollbarWidget.hidden = library.entries.size() <= 12;
		getLibraryContainer().scrollbarWidget.setLength(Math.max(0, library.entries.size() - 12));

		drawWidgets(x, y);

		int inP = library.progressIn * 22 / 100;
		int outP = library.progressOut * 22 / 100;

		drawTexturedModalRect(guiLeft + 194 + 22 - inP, guiTop + 57, 234 + 22 - inP, 240, inP, 16);
		drawTexturedModalRect(guiLeft + 194, guiTop + 79, 234, 224, outP, 16);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (deleteButton != null && button == deleteButton) {
			library.deleteSelectedBpt();
		}
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);

		int x = i - guiLeft;
		int y = j - guiTop;

		if (x >= 8 && x <= 161) {
			int ySlot = (y - 22) / 9 + getLibraryContainer().scrollbarWidget.getPosition();

			if (ySlot > -1 && ySlot < library.entries.size()) {
				library.selectBlueprint(ySlot);
			}
		}

		checkDelete();
	}

	protected void checkDelete() {
		if (library.selected != -1) {
			deleteButton.enabled = true;
		} else {
			deleteButton.enabled = false;
		}
	}
}

/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.builders.filler.pattern.FillerPattern;
import buildcraft.core.Box;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;

class UrbanistToolFiller extends UrbanistToolArea {

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/urbanist_tool_filler.png");
	private static final int GUI_TEXTURE_WIDTH = 64;
	private static final int GUI_TEXTURE_HEIGHT = 210;

	LinkedList<FillerSlot> fillerSlots = new LinkedList<FillerSlot>();

	ArrayList<IFillerPattern> patterns = new ArrayList<IFillerPattern>();

	int selection = -1;

	class FillerSlot extends AdvancedSlot {
		public int index;
		public boolean isSelected = false;

		public FillerSlot(GuiAdvancedInterface gui, int index) {
			super(gui, -100, -100);

			this.index = index;
		}

		@Override
		public ResourceLocation getTexture() {
			return TextureMap.locationBlocksTexture;
		}

		@SideOnly(Side.CLIENT)
		@Override
		public IIcon getIcon() {
			if (index < patterns.size()) {
				return getPattern().getIcon();
			} else {
				return null;
			}
		}

		@Override
		public String getDescription() {
			return getPattern().getDisplayName();
		}

		@Override
		public void selected () {
			for (FillerSlot s : fillerSlots) {
				s.isSelected = false;
			}

			isSelected = true;
			selection = index;
		}

		public IFillerPattern getPattern () {
			return patterns.get(index);
		}
	}

	public UrbanistToolFiller () {
		for (FillerPattern pattern : FillerPattern.patterns.values()) {
			patterns.add(pattern);
		}
	}

	@Override
	public IIcon getIcon() {
		return UrbanistToolsIconProvider.INSTANCE.getIcon(UrbanistToolsIconProvider.Tool_Filler);
	}

	@Override
	public String getDescription() {
		return "Build from Filler Pattern";
	}

	@Override
	public void drawGuiContainerBackgroundLayer(GuiUrbanist gui, float f, int x, int y) {
		Minecraft.getMinecraft().renderEngine.bindTexture(GUI_TEXTURE);
		gui.drawTexturedModalRect(0, 0, 0, 0, GUI_TEXTURE_WIDTH, GUI_TEXTURE_HEIGHT);
	}

	@Override
	public void drawSelection (GuiUrbanist gui, float f, int x, int y) {
		Minecraft.getMinecraft().renderEngine.bindTexture(GUI_TEXTURE);

		for (int i = 0; i < fillerSlots.size(); ++i) {
			if (fillerSlots.get(i).isSelected) {
				gui.drawTexturedModalRect(4, 42 + 18 * i, 64, 0, 18, 18);
			}
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(GuiUrbanist gui, int par1, int par2) {
		gui.getFontRenderer ().drawString("Filler", 4, 4, 0x404040);
	}

	@Override
	public boolean onInterface (int mouseX, int mouseY) {
		if (mouseX < GUI_TEXTURE_WIDTH && mouseY < GUI_TEXTURE_HEIGHT) {
			return true;
		}

		return false;
	}

	@Override
	public void createSlots(GuiUrbanist gui, LinkedList<AdvancedSlot> slots) {
		for (int i = 0; i < 8; ++i) {
			FillerSlot slot = new FillerSlot(gui, i);
			fillerSlots.add(slot);
			slots.add(slot);
		}
	}

	@Override
	public void show () {
		for (int i = 0; i < 8; ++i) {
			fillerSlots.get(i).x = 4;
			fillerSlots.get(i).y = 42 + 18 * i;
		}
	}

	@Override
	public void hide () {
		for (int i = 0; i < 8; ++i) {
			fillerSlots.get(i).x = -100;
		}
	}

	@Override
	public void areaSet (GuiUrbanist gui, int x1, int y1, int z1, int x2, int y2, int z2) {
		super.areaSet(gui, x1, y1, z1, x2, y2, z2);

		if (selection != -1) {
			Box box = new Box();
			box.initialize(x1, y1, z1, x2, y2, z2);

			gui.urbanist.rpcStartFiller(fillerSlots.get(selection).getPattern().getUniqueTag(), box);
		}

	}
}
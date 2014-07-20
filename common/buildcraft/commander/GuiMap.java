/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

import org.lwjgl.opengl.GL11;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.EnumColor;
import buildcraft.core.BCDynamicTexture;
import buildcraft.core.DefaultProps;
import buildcraft.core.MapArea;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.network.RPCHandler;

public class GuiMap extends GuiAdvancedInterface {

	private int mapWidth = 200;
	private int mapHeight = 100;

	private TileMap map;

	private BCDynamicTexture newSelection;
	private int selX1 = 0,
			selX2 = 0,
			selY1 = 0,
			selY2 = 0;
	private boolean inSelection = false;

	private BCDynamicTexture currentSelection;

	private static final ResourceLocation TMP_TEXTURE = new ResourceLocation("buildcraft",
			DefaultProps.TEXTURE_PATH_GUI + "/map_gui.png");

	private int mapXMin = 0;
	private int mapYMin = 0;

	private int zoomLevel = 1;
	private int cx;
	private int cz;

	private AreaSlot colorSelected = null;

	private float alpha = 0.8F;

	private static class AreaSlot extends AdvancedSlot {

		public EnumColor color;

		public AreaSlot(GuiAdvancedInterface gui, int x, int y, EnumColor iColor) {
			super(gui, x, y);

			color = iColor;
		}

		@Override
		public IIcon getIcon() {
			return color.getIcon();
		}

		@Override
		public String getDescription() {
			return color.getLocalizedName();
		}
	}

	public GuiMap(IInventory inventory, TileMap iMap) {
		super(new ContainerMap(iMap), inventory, TMP_TEXTURE);

		xSize = 256;
		ySize = 220;

		map = iMap;

		map.bcTexture = new BCDynamicTexture(mapWidth, mapHeight);
		map.bcTexture.createDynamicTexture();

		currentSelection = new BCDynamicTexture(mapWidth, mapHeight);
		currentSelection.createDynamicTexture();

		newSelection = new BCDynamicTexture(1, 1);
		newSelection.createDynamicTexture();


		getContainer().currentAreaSelection = new MapArea();

		cx = map.xCoord;
		cz = map.zCoord;

		slots = new AdvancedSlot[16];

		for (int i = 0; i < 4; ++i) {
			for (int j = 0; j < 4; ++j) {
				slots[i * 4 + j] = new AreaSlot(this, 8 + 18 * i, 138 + 18 * j, EnumColor.values()[i * 4 + j]);
			}
		}

		colorSelected = (AreaSlot) slots[0];

		newSelection.setColor(0, 0, colorSelected.color.getDarkHex(), alpha);

		uploadMap();
		getContainer().gui = this;
	}

	private void uploadMap() {
		RPCHandler.rpcServer(map, "computeMap", cx, cz, map.bcTexture.width, map.bcTexture.height,
				zoomLevel);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);

		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		mapXMin = (width - map.bcTexture.width) / 2;

		if (map.bcTexture.height <= 200) {
			mapYMin = cornerY + 20;
		} else {
			mapYMin = (height - map.bcTexture.height) / 2;
		}

		map.bcTexture.drawMap(mapXMin, mapYMin, zLevel);

		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_BLEND);

		currentSelection.drawMap(mapXMin, mapYMin, zLevel);

		GL11.glPopAttrib();

		newSelection.updateDynamicTexture();

		if (inSelection && selX2 != 0) {
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_BLEND);

			int x1 = selX1 < selX2 ? selX1 : selX2;
			int x2 = selX1 < selX2 ? selX2 : selX1;
			int y1 = selY1 < selY2 ? selY1 : selY2;
			int y2 = selY1 < selY2 ? selY2 : selY1;

			drawTexturedModalRect(x1, y1, 0, 0, x2 - x1 + 1, y2 - y1 + 1);
			GL11.glPopAttrib();
		}

		if (map.bcTexture.height <= 200) {
			drawBackgroundSlots();

			bindTexture(texture);
			drawTexturedModalRect(cornerX + colorSelected.x, cornerY + colorSelected.y, 0, 220, 16, 16);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int blocksX = (mouseX - mapXMin) * zoomLevel;
		int blocksZ = (mouseY - mapYMin) * zoomLevel;

		int blockStartX = cx - mapWidth * zoomLevel / 2;
		int blockStartZ = cz - mapHeight * zoomLevel / 2;

		boolean clickOnMap = mouseX >= mapXMin
				&& mouseX <= mapXMin + map.bcTexture.width && mouseY >= mapYMin &&
				mouseY <= mapYMin + map.bcTexture.height;

		if (clickOnMap) {
			if (mouseButton == 1) {
				cx = blockStartX + blocksX;
				cz = blockStartZ + blocksZ;

				uploadMap();
				refreshSelectedArea();
			} else {
				inSelection = true;
				selX1 = mouseX;
				selY1 = mouseY;
				selX2 = 0;
				selY2 = 0;
			}
		} else {
			int cornerX = (width - xSize) / 2;
			int cornerY = (height - ySize) / 2;

			int position = getSlotAtLocation(mouseX - cornerX, mouseY - cornerY);

			AdvancedSlot slot = null;

			if (position < 0) {
				return;
			}

			slot = slots[position];

			if (slot instanceof AreaSlot) {
				colorSelected = (AreaSlot) slot;

				newSelection.setColor(0, 0, colorSelected.color.getDarkHex(), alpha);
				getContainer().loadArea(colorSelected.color.ordinal());
			}
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int lastButtonBlicked, long time) {
		super.mouseClickMove(mouseX, mouseY, lastButtonBlicked, time);

		if (inSelection
				&& mouseX >= mapXMin && mouseX <= mapXMin + map.bcTexture.width
				&& mouseY >= mapYMin && mouseY <= mapYMin + map.bcTexture.height) {

			selX2 = mouseX;
			selY2 = mouseY;
		}
	}

	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int eventType) {
		super.mouseMovedOrUp(mouseX, mouseY, eventType);

		if (eventType != -1 && inSelection) {
			int blockStartX = cx - mapWidth * zoomLevel / 2;
			int blockStartZ = cz - mapHeight * zoomLevel / 2;

			int x1 = selX1 < selX2 ? selX1 : selX2;
			int x2 = selX1 < selX2 ? selX2 : selX1;
			int y1 = selY1 < selY2 ? selY1 : selY2;
			int y2 = selY1 < selY2 ? selY2 : selY1;

			int lengthX = (x2 - x1) * zoomLevel;
			int lengthY = (y2 - y1) * zoomLevel;

			for (int i = 0; i <= lengthX; ++i) {
				for (int j = 0; j <= lengthY; ++j) {
					int x = blockStartX + (x1 - mapXMin) * zoomLevel + i;
					int z = blockStartZ + (y1 - mapYMin) * zoomLevel + j;

					getContainer().currentAreaSelection.set(x, z, true);
				}
			}

			inSelection = false;
			getContainer().saveArea(colorSelected.color.ordinal());
			refreshSelectedArea();
		}
	}

	@Override
	protected void keyTyped(char carac, int val) {
		super.keyTyped(carac, val);

		if (carac == '+' && zoomLevel > 1) {
			zoomLevel--;
			uploadMap();
			refreshSelectedArea();
		} else if (carac == '-' && zoomLevel < 6) {
			zoomLevel++;
			uploadMap();
			refreshSelectedArea();
		} else if (carac == 'm') {
			mapWidth = 200;
			mapHeight = 100;

			map.bcTexture = new BCDynamicTexture(mapWidth, mapHeight);
			map.bcTexture.createDynamicTexture();

			currentSelection = new BCDynamicTexture(mapWidth, mapHeight);
			currentSelection.createDynamicTexture();

			uploadMap();
			refreshSelectedArea();
		} else if (carac == 'M') {
			mapWidth = this.mc.displayWidth;
			mapHeight = this.mc.displayHeight;

			map.bcTexture = new BCDynamicTexture(mapWidth, mapHeight);
			map.bcTexture.createDynamicTexture();

			currentSelection = new BCDynamicTexture(mapWidth, mapHeight);
			currentSelection.createDynamicTexture();

			uploadMap();
			refreshSelectedArea();
		}
	}

	public void refreshSelectedArea() {
		int color = colorSelected.color.getDarkHex();

		int rAdd = (color >> 16) & 255;
		int gAdd = (color >> 8) & 255;
		int bAdd = color & 255;

		for (int i = 0; i < currentSelection.width; ++i) {
			for (int j = 0; j < currentSelection.height; ++j) {
				int blockStartX = cx - mapWidth * zoomLevel / 2;
				int blockStartZ = cz - mapHeight * zoomLevel / 2;

				double r = 0;
				double g = 0;
				double b = 0;

				for (int stepi = 0; stepi < zoomLevel; ++stepi) {
					for (int stepj = 0; stepj < zoomLevel; ++stepj) {
						int x = blockStartX + i * zoomLevel + stepi;
						int z = blockStartZ + j * zoomLevel + stepj;

						if (getContainer().currentAreaSelection.get(x, z)) {
							r += rAdd;
							g += gAdd;
							b += bAdd;
						}
					}
				}

				r /= (zoomLevel * zoomLevel);
				g /= (zoomLevel * zoomLevel);
				b /= (zoomLevel * zoomLevel);

				r /= 255F;
				g /= 255F;
				b /= 255F;

				if (r != 0) {
					currentSelection.setColor(i, j, r, g, b, alpha);
				} else {
					currentSelection.setColor(i, j, 0, 0, 0, 0);
				}
			}
		}
	}

	@Override
	protected ContainerMap getContainer() {
		return (ContainerMap) super.getContainer();
	}
}
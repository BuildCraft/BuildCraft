/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.BCDynamicTexture;
import buildcraft.core.DefaultProps;
import buildcraft.core.MapArea;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.network.RPCHandler;

public class GuiMap extends GuiAdvancedInterface {

	private static final int MAP_WIDTH = 300;
	private static final int MAP_HEIGHT = 200;

	private TileMap map;

	private BCDynamicTexture newSelection;
	private int selX1 = 0,
			selX2 = 0,
			selY1 = 0,
			selY2 = 0;
	private boolean inSelection = false;

	private MapArea areaSelection;
	private BCDynamicTexture currentSelection;

	private static final ResourceLocation TMP_TEXTURE = new ResourceLocation("buildcraft",
			DefaultProps.TEXTURE_PATH_GUI + "/builder_blueprint.png");

	private int mapXMin = 0;
	private int mapYMin = 0;

	private int zoomLevel = 1;
	private int cx;
	private int cz;

	public GuiMap(IInventory inventory, TileMap iMap) {
		super(new ContainerMap(0), inventory, TMP_TEXTURE);

		map = iMap;

		map.bcTexture = new BCDynamicTexture(MAP_WIDTH, MAP_HEIGHT);
		map.bcTexture.createDynamicTexture();

		newSelection = new BCDynamicTexture(1, 1);
		newSelection.createDynamicTexture();
		newSelection.setColor(0, 0, 1, 0, 0, 0.4F);

		areaSelection = new MapArea();

		cx = map.xCoord;
		cz = map.zCoord;

		uploadMap();
	}

	private void uploadMap() {
		RPCHandler.rpcServer(map, "computeMap", cx, cz, map.bcTexture.width, map.bcTexture.height,
				zoomLevel);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		mapXMin = (width - map.bcTexture.width) / 2;
		mapYMin = (height - map.bcTexture.height) / 2;

		map.bcTexture.drawMap(mapXMin, mapYMin, zLevel);

		newSelection.updateDynamicTexture();

		if (selX2 != 0) {
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_BLEND);

			int x1 = selX1 < selX2 ? selX1 : selX2;
			int x2 = selX1 < selX2 ? selX2 : selX1;
			int y1 = selY1 < selY2 ? selY1 : selY2;
			int y2 = selY1 < selY2 ? selY2 : selY1;

			drawTexturedModalRect(x1, y1, 0, 0, x2 - x1 + 1, y2 - y1 + 1);
			GL11.glPopAttrib();
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int blocksX = (mouseX - mapXMin) * zoomLevel;
		int blocksZ = (mouseY - mapYMin) * zoomLevel;

		int blockStartX = cx - MAP_WIDTH * zoomLevel / 2;
		int blockStartZ = cz - MAP_HEIGHT * zoomLevel / 2;

		boolean clickOnMap = mouseX >= mapXMin
				&& mouseX <= mapXMin + map.bcTexture.width && mouseY >= mapYMin &&
				mouseY <= mapYMin + map.bcTexture.height;

		if (clickOnMap) {
			cx = blockStartX + blocksX;
			cz = blockStartZ + blocksZ;
			uploadMap();
		}

		/*
		 * if (inSelection) { inSelection = false; } else if (mouseX >= mapXMin
		 * && mouseX <= mapXMin + map.bcTexture.width && mouseY >= mapYMin &&
		 * mouseY <= mapYMin + map.bcTexture.height) {
		 *
		 * inSelection = true; selX1 = mouseX; selY1 = mouseY; selX2 = 0; selY2
		 * = 0; }
		 */
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();

		int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		if (inSelection
				&& x >= mapXMin && x <= mapXMin + map.bcTexture.width
				&& y >= mapYMin && y <= mapYMin + map.bcTexture.height) {

			selX2 = x;
			selY2 = y;
		}
	}

	@Override
	protected void keyTyped(char carac, int val) {
		super.keyTyped(carac, val);

		if (carac == 'z' && zoomLevel > 1) {
			zoomLevel--;
			uploadMap();
		} else if (carac == 'Z' && zoomLevel < 6) {
			zoomLevel++;
			uploadMap();
		}
	}
}
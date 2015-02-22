/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.SheetIcon;
import buildcraft.api.enums.EnumColor;
import buildcraft.core.BCDynamicTexture;
import buildcraft.core.DefaultProps;
import buildcraft.core.ZonePlan;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.gui.buttons.GuiBetterButton;
import buildcraft.core.gui.tooltips.ToolTip;
import buildcraft.core.gui.tooltips.ToolTipLine;
import buildcraft.core.network.CommandWriter;
import buildcraft.core.network.PacketCommand;
import buildcraft.core.utils.StringUtils;

public class GuiZonePlan extends GuiAdvancedInterface {

	private static final ResourceLocation TMP_TEXTURE = new ResourceLocation("buildcraft",
			DefaultProps.TEXTURE_PATH_GUI + "/map_gui.png");

	private int mapWidth = 200;
	private int mapHeight = 100;

	private TileZonePlan zonePlan;

	private BCDynamicTexture newSelection;
	private int selX1 = 0;
	private int selX2 = 0;
	private int selY1 = 0;
	private int selY2 = 0;

	private boolean inSelection = false;

	private BCDynamicTexture currentSelection;

	private int mapXMin = 0;
	private int mapYMin = 0;

	private int zoomLevel = 1;
	private int cx;
	private int cz;

	private AreaSlot colorSelected = null;

	private float alpha = 0.8F;

	private GuiBetterButton tool;

	private List inventorySlots;
	private List savedButtonList;

	private static class AreaSlot extends AdvancedSlot {

		public EnumColor color;

		public AreaSlot(GuiAdvancedInterface gui, int x, int y, EnumColor iColor) {
			super(gui, x, y);

			color = iColor;
		}

		@Override
		public SheetIcon getIcon() {
			return color.getIcon();
		}

		@Override
		public String getDescription() {
			return color.getLocalizedName();
		}
	}

	public GuiZonePlan(IInventory inventory, TileZonePlan iZonePlan) {
		super(new ContainerZonePlan(inventory, iZonePlan), inventory, TMP_TEXTURE);

		getContainer().gui = this;

		xSize = 256;
		ySize = 220;

		zonePlan = iZonePlan;

		getContainer().currentAreaSelection = new ZonePlan();

		cx = zonePlan.getPos().getX();
		cz = zonePlan.getPos().getZ();

		resetNullSlots(16);

		for (int i = 0; i < 4; ++i) {
			for (int j = 0; j < 4; ++j) {
				slots.set(i * 4 + j, new AreaSlot(this, 8 + 18 * i, 138 + 18 * j, EnumColor.values()[i * 4 + j]));
			}
		}

		colorSelected = (AreaSlot) slots.get(0);

		inventorySlots = container.inventorySlots;
	}

	private void initializeMap() {
		getContainer().mapTexture = new BCDynamicTexture(mapWidth, mapHeight);
		getContainer().mapTexture.createDynamicTexture();

		currentSelection = new BCDynamicTexture(mapWidth, mapHeight);
		currentSelection.createDynamicTexture();

		newSelection = new BCDynamicTexture(1, 1);
		newSelection.createDynamicTexture();

		newSelection.setColor(0, 0, colorSelected.color.getDarkHex(), alpha);

		uploadMap();
		getContainer().loadArea(colorSelected.color.ordinal());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		tool = new GuiBetterButton(0, guiLeft + 5, guiTop + 20, 20, "+");
		tool.setToolTip(new ToolTip(new ToolTipLine(StringUtils.localize("tip.tool.add"))));
		buttonList.add(tool);

		savedButtonList = buttonList;
	}

	private void uploadMap() {
		BuildCraftCore.instance.sendToServer(new PacketCommand(getContainer(), "computeMap", new CommandWriter() {
			public void write(ByteBuf data) {
				data.writeInt(cx);
				data.writeInt(cz);
				data.writeShort(getContainer().mapTexture.width);
				data.writeShort(getContainer().mapTexture.height);
				data.writeByte(zoomLevel);
			}
		}));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);

		if (getContainer().mapTexture == null) {
			initializeMap();
		}

		mapXMin = (width - getContainer().mapTexture.width) / 2;

		if (getContainer().mapTexture.height <= 200) {
			mapYMin = guiTop + 20;
		} else {
			mapYMin = (height - getContainer().mapTexture.height) / 2;
		}

		getContainer().mapTexture.drawMap(mapXMin, mapYMin, zLevel);

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

		if (getContainer().mapTexture.height <= 200) {
			drawBackgroundSlots();

			bindTexture(texture);
			drawTexturedModalRect(guiLeft + colorSelected.x, guiTop + colorSelected.y, 0, 220, 16, 16);
			drawTexturedModalRect(guiLeft + 236, guiTop + 38, 16, 220, 8,
					(int) ((zonePlan.progress / (float) TileZonePlan.CRAFT_TIME) * 27));
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int blocksX = (mouseX - mapXMin) * zoomLevel;
		int blocksZ = (mouseY - mapYMin) * zoomLevel;

		int blockStartX = cx - mapWidth * zoomLevel / 2;
		int blockStartZ = cz - mapHeight * zoomLevel / 2;

		boolean clickOnMap = mouseX >= mapXMin
				&& mouseX <= mapXMin + getContainer().mapTexture.width && mouseY >= mapYMin &&
				mouseY <= mapYMin + getContainer().mapTexture.height;

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
			AdvancedSlot slot = getSlotAtLocation(mouseX, mouseY);

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
				&& mouseX >= mapXMin && mouseX <= mapXMin + getContainer().mapTexture.width
				&& mouseY >= mapYMin && mouseY <= mapYMin + getContainer().mapTexture.height) {

			selX2 = mouseX;
			selY2 = mouseY;
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int eventType) {
		super.mouseReleased(mouseX, mouseY, eventType);

		if (eventType != -1 && inSelection) {
			boolean val = tool.displayString.equals("+");
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

					getContainer().currentAreaSelection.set(x, z, val);
				}
			}

			inSelection = false;
			getContainer().saveArea(colorSelected.color.ordinal());
			refreshSelectedArea();
		}
	}

	@Override
	protected void keyTyped(char carac, int val) throws IOException {
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

			getContainer().mapTexture = new BCDynamicTexture(mapWidth, mapHeight);
			getContainer().mapTexture.createDynamicTexture();

			currentSelection = new BCDynamicTexture(mapWidth, mapHeight);
			currentSelection.createDynamicTexture();

			uploadMap();
			refreshSelectedArea();
			container.inventorySlots = inventorySlots;
			buttonList = savedButtonList;
		} else if (carac == 'M') {
			mapWidth = this.mc.displayWidth;
			mapHeight = this.mc.displayHeight;

			getContainer().mapTexture = new BCDynamicTexture(mapWidth, mapHeight);
			getContainer().mapTexture.createDynamicTexture();

			currentSelection = new BCDynamicTexture(mapWidth, mapHeight);
			currentSelection.createDynamicTexture();

			uploadMap();
			refreshSelectedArea();
			container.inventorySlots = new LinkedList();
			buttonList = new LinkedList();
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

				r /= zoomLevel * zoomLevel;
				g /= zoomLevel * zoomLevel;
				b /= zoomLevel * zoomLevel;

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
	public ContainerZonePlan getContainer() {
		return (ContainerZonePlan) super.getContainer();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button == tool) {
			if (tool.displayString.equals("+")) {
				tool.displayString = "-";
				tool.getToolTip().remove(0);
				tool.getToolTip().add(new ToolTipLine(StringUtils.localize("tip.tool.remove")));
			} else {
				tool.displayString = "+";
				tool.getToolTip().remove(0);
				tool.getToolTip().add(new ToolTipLine(StringUtils.localize("tip.tool.add")));
			}
		}
	}
}
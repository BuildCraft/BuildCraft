/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.gui;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import io.netty.buffer.ByteBuf;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.EnumColor;
import buildcraft.core.DefaultProps;
import buildcraft.core.lib.gui.AdvancedSlot;
import buildcraft.core.lib.gui.GuiAdvancedInterface;
import buildcraft.core.lib.gui.buttons.GuiBetterButton;
import buildcraft.core.lib.gui.buttons.StandardButtonTextureSets;
import buildcraft.core.lib.gui.tooltips.ToolTip;
import buildcraft.core.lib.gui.tooltips.ToolTipLine;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.render.DynamicTextureBC;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.robotics.TileZonePlan;
import buildcraft.robotics.ZonePlan;

public class GuiZonePlan extends GuiAdvancedInterface {
	public static final int WINDOWED_MAP_WIDTH = 213;
	public static final int WINDOWED_MAP_HEIGHT = 100;

	private static final ResourceLocation TMP_TEXTURE = new ResourceLocation("buildcraftrobotics:textures/gui/zone_planner_gui.png");

	private int mapWidth = WINDOWED_MAP_WIDTH;
	private int mapHeight = WINDOWED_MAP_HEIGHT;

	private TileZonePlan zonePlan;

	private DynamicTextureBC newSelection;
	private int selX1 = 0;
	private int selX2 = 0;
	private int selY1 = 0;
	private int selY2 = 0;

	private boolean inSelection = false;

	private DynamicTextureBC currentSelection;

	private int mapXMin = 0;
	private int mapYMin = 0;

	private float blocksPerPixel = 1.0f;
	private int cx;
	private int cz;

	private AreaSlot colorSelected = null;

	private float alpha = 0.8F;

	private GuiBetterButton tool, fsButton;

	private List<GuiBetterButton> savedButtonList;

	private GuiTextField textField;

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

	public GuiZonePlan(IInventory inventory, TileZonePlan iZonePlan) {
		super(new ContainerZonePlan(inventory, iZonePlan), inventory, TMP_TEXTURE);

		getContainer().gui = this;

		xSize = 256;
		ySize = 228;

		zonePlan = iZonePlan;

		getContainer().mapTexture = new DynamicTextureBC(mapWidth, mapHeight);
		currentSelection = new DynamicTextureBC(mapWidth, mapHeight);
		newSelection = new DynamicTextureBC(1, 1);

		getContainer().currentAreaSelection = new ZonePlan();

		cx = zonePlan.xCoord;
		cz = zonePlan.zCoord;

		resetNullSlots(16);

		for (int i = 0; i < 4; ++i) {
			for (int j = 0; j < 4; ++j) {
				slots.set(i * 4 + j, new AreaSlot(this, 8 + 18 * i, 146 + 18 * j, EnumColor.values()[i * 4 + j]));
			}
		}

		colorSelected = (AreaSlot) slots.get(0);

		newSelection.setColor(0, 0, colorSelected.color.getDarkHex(), alpha);

		uploadMap();
		getContainer().loadArea(colorSelected.color.ordinal());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		tool = new GuiBetterButton(0, guiLeft + 27, guiTop + 111, 15, StandardButtonTextureSets.SMALL_BUTTON, "+");
		tool.setToolTip(new ToolTip(new ToolTipLine(StringUtils.localize("tip.tool.add"))));
		buttonList.add(tool);
		fsButton = new GuiBetterButton(1, guiLeft + 44, guiTop + 111, 20, StandardButtonTextureSets.SMALL_BUTTON, "FS");
		fsButton.setToolTip(new ToolTip(new ToolTipLine(StringUtils.localize("tip.tool.fullscreen"))));
		buttonList.add(fsButton);

		savedButtonList = buttonList;

		textField = new GuiTextField(this.fontRendererObj, 28, 129, 156, 12);
		textField.setMaxStringLength(DefaultProps.MAX_NAME_SIZE);
		textField.setText(zonePlan.mapName);
		textField.setFocused(true);
	}

	private void uploadMap() {
		BuildCraftCore.instance.sendToServer(new PacketCommand(getContainer(), "computeMap", new CommandWriter() {
			public void write(ByteBuf data) {
				data.writeInt(cx);
				data.writeInt(cz);
				data.writeShort(getContainer().mapTexture.width);
				data.writeShort(getContainer().mapTexture.height);
				data.writeFloat(blocksPerPixel);
			}
		}));
	}

	private boolean isFullscreen() {
		return getContainer().mapTexture.height > WINDOWED_MAP_HEIGHT;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);

		if (getContainer().mapTexture.width <= WINDOWED_MAP_WIDTH) {
			mapXMin = guiLeft + 8 + ((WINDOWED_MAP_WIDTH - getContainer().mapTexture.width) / 2);
		} else {
			mapXMin = (width - getContainer().mapTexture.width) / 2;
		}

		if (getContainer().mapTexture.height <= WINDOWED_MAP_HEIGHT) {
			mapYMin = guiTop + 9 + ((WINDOWED_MAP_HEIGHT - getContainer().mapTexture.height) / 2);
		} else {
			mapYMin = (height - getContainer().mapTexture.height) / 2;
		}

		getContainer().mapTexture.draw(mapXMin, mapYMin, zLevel);

		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_BLEND);

		currentSelection.draw(mapXMin, mapYMin, zLevel);

		GL11.glPopAttrib();
		GL11.glDisable(GL11.GL_BLEND);

		newSelection.updateTexture();

		if (inSelection && selX2 != 0) {
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_BLEND);

			int x1 = selX1 < selX2 ? selX1 : selX2;
			int x2 = selX1 < selX2 ? selX2 : selX1;
			int y1 = selY1 < selY2 ? selY1 : selY2;
			int y2 = selY1 < selY2 ? selY2 : selY1;

			drawTexturedModalRect(x1, y1, 0, 0, x2 - x1 + 1, y2 - y1 + 1);
			GL11.glPopAttrib();
			GL11.glDisable(GL11.GL_BLEND);
		}

		if (!isFullscreen()) {
			drawBackgroundSlots(x, y);

			bindTexture(texture);

			GL11.glEnable(GL11.GL_ALPHA_TEST);
			drawTexturedModalRect(guiLeft + colorSelected.x, guiTop + colorSelected.y, 0, 228, 16, 16);
			drawTexturedModalRect(guiLeft + 236, guiTop + 27, 16, 228, 8,
					(int) ((zonePlan.progress / (float) TileZonePlan.CRAFT_TIME) * 27));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		if (!isFullscreen()) {
			textField.drawTextBox();
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		int blocksX = Math.round((mouseX - mapXMin) * blocksPerPixel);
		int blocksZ = Math.round((mouseY - mapYMin) * blocksPerPixel);

		int blockStartX = Math.round(cx - mapWidth * blocksPerPixel / 2);
		int blockStartZ = Math.round(cz - mapHeight * blocksPerPixel / 2);

		boolean clickOnMap = mouseX >= mapXMin
				&& mouseX <= mapXMin + getContainer().mapTexture.width && mouseY >= mapYMin &&
				mouseY <= mapYMin + getContainer().mapTexture.height;

		if (clickOnMap) {
			if (mouseButton == 1) {
				cx = blockStartX + blocksX;
				cz = blockStartZ + blocksZ;

				uploadMap();
				refreshSelectedArea();
				return;
			} else {
				inSelection = true;
				selX1 = mouseX;
				selY1 = mouseY;
				selX2 = 0;
				selY2 = 0;
				return;
			}
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
		textField.mouseClicked(mouseX - guiLeft, mouseY - guiTop, mouseButton);

		AdvancedSlot slot = getSlotAtLocation(mouseX, mouseY);

		if (slot instanceof AreaSlot) {
			colorSelected = (AreaSlot) slot;

			newSelection.setColor(0, 0, colorSelected.color.getDarkHex(), alpha);
			getContainer().loadArea(colorSelected.color.ordinal());
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
	protected void mouseMovedOrUp(int mouseX, int mouseY, int eventType) {
		super.mouseMovedOrUp(mouseX, mouseY, eventType);

		if (eventType != -1 && inSelection) {
			boolean val = tool.displayString.equals("+");
			int blockStartX = Math.round(cx - mapWidth * blocksPerPixel / 2);
			int blockStartZ = Math.round(cz - mapHeight * blocksPerPixel / 2);

			int x1 = selX1 < selX2 ? selX1 : selX2;
			int x2 = selX1 < selX2 ? selX2 : selX1;
			int y1 = selY1 < selY2 ? selY1 : selY2;
			int y2 = selY1 < selY2 ? selY2 : selY1;

			int lengthX = Math.round((x2 - x1) * blocksPerPixel);
			int lengthY = Math.round((y2 - y1) * blocksPerPixel);

			for (int i = 0; i <= lengthX; ++i) {
				for (int j = 0; j <= lengthY; ++j) {
					int x = Math.round(blockStartX + (x1 - mapXMin) * blocksPerPixel) + i;
					int z = Math.round(blockStartZ + (y1 - mapYMin) * blocksPerPixel) + j;

					getContainer().currentAreaSelection.set(x, z, val);
				}
			}

			inSelection = false;
			getContainer().saveArea(colorSelected.color.ordinal());
			refreshSelectedArea();
		}
	}

	private void toFullscreen() {
		if (isFullscreen()) {
			return;
		}

		if (blocksPerPixel > 4.0f) {
			blocksPerPixel = 4.0f;
		}

		mapWidth = this.mc.displayWidth;
		mapHeight = this.mc.displayHeight;

		getContainer().mapTexture = new DynamicTextureBC(mapWidth, mapHeight);
		currentSelection = new DynamicTextureBC(mapWidth, mapHeight);

		for (Slot s : (List<Slot>) container.inventorySlots) {
			s.xDisplayPosition += 1048576;
			s.yDisplayPosition += 1048576;
		}

		uploadMap();
		refreshSelectedArea();

		buttonList = new LinkedList<GuiBetterButton>();
	}

	private void toWindowed() {
		if (!isFullscreen()) {
			return;
		}

		mapWidth = WINDOWED_MAP_WIDTH;
		mapHeight = WINDOWED_MAP_HEIGHT;

		getContainer().mapTexture = new DynamicTextureBC(mapWidth, mapHeight);
		currentSelection = new DynamicTextureBC(mapWidth, mapHeight);

		for (Slot s : (List<Slot>) container.inventorySlots) {
			s.xDisplayPosition -= 1048576;
			s.yDisplayPosition -= 1048576;
		}

		uploadMap();
		refreshSelectedArea();

		buttonList = savedButtonList;
	}

	private boolean incBlocksPerPixel() {
		if (blocksPerPixel > 0.125f) {
			if (blocksPerPixel <= 1.0f) {
				blocksPerPixel /= 2;
			} else {
				blocksPerPixel--;
			}
			return true;
		}
		return false;
	}

	private boolean decBlocksPerPixel() {
		if ((isFullscreen() && blocksPerPixel < 4.0f) || (!isFullscreen() && blocksPerPixel < 8.0f)) {
			if (blocksPerPixel >= 1.0f) {
				blocksPerPixel++;
			} else {
				blocksPerPixel *= 2;
			}
			return true;
		}
		return false;
	}

	@Override
	protected void keyTyped(char carac, int val) {
		if (!isFullscreen() && textField.isFocused()) {
			if (carac == 13 || carac == 27) {
				textField.setFocused(false);
			} else {
				textField.textboxKeyTyped(carac, val);
				final String text = textField.getText();
				BuildCraftCore.instance.sendToServer(new PacketCommand(getContainer(), "setName", new CommandWriter() {
					public void write(ByteBuf data) {
						NetworkUtils.writeUTF(data, text);
					}
				}));
			}
			return;
		} else if (val == Keyboard.KEY_F5) {
			uploadMap();
			refreshSelectedArea();
		} else if (carac == '+' && incBlocksPerPixel()) {
			uploadMap();
			refreshSelectedArea();
		} else if (carac == '-' && decBlocksPerPixel()) {
			uploadMap();
			refreshSelectedArea();
		} else if (carac == 'm' || (carac == 27 && isFullscreen())) {
			toWindowed();
		} else if (carac == 'M') {
			toFullscreen();
		} else {
			super.keyTyped(carac, val);
		}
	}

	public void refreshSelectedArea() {
		int color = colorSelected.color.getDarkHex();

		int rAdd = (color >> 16) & 255;
		int gAdd = (color >> 8) & 255;
		int bAdd = color & 255;

		for (int i = 0; i < currentSelection.width; ++i) {
			for (int j = 0; j < currentSelection.height; ++j) {
				int blockStartX = Math.round(cx - mapWidth * blocksPerPixel / 2);
				int blockStartZ = Math.round(cz - mapHeight * blocksPerPixel / 2);
				int c = (int) Math.ceil(blocksPerPixel);

				double r = 0;
				double g = 0;
				double b = 0;

				for (int stepi = 0; stepi < c; ++stepi) {
					for (int stepj = 0; stepj < c; ++stepj) {
						int x = Math.round(blockStartX + i * blocksPerPixel) + stepi;
						int z = Math.round(blockStartZ + j * blocksPerPixel) + stepj;

						if (getContainer().currentAreaSelection.get(x, z)) {
							r += rAdd;
							g += gAdd;
							b += bAdd;
						}
					}
				}

				r /= c * c;
				g /= c * c;
				b /= c * c;

				if (r != 0) {
					currentSelection.setColori(i, j, (int) r, (int) g, (int) b, (int) (alpha * 255.0F));
				} else {
					currentSelection.setColori(i, j, 0, 0, 0, 0);
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
		} else if (button == fsButton) {
			toFullscreen();
		}
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();

		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		if (mouseX >= mapXMin && mouseX <= mapXMin + getContainer().mapTexture.width
				&& mouseY >= mapYMin && mouseY <= mapYMin + getContainer().mapTexture.height) {
			int wheel = Mouse.getEventDWheel();
			if (wheel != 0) {
				if (wheel > 0 && decBlocksPerPixel()) {
					uploadMap();
					refreshSelectedArea();
				} else if (wheel < 0 && incBlocksPerPixel()) {
					uploadMap();
					refreshSelectedArea();
				}
			}
		}
	}
}
/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.gui;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.builders.network.PacketLibraryAction;
import buildcraft.core.DefaultProps;
import buildcraft.core.blueprints.BptPlayerIndex;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketPayloadArrays;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.StringUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiBlueprintLibrary extends GuiBuildCraft {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/template_gui.png");
	EntityPlayer player;
	TileBlueprintLibrary library;
	ContainerBlueprintLibrary container;
	boolean computeInput;
	BptPlayerIndex index;

	public GuiBlueprintLibrary(EntityPlayer player, TileBlueprintLibrary library) {
		super(new ContainerBlueprintLibrary(player, library), library, TEXTURE);
		this.player = player;
		xSize = 176;
		ySize = 225;

		this.library = library;
		container = (ContainerBlueprintLibrary) inventorySlots;

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

		buttonList.add(prevPageButton);
		buttonList.add(nextPageButton);

		// if (library.owner.equals(player.username)) {
		deleteButton = new GuiButton(2, j + 100, k + 114, 25, 20, StringUtils.localize("gui.del"));
		buttonList.add(deleteButton);

		lockButton = new GuiButton(3, j + 127, k + 114, 40, 20, StringUtils.localize("gui.lock"));
		buttonList.add(lockButton);
		if (library.locked) {
			lockButton.displayString = StringUtils.localize("gui.unlock");
		} else {
			lockButton.displayString = StringUtils.localize("gui.lock");
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		// fontRenderer.drawString(library.owner + "'s Library", 6, 6,
		// 0x404040);
		String title = StringUtils.localize("tile.libraryBlock");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);

		int c = 0;
		String[] currentNames = library.currentNames;
		for (int i = 0; i < currentNames.length; i++) {
			String name = currentNames[i];
			if (name == null) {
				break;
			}
			if (name.length() > BuildCraftBuilders.MAX_BLUEPRINTS_NAME_SIZE) {
				name = name.substring(0, BuildCraftBuilders.MAX_BLUEPRINTS_NAME_SIZE);
			}

			if (i == library.selected) {
				int l1 = 8;
				int i2 = 24;
				drawGradientRect(l1, i2 + 9 * c, l1 + 88, i2 + 9 * (c + 1), 0x80ffffff, 0x80ffffff);
			}

			fontRenderer.drawString(name, 9, 25 + 9 * c, 0x404040);
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

		drawTexturedModalRect(j + 128 + 22 - inP, k + 61, 176 + 22 - inP, 16, inP, 16);
		drawTexturedModalRect(j + 128, k + 78, 176, 0, outP, 16);
	}

	@Override
	public void updateScreen() {
		if (library.locked) {
			lockButton.displayString = StringUtils.localize("gui.unlock");
		} else {
			lockButton.displayString = StringUtils.localize("gui.lock");
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		PacketLibraryAction packet = new PacketLibraryAction(PacketIds.LIBRARY_ACTION, library.xCoord, library.yCoord, library.zCoord);
		if (button == nextPageButton) {
			packet.actionId = TileBlueprintLibrary.COMMAND_NEXT;
		} else if (button == prevPageButton) {
			packet.actionId = TileBlueprintLibrary.COMMAND_PREV;
		} else if (lockButton != null && button == lockButton) {
			packet.actionId = TileBlueprintLibrary.COMMAND_LOCK_UPDATE;
		} else if (deleteButton != null && button == deleteButton) {
			packet.actionId = TileBlueprintLibrary.COMMAND_DELETE;
		}
		CoreProxy.proxy.sendToServer(packet.getPacket());
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
				if (ySlot < library.currentNames.length) {
					PacketPayloadArrays payload = new PacketPayloadArrays();
					payload.intPayload = new int[]{ySlot};
					PacketLibraryAction packet = new PacketLibraryAction(PacketIds.LIBRARY_SELECT, library.xCoord, library.yCoord, library.zCoord);
					packet.actionId = ySlot;
					CoreProxy.proxy.sendToServer(packet.getPacket());
				}
			}
		}
	}
}

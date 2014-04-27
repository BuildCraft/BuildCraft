/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.gui.buttons.GuiImageButton;
import buildcraft.core.gui.buttons.IButtonClickEventListener;
import buildcraft.core.gui.buttons.IButtonClickEventTrigger;
import buildcraft.core.network.PacketGuiReturn;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.pipes.PipeItemsEmerald;
import buildcraft.transport.pipes.PipeItemsEmerald.FilterMode;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiEmeraldPipe extends GuiBuildCraft implements IButtonClickEventListener {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/pipe_emerald.png");

	private static final int WHITE_LIST_BUTTON_ID = 1;
	private static final int BLACK_LIST_BUTTON_ID = 2;
	private static final int ROUND_ROBIN_BUTTON_ID = 3;

	private GuiImageButton whiteListButton;
	private GuiImageButton blackListButton;
	private GuiImageButton roundRobinButton;

	IInventory playerInventory;
	IInventory filterInventory;

	PipeItemsEmerald pipe;

	public GuiEmeraldPipe(IInventory playerInventory, PipeItemsEmerald pipe) {
		super(new ContainerEmeraldPipe(playerInventory, pipe), pipe.getFilters(), TEXTURE);

		this.pipe = pipe;

		this.playerInventory = playerInventory;
		this.filterInventory = pipe.getFilters();

		xSize = 175;
		ySize = 161;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.buttonList.clear();

		this.whiteListButton = new GuiImageButton(WHITE_LIST_BUTTON_ID, this.guiLeft + 7, this.guiTop + 41, GuiImageButton.ButtonImage.WHITE_LIST);
		this.whiteListButton.registerListener(this);
		this.buttonList.add(this.whiteListButton);

		this.blackListButton = new GuiImageButton(BLACK_LIST_BUTTON_ID, this.guiLeft + 7 + 18, this.guiTop + 41, GuiImageButton.ButtonImage.BLACK_LIST);
		this.blackListButton.registerListener(this);
		this.buttonList.add(this.blackListButton);

		this.roundRobinButton = new GuiImageButton(ROUND_ROBIN_BUTTON_ID, this.guiLeft + 7 + 36, this.guiTop + 41, GuiImageButton.ButtonImage.ROUND_ROBIN);
		this.roundRobinButton.registerListener(this);
		this.buttonList.add(this.roundRobinButton);

		switch (pipe.getSettings().getFilterMode()) {
			case WHITE_LIST:
				this.whiteListButton.Activate();
				break;
			case BLACK_LIST:
				this.blackListButton.Activate();
				break;
			case ROUND_ROBIN:
				this.roundRobinButton.Activate();
				break;
		}
	}

	@Override
	public void onGuiClosed() {
		if (pipe.getWorld().isRemote) {
			PacketGuiReturn pkt = new PacketGuiReturn(pipe.getContainer());
			pkt.sendPacket();
		}

		super.onGuiClosed();
	}

	@Override
	public void handleButtonClick(IButtonClickEventTrigger sender, int buttonId) {
		switch (buttonId) {
			case WHITE_LIST_BUTTON_ID:
				whiteListButton.Activate();
				blackListButton.DeActivate();
				roundRobinButton.DeActivate();

				pipe.getSettings().setFilterMode(FilterMode.WHITE_LIST);
				break;
			case BLACK_LIST_BUTTON_ID:
				whiteListButton.DeActivate();
				blackListButton.Activate();
				roundRobinButton.DeActivate();

				pipe.getSettings().setFilterMode(FilterMode.BLACK_LIST);
				break;
			case ROUND_ROBIN_BUTTON_ID:
				whiteListButton.DeActivate();
				blackListButton.DeActivate();
				roundRobinButton.Activate();

				pipe.getSettings().setFilterMode(FilterMode.ROUND_ROBIN);
				break;
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("gui.pipes.emerald.title");

		fontRendererObj.drawString(title, (xSize - fontRendererObj.getStringWidth(title)) / 2, 6, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 93, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
}

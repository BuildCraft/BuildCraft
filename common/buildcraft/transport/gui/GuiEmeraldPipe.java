/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.gui.buttons.GuiMultiButton;
import buildcraft.core.network.PacketGuiReturn;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.pipes.PipeItemsEmerald;

public class GuiEmeraldPipe extends GuiBuildCraft {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/filter_2.png");
	private GuiMultiButton button;

	IInventory playerInventory;
	IInventory filterInventory;
	PipeItemsEmerald pipe;

	public GuiEmeraldPipe(IInventory playerInventory, PipeItemsEmerald pipe) {
		super(new ContainerEmeraldPipe(playerInventory, pipe), pipe.getFilters(), TEXTURE);

		this.pipe = pipe;
		this.playerInventory = playerInventory;
		this.filterInventory = pipe.getFilters();

		xSize = 175;
		ySize = 132;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.buttonList.clear();
		this.button = new GuiMultiButton(0, this.guiLeft + this.xSize - (80 + 6), this.guiTop + 34, 80, this.pipe.getStateController().copy());
		this.buttonList.add(this.button);
	}

	@Override
	public void onGuiClosed() {
		if (CoreProxy.proxy.isRenderWorld(pipe.getWorld())) {
			pipe.getStateController().setCurrentState(button.getController().getCurrentState());
			PacketGuiReturn pkt = new PacketGuiReturn(pipe.getContainer());
			pkt.sendPacket();
		}

		super.onGuiClosed();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString(filterInventory.getInvName(), getCenteredOffset(filterInventory.getInvName()), 6, 0x404040);
		fontRenderer.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 93, 0x404040);
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

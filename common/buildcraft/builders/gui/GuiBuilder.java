/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.gui;

import java.util.Collection;

import org.lwjgl.opengl.GL11;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftCore;
import buildcraft.builders.TileBuilder;
import buildcraft.core.DefaultProps;
import buildcraft.core.fluids.Tank;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.gui.ItemSlot;
import buildcraft.core.network.CommandWriter;
import buildcraft.core.network.PacketCommand;
import buildcraft.core.utils.StringUtils;

public class GuiBuilder extends GuiAdvancedInterface {
	private static final ResourceLocation BLUEPRINT_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/builder_blueprint.png");
	private static final ResourceLocation FOREGROUND_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/builder_foreground.png");
	private IInventory playerInventory;
	private TileBuilder builder;
	private GuiButton selectedButton;

	public GuiBuilder(IInventory playerInventory, TileBuilder builder) {
		super(new ContainerBuilder(playerInventory, builder), builder, BLUEPRINT_TEXTURE);
		this.playerInventory = playerInventory;
		this.builder = builder;
		xSize = 176;
		ySize = 225;

		resetNullSlots(6 * 4);

		for (int i = 0; i < 6; ++i) {
			for (int j = 0; j < 4; ++j) {
				slots.set(i * 4 + j, new ItemSlot(this, 179 + j * 18, 18 + i * 18));
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		drawCenteredString(StringUtils.localize("tile.builderBlock.name"), 178 / 2, 16, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.building.resources"), 8, 60, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.needed"), 178, 7, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.building.fluids"), 178, 133, 0x404040);

		drawTooltipForSlotAt(par1, par2);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		mc.renderEngine.bindTexture(BLUEPRINT_TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, 256, ySize);

		for (AdvancedSlot s : slots) {
			((ItemSlot) s).stack = null;
		}

		Collection<ItemStack> needs = builder.getNeededItems();

		if (needs != null) {
			int s = 0;

			for (ItemStack stack : needs) {
				if (s >= slots.size()) {
					break;
				}

				((ItemSlot) slots.get(s)).stack = stack.copy();
				s++;
			}
		}

		drawBackgroundSlots();

		for (int i = 0; i < builder.fluidTanks.length; i++) {
			Tank tank = builder.fluidTanks[i];
			drawFluid(tank.getFluid(), guiLeft + 179 + 18 * i, guiTop + 145, 16, 47, tank.getCapacity());
		}
		mc.renderEngine.bindTexture(FOREGROUND_TEXTURE);
		for (int i = 0; i < builder.fluidTanks.length; i++) {
			drawTexturedModalRect(guiLeft + 179 + 18 * i, guiTop + 145, 0, 54, 16, 47);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		for (int i = 0; i < 4; i++) {
			buttonList.add(new BuilderEraseButton(i, guiLeft + 178 + 18 * i, guiTop + 197, 18, 18));
		}
	}

	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int eventType) {
		super.mouseMovedOrUp(mouseX, mouseY, eventType);

		if (this.selectedButton != null && eventType == 0) {
			this.selectedButton.mouseReleased(mouseX, mouseY);
			this.selectedButton = null;
		}
	}

	private class BuilderEraseButton extends GuiButton {
		private boolean clicked;

		public BuilderEraseButton(int id, int x, int y, int width, int height) {
			super(id, x, y, width, height, null);
		}

		@Override
		public boolean mousePressed(Minecraft mc, int x, int y) {
			if (super.mousePressed(mc, x, y)) {
				selectedButton = this;
				clicked = true;
				BuildCraftCore.instance.sendToServer(new PacketCommand(builder, "eraseFluidTank", new CommandWriter() {
					public void write(ByteBuf data) {
						data.writeInt(id);
					}
				}));
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void mouseReleased(int x, int y) {
			super.mouseReleased(x, y);
			clicked = false;
		}

		@Override
		public void drawButton(Minecraft mc, int x, int y) {
			this.hovered = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;

			mc.renderEngine.bindTexture(FOREGROUND_TEXTURE);
			drawTexturedModalRect(xPosition, yPosition, 0, (clicked ? 1 : this.hovered ? 2 : 0) * 18, 18, 18);
			mouseDragged(mc, x, y);
		}
	}
}

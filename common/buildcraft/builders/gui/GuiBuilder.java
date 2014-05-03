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

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.builders.TileBuilder;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.utils.StringUtils;

public class GuiBuilder extends GuiAdvancedInterface {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/builder.png");
	private static final ResourceLocation BLUEPRINT_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/builder_blueprint.png");
	private IInventory playerInventory;
	private TileBuilder builder;
	private int inventoryRows = 6;

	public GuiBuilder(IInventory playerInventory, TileBuilder builder) {
		super(new ContainerBuilder(playerInventory, builder), builder, TEXTURE);
		this.playerInventory = playerInventory;
		this.builder = builder;
		xSize = 176;
		ySize = 225;

		slots = new AdvancedSlot[7 * 4];

		for (int i = 0; i < 7; ++i) {
			for (int j = 0; j < 4; ++j) {
				slots[i * 4 + j] = new ItemSlot(179 + j * 18, 18 + i * 18);
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		String title = StringUtils.localize("tile.builderBlock.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 12, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.building.resources"), 8, 60, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.needed"), 185, 7, 0x404040);

		drawForegroundSelection(par1, par2);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		int realXSize = 0;

//		if (builder.isBuildingBlueprint()) {
			mc.renderEngine.bindTexture(BLUEPRINT_TEXTURE);
			realXSize = 256;
//		} else {
//			mc.renderEngine.bindTexture(TEXTURE);
//			realXSize = 176;
//		}

		drawTexturedModalRect(j, k, 0, 0, realXSize, ySize);

		for (int s = 0; s < slots.length; ++s) {
			((ItemSlot) slots[s]).stack = null;
		}

		Collection<ItemStack> needs = builder.getNeededItems();

		if (needs != null) {
			int s = 0;

			for (ItemStack stack : needs) {
				if (s >= slots.length) {
					break;
				}

				((ItemSlot) slots[s]).stack = stack.copy();
				s++;
			}
		}

		drawBackgroundSlots();
	}
}

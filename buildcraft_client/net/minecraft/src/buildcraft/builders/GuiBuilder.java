/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import java.util.Collection;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.core.GuiAdvancedInterface;

import org.lwjgl.opengl.GL11;

public class GuiBuilder extends GuiAdvancedInterface {

	IInventory playerInventory;
	TileBuilder builder;

	public GuiBuilder(IInventory playerInventory, TileBuilder builder) {
		super(new CraftingBuilder(playerInventory, builder));
		this.playerInventory = playerInventory;
		this.builder = builder;
		xSize = 176;
		ySize = 225;

		slots = new AdvancedSlot [7 * 4];

		for (int i = 0; i < 7; ++i)
			for (int j = 0; j < 4; ++j)
				slots [i * 4 + j] = new ItemSlot (179 + j * 18, 18 + i * 18);
	}

	@Override
    protected void drawGuiContainerForegroundLayer() {
    	super.drawGuiContainerForegroundLayer ();

        fontRenderer.drawString("Builder", 73, 12, 0x404040);
        fontRenderer.drawString("Building Resources", 8, 60, 0x404040);
        fontRenderer.drawString("Inventory", 8, ySize - 97, 0x404040);

        if (builder.isBuildingBlueprint())
			fontRenderer.drawString("Needed", 185, 7, 0x404040);

        drawForegroundSelection();
    }

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = 0;
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		int realXSize = 0;

		if (builder.isBuildingBlueprint()) {
			i = mc.renderEngine.getTexture("/net/minecraft/src/buildcraft/builders/gui/builder_blueprint.png");
			realXSize = 256;
		} else {
			i = mc.renderEngine.getTexture("/net/minecraft/src/buildcraft/builders/gui/builder.png");
			realXSize = 176;
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);

		drawTexturedModalRect(j, k, 0, 0, realXSize, ySize);

		for (int s = 0; s < slots.length; ++s)
			((ItemSlot) slots [s]).stack = null;

        Collection <ItemStack> needs = builder.getNeededItems();

        if (needs != null) {
        	int s = 0;

        	for (ItemStack stack : needs) {
        		if (s >= slots.length)
					break;

        		((ItemSlot) slots [s]).stack = stack.copy();
        		s++;
        	}
        }


		drawBackgroundSlots();
	}

	int inventoryRows = 6;
}

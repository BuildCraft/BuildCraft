/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.core.lib.utils.StringUtils;
import buildcraft.energy.TileEngineIron;

public class GuiCombustionEngine extends GuiEngine {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftenergy:textures/gui/combustion_engine_gui.png");

	public GuiCombustionEngine(InventoryPlayer inventoryplayer, TileEngineIron tileEngine) {
		super(new ContainerEngine(inventoryplayer, tileEngine), tileEngine, TEXTURE);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		String title = StringUtils.localize("tile.engineIron.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);

		TileEngineIron engine = (TileEngineIron) tile;
		FluidStack stack = null;

		if (engine != null) {
			if (par2 >= (guiTop + 19) && par2 < (guiTop + 19 + 60)) {
				if (par1 >= (guiLeft + 104) && par1 < (guiLeft + 104 + 16)) {
					stack = engine.getFuel();
				} else if (par1 >= (guiLeft + 122) && par1 < (guiLeft + 122 + 16)) {
					stack = engine.getCoolant();
				}
			}
		}

		if (stack != null && stack.amount > 0) {
			List<String> fluidTip = new ArrayList<String>();
			fluidTip.add(stack.getLocalizedName());

			drawHoveringText(fluidTip, par1 - guiLeft, par2 - guiTop, fontRendererObj);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);
		TileEngineIron engine = (TileEngineIron) tile;
		if (engine != null) {
			drawFluid(engine.getFuel(), guiLeft + 104, guiTop + 19, 16, 58, TileEngineIron.MAX_LIQUID);
			drawFluid(engine.getCoolant(), guiLeft + 122, guiTop + 19, 16, 58, TileEngineIron.MAX_LIQUID);
		}
		mc.renderEngine.bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft + 104, guiTop + 19, 176, 0, 16, 60);
		drawTexturedModalRect(guiLeft + 122, guiTop + 19, 176, 0, 16, 60);
	}
}

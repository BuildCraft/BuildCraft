/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy.gui;

import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.Icon;
import net.minecraftforge.liquids.LiquidStack;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import buildcraft.core.utils.StringUtils;
import buildcraft.energy.EngineIron;
import buildcraft.energy.TileEngine;

public class GuiCombustionEngine extends GuiEngine {

	public GuiCombustionEngine(InventoryPlayer inventoryplayer, TileEngine tileEngine) {
		super(new ContainerEngine(inventoryplayer, tileEngine), tileEngine);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		String title = StringUtils.localize("tile.engineIron");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRenderer.drawString(StringUtils.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(DefaultProps.TEXTURE_PATH_GUI + "/combustion_engine_gui.png");
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		TileEngine engine = (TileEngine) tile;
		EngineIron engineIron = ((EngineIron) engine.engine);

		if (engine.getScaledBurnTime(58) > 0) {
			displayGauge(j, k, 19, 104, engine.getScaledBurnTime(58), engineIron.getFuel());
		}

		if (engineIron.getScaledCoolant(58) > 0) {
			displayGauge(j, k, 19, 122, engineIron.getScaledCoolant(58), engineIron.getCoolant());
		}
	}

	private void displayGauge(int j, int k, int line, int col, int squaled, LiquidStack liquid) {
		if (liquid == null)
		{
			return;
		}
		int start = 0;

		Icon liquidIcon;
		String textureSheet;
		if(liquid.canonical() != null && liquid.canonical().getRenderingIcon() != null) {
			textureSheet = liquid.canonical().getTextureSheet();
			liquidIcon = liquid.canonical().getRenderingIcon();
		} else {
			if (liquid.itemID < Block.blocksList.length && Block.blocksList[liquid.itemID].blockID > 0) {
				liquidIcon = Block.blocksList[liquid.itemID].getBlockTextureFromSide(0);
				textureSheet = "/terrain.png";
			} else {
				liquidIcon = Item.itemsList[liquid.itemID].getIconFromDamage(liquid.itemMeta);
				textureSheet = "/gui/items.png";
			}
		}
		mc.renderEngine.bindTexture(textureSheet);

		while (true) {
			int x = 0;

			if (squaled > 16) {
				x = 16;
				squaled -= 16;
			} else {
				x = squaled;
				squaled = 0;
			}

			drawTexturedModelRectFromIcon(j + col, k + line + 58 - x - start, liquidIcon, 16, 16 - (16 - x));
			start = start + 16;

			if (x == 0 || squaled == 0) {
				break;
			}
		}

		mc.renderEngine.bindTexture(DefaultProps.TEXTURE_PATH_GUI + "/combustion_engine_gui.png");
		drawTexturedModalRect(j + col, k + line, 176, 0, 16, 60);
	}

}

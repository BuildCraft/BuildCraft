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
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import buildcraft.core.utils.StringUtil;
import buildcraft.energy.EngineIron;
import buildcraft.energy.TileEngine;

public class GuiCombustionEngine extends GuiEngine {

	public GuiCombustionEngine(InventoryPlayer inventoryplayer, TileEngine tileEngine) {
		super(new ContainerEngine(inventoryplayer, tileEngine), tileEngine);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		String title = StringUtil.localize("tile.engineIron");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRenderer.drawString(StringUtil.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture(DefaultProps.TEXTURE_PATH_GUI + "/combustion_engine_gui.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		TileEngine engine = (TileEngine) tile;
		EngineIron engineIron = ((EngineIron) engine.engine);

		if (engine.getScaledBurnTime(58) > 0) {
			displayGauge(j, k, 19, 104, engine.getScaledBurnTime(58), engineIron.getFuelId(), engineIron.getFuelMeta());
		}

		if (engineIron.getScaledCoolant(58) > 0) {
			displayGauge(j, k, 19, 122, engineIron.getScaledCoolant(58), engineIron.getCoolantId(), engineIron.getCoolantMeta());
		}
	}

	private void displayGauge(int j, int k, int line, int col, int squaled, int liquidId, int liquidMeta) {
		int liquidImgIndex = 0;

		if (liquidId <= 0)
			return;
		if (liquidId < Block.blocksList.length && Block.blocksList[liquidId] != null) {
			ForgeHooksClient.bindTexture(Block.blocksList[liquidId].getTextureFile(), 0);
			liquidImgIndex = Block.blocksList[liquidId].blockIndexInTexture;
		} else if (Item.itemsList[liquidId] != null) {
			ForgeHooksClient.bindTexture(Item.itemsList[liquidId].getTextureFile(), 0);
			liquidImgIndex = Item.itemsList[liquidId].getIconFromDamage(liquidMeta);
		} else
			return;

		int imgLine = liquidImgIndex / 16;
		int imgColumn = liquidImgIndex - imgLine * 16;

		int start = 0;

		while (true) {
			int x = 0;

			if (squaled > 16) {
				x = 16;
				squaled -= 16;
			} else {
				x = squaled;
				squaled = 0;
			}

			drawTexturedModalRect(j + col, k + line + 58 - x - start, imgColumn * 16, imgLine * 16 + (16 - x), 16, 16 - (16 - x));
			start = start + 16;

			if (x == 0 || squaled == 0) {
				break;
			}
		}

		int i = mc.renderEngine.getTexture(DefaultProps.TEXTURE_PATH_GUI + "/combustion_engine_gui.png");

		mc.renderEngine.bindTexture(i);
		drawTexturedModalRect(j + col, k + line, 176, 0, 16, 60);
	}
}

/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.core.DefaultProps;
import buildcraft.core.fluids.Tank;
import buildcraft.core.utils.StringUtils;
import buildcraft.energy.TileEngineIron;
import buildcraft.energy.TileEngineWithInventory;

public class GuiCombustionEngine extends GuiEngine {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/combustion_engine_gui.png");
	private static final ResourceLocation BLOCK_TEXTURE = TextureMap.locationBlocksTexture;

	public GuiCombustionEngine(InventoryPlayer inventoryplayer, TileEngineWithInventory tileEngine) {
		super(new ContainerEngine(inventoryplayer, tileEngine), tileEngine, TEXTURE);
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
		super.drawGuiContainerBackgroundLayer(f, x, y);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;

		TileEngineIron engine = (TileEngineIron) tile;

		if (engine.getScaledBurnTime(58) > 0) {
			displayGauge(j, k, 19, 104, engine.getScaledBurnTime(58), engine.getFuel(), engine.tankFuel);
		}

		if (engine.getScaledCoolant(58) > 0) {
			displayGauge(j, k, 19, 122, engine.getScaledCoolant(58), engine.getCoolant(), engine.tankCoolant);
		}
	}

	private void displayGauge(int j, int k, int line, int col, int squaled, FluidStack liquid, Tank tank) {
		if (liquid == null) {
			return;
		}
		int start = 0;

		Icon liquidIcon = null;
		Fluid fluid = liquid.getFluid();
		int color = tank.colorRenderCache;
		if (fluid != null && fluid.getStillIcon() != null) {
			liquidIcon = fluid.getStillIcon();
		}
		mc.renderEngine.bindTexture(BLOCK_TEXTURE);
		float red = (float) (color >> 16 & 255) / 255.0F;
		float green = (float) (color >> 8 & 255) / 255.0F;
		float blue = (float) (color & 255) / 255.0F;
		GL11.glColor4f(red, green, blue, 1.0F);
		
		if (liquidIcon != null) {
			while (true) {
				int x;

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
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		drawTexturedModalRect(j + col, k + line, 176, 0, 16, 60);
	}
}

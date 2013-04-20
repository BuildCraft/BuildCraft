/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory.render;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.liquids.LiquidStack;

import org.lwjgl.opengl.GL11;

import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderEntityBlock.BlockInterface;
import buildcraft.factory.TileTank;

public class RenderTank extends TileEntitySpecialRenderer {

	final static private int displayStages = 100;

	private final HashMap<LiquidStack, int[]> stage = new HashMap<LiquidStack, int[]>();

	private int[] getDisplayLists(LiquidStack liquid, World world) {

		if (stage.containsKey(liquid)) {
			return stage.get(liquid);
		}

		int[] d = new int[displayStages];
		stage.put(liquid, d);

		BlockInterface block = new BlockInterface();
		block.baseBlock = Block.waterStill;
		block.texture = liquid.getRenderingIcon();

		if (liquid.itemID < Block.blocksList.length && Block.blocksList[liquid.itemID] != null) {
			block.baseBlock = Block.blocksList[liquid.itemID];
		}

		for (int s = 0; s < displayStages; ++s) {
			d[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d[s], 4864 /* GL_COMPILE */);

			block.minX = 0.125 + 0.01;
			block.minY = 0;
			block.minZ = 0.125 + 0.01;

			block.maxX = 0.875 - 0.01;
			block.maxY = (float) s / (float) displayStages;
			block.maxZ = 0.875 - 0.01;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();
		}

		return d;
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {

		TileTank tank = ((TileTank) tileentity);

		LiquidStack liquid = tank.tank.getLiquid();
		if (liquid == null)
		    return;

		LiquidStack refLiquid = liquid.canonical();

		if (refLiquid == null || liquid.amount <= 0)
			return;

		int[] displayList = getDisplayLists(refLiquid, tileentity.worldObj);
		if (displayList == null)
			return;

		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);
		
       		bindTextureByName(refLiquid.getTextureSheet());

		GL11.glTranslatef((float) x, (float) y, (float) z);

		GL11.glCallList(displayList[(int) ((float) liquid.amount / (float) (tank.tank.getCapacity()) * (displayStages - 1))]);

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}
}

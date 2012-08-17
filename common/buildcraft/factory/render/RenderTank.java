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

import net.minecraft.src.Block;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.Item;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraft.src.World;

import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;

import buildcraft.api.liquids.LiquidStack;
import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderEntityBlock.BlockInterface;
import buildcraft.factory.TileTank;

public class RenderTank extends TileEntitySpecialRenderer {

	final static private int displayStages = 100;

	private HashMap<Integer, int[]> stage = new HashMap<Integer, int[]>();

	private int[] getDisplayLists(int liquidId, int damage, World world) {

		if (stage.containsKey(liquidId))
			return stage.get(liquidId);

		int[] d = new int[displayStages];
		stage.put(liquidId, d);

		BlockInterface block = new BlockInterface();
		if (liquidId < Block.blocksList.length && Block.blocksList[liquidId] != null)
			block.texture = Block.blocksList[liquidId].blockIndexInTexture;
		else
			block.texture = Item.itemsList[liquidId].getIconFromDamage(damage);

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

		if (liquid == null || liquid.amount <= 0
				|| liquid.itemID <= 0)
			return;

		int[] displayList = getDisplayLists(liquid.itemID, liquid.itemMeta, tileentity.worldObj);

		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);

		if (liquid.itemID < Block.blocksList.length && Block.blocksList[liquid.itemID] != null) {
			ForgeHooksClient.bindTexture(Block.blocksList[liquid.itemID].getTextureFile(), 0);
		} else {
			ForgeHooksClient.bindTexture(Item.itemsList[liquid.itemID].getTextureFile(), 0);
		}

		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

		GL11.glCallList(displayList[(int) ((float) liquid.amount / (float) (tank.tank.getCapacity()) * (displayStages - 1))]);

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}
}

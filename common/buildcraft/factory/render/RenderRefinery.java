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
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import buildcraft.core.IInventoryRenderer;
import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderEntityBlock.BlockInterface;
import buildcraft.factory.TileRefinery;

public class RenderRefinery extends TileEntitySpecialRenderer implements IInventoryRenderer {

	static final float factor = (float) (1.0 / 16.0);

	private ModelRenderer tank;
	private ModelRenderer magnet[] = new ModelRenderer[4];

	private ModelBase model = new ModelBase() {
	};

	public RenderRefinery() {

		// constructor:
		tank = new ModelRenderer(model, 0, 0);
		tank.addBox(-4F, -8F, -4F, 8, 16, 8);
		tank.rotationPointX = 8;
		tank.rotationPointY = 8;
		tank.rotationPointZ = 8;

		// constructor:

		for (int i = 0; i < 4; ++i) {
			magnet[i] = new ModelRenderer(model, 32, i * 8);
			magnet[i].addBox(0, -8F, -8F, 8, 4, 4);
			magnet[i].rotationPointX = 8;
			magnet[i].rotationPointY = 8;
			magnet[i].rotationPointZ = 8;

		}

	}

	final static private int displayStages = 100;

	private HashMap<Integer, HashMap<Integer, int[]>> stage = new HashMap<Integer, HashMap<Integer, int[]>>();

	private int[] getDisplayLists(int liquidId, int damage, World world) {

		if (stage.containsKey(liquidId)) {
			HashMap<Integer, int[]> x = stage.get(liquidId);
			if (x.containsKey(damage))
				return x.get(damage);
		} else {
			stage.put(liquidId, new HashMap<Integer, int[]>());
		}

		int[] d = new int[displayStages];
		stage.get(liquidId).put(damage, d);

		BlockInterface block = new BlockInterface();

		// Retrieve the texture depending on type of item.
		if (liquidId < Block.blocksList.length && Block.blocksList[liquidId] != null) {
			block.texture = Block.blocksList[liquidId].getBlockTextureFromSideAndMetadata(0, damage);
		} else if (Item.itemsList[liquidId] != null) {
			block.texture = Item.itemsList[liquidId].getIconFromDamage(damage);
		} else
			return null;

		for (int s = 0; s < displayStages; ++s) {
			d[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d[s], 4864 /* GL_COMPILE */);

			block.minX = 0.5 - 4F * factor + 0.01;
			block.minY = 0;
			block.minZ = 0.5 - 4F * factor + 0.01;

			block.maxX = 0.5 + 4F * factor - 0.01;
			block.maxY = (float) s / (float) displayStages;
			block.maxZ = 0.5 + 4F * factor - 0.01;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();
		}

		return d;
	}

	public RenderRefinery(String baseTexture) {
		this();
	}

	@Override
	public void inventoryRender(double x, double y, double z, float f, float f1) {
		render(null, x, y, z);
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {

		render((TileRefinery) tileentity, x, y, z);
	}

	private void render(TileRefinery tile, double x, double y, double z) {

		int liquid1 = 0, liquid2 = 0, liquid3 = 0;
		int liquidMeta1 = 0, liquidMeta2 = 0, liquidMeta3 = 0;
		int qty1 = 0, qty2 = 0, qty3 = 0;
		float anim = 0;
		int angle = 0;
		ModelRenderer theMagnet = magnet[0];

		if (tile != null) {
			if (tile.ingredient1.getLiquid() != null) {
				liquid1 = tile.ingredient1.getLiquid().itemID;
				liquidMeta1 = tile.ingredient1.getLiquid().itemMeta;
				qty1 = tile.ingredient1.getLiquid().amount;
			}

			if (tile.ingredient2.getLiquid() != null) {
				liquid2 = tile.ingredient2.getLiquid().itemID;
				liquidMeta2 = tile.ingredient2.getLiquid().itemMeta;
				qty2 = tile.ingredient2.getLiquid().amount;
			}

			if (tile.result.getLiquid() != null) {
				liquid3 = tile.result.getLiquid().itemID;
				liquidMeta3 = tile.result.getLiquid().itemMeta;
				qty3 = tile.result.getLiquid().amount;
			}

			anim = tile.getAnimationStage();

			switch (tile.worldObj.getBlockMetadata(tile.xCoord, tile.yCoord, tile.zCoord)) {
			case 2:
				angle = 90;
				break;
			case 3:
				angle = 270;
				break;
			case 4:
				angle = 180;
				break;
			case 5:
				angle = 0;
				break;
			}

			if (tile.animationSpeed <= 1) {
				theMagnet = magnet[0];
			} else if (tile.animationSpeed <= 2.5) {
				theMagnet = magnet[1];
			} else if (tile.animationSpeed <= 4.5) {
				theMagnet = magnet[2];
			} else {
				theMagnet = magnet[3];
			}

		}

		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);

		GL11.glTranslatef((float) x, (float) y, (float) z);

		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		GL11.glRotatef(angle, 0, 1, 0);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

		ForgeHooksClient.bindTexture(DefaultProps.TEXTURE_PATH_BLOCKS + "/refinery.png", 0);
		GL11.glTranslatef(-4F * factor, 0, -4F * factor);
		tank.render(factor);
		GL11.glTranslatef(4F * factor, 0, 4F * factor);

		GL11.glTranslatef(-4F * factor, 0, 4F * factor);
		tank.render(factor);
		GL11.glTranslatef(4F * factor, 0, -4F * factor);

		GL11.glTranslatef(4F * factor, 0, 0);
		tank.render(factor);
		GL11.glTranslatef(-4F * factor, 0, 0);

		float trans1, trans2;

		if (anim <= 100) {
			trans1 = 12F * factor * anim / 100F;
			trans2 = 0;
		} else if (anim <= 200) {
			trans1 = 12F * factor - (12F * factor * (anim - 100F) / 100F);
			trans2 = 12F * factor * (anim - 100F) / 100F;
		} else {
			trans1 = 12F * factor * (anim - 200F) / 100F;
			trans2 = 12F * factor - (12F * factor * (anim - 200F) / 100F);
		}

		GL11.glTranslatef(0, trans1, 0);
		theMagnet.render(factor);
		GL11.glTranslatef(0, -trans1, 0);

		GL11.glTranslatef(0, trans2, 12F * factor);
		theMagnet.render(factor);
		GL11.glTranslatef(0, -trans2, -12F * factor);

		GL11.glTranslatef(0.5F, 0.5F, 0.5F);

		GL11.glTranslatef(-4F * factor, 0, -4F * factor);
		if (qty1 > 0) {
			int[] list1 = getDisplayLists(liquid1, liquidMeta1, tile.worldObj);

			if (list1 != null) {
				setTextureFor(liquid1);
				GL11.glCallList(list1[(int) ((float) qty1 / (float) TileRefinery.LIQUID_PER_SLOT * (displayStages - 1))]);
			}
		}
		GL11.glTranslatef(4F * factor, 0, 4F * factor);

		GL11.glTranslatef(-4F * factor, 0, 4F * factor);
		if (qty2 > 0) {
			int[] list2 = getDisplayLists(liquid2, liquidMeta2, tile.worldObj);

			if (list2 != null) {
				setTextureFor(liquid2);
				GL11.glCallList(list2[(int) ((float) qty2 / (float) TileRefinery.LIQUID_PER_SLOT * (displayStages - 1))]);
			}
		}
		GL11.glTranslatef(4F * factor, 0, -4F * factor);

		GL11.glTranslatef(4F * factor, 0, 0);
		if (qty3 > 0) {
			int[] list3 = getDisplayLists(liquid3, liquidMeta3, tile.worldObj);

			if (list3 != null) {
				setTextureFor(liquid3);
				GL11.glCallList(list3[(int) ((float) qty3 / (float) TileRefinery.LIQUID_PER_SLOT * (displayStages - 1))]);
			}
		}
		GL11.glTranslatef(-4F * factor, 0, 0);

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

	public void setTextureFor(int liquidId) {
		if (liquidId < Block.blocksList.length && Block.blocksList[liquidId] != null) {
			ForgeHooksClient.bindTexture(Block.blocksList[liquidId].getTextureFile(), 0);
		} else {
			ForgeHooksClient.bindTexture(Item.itemsList[liquidId].getTextureFile(), 0);
		}

	}
}

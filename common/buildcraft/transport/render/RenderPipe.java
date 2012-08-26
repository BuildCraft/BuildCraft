/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.render;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.src.Block;
import net.minecraft.src.EntityItem;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.RenderManager;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraft.src.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.api.core.Orientations;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.DefaultProps;
import buildcraft.core.Utils;
import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderEntityBlock.BlockInterface;
import buildcraft.transport.EntityData;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.PipeTransportLiquids;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;

public class RenderPipe extends TileEntitySpecialRenderer {

	final static private int maxPower = 1000;

	final static private int displayLiquidStages = 40;
	
	final static private int renderDistanceSq = 24 * 24;
	
	final static private int numItemsToRender = 10;

	private final static EntityItem dummyEntityItem = new EntityItem(null);

	private class DisplayLiquidList {

		public int[] sideHorizontal = new int[displayLiquidStages];
		public int[] sideVertical = new int[displayLiquidStages];
		public int[] centerHorizontal = new int[displayLiquidStages];
		public int[] centerVertical = new int[displayLiquidStages];
	}

	private HashMap<Integer, HashMap<Integer, DisplayLiquidList>> displayLiquidLists = new HashMap<Integer, HashMap<Integer, DisplayLiquidList>>();
	

	private final int[] angleY = { 0, 0, 270, 90, 0, 180 };
	private final int[] angleZ = { 90, 270, 0, 0, 0, 0 };

	final static private int displayPowerStages = 80;

	public int[] displayPowerList = new int[displayPowerStages];
	public double[] displayPowerLimits = new double[displayPowerStages];

	private RenderBlocks renderBlocks;

	public RenderPipe() {
		renderBlocks = new RenderBlocks();
	}

	private DisplayLiquidList getDisplayLiquidLists(int liquidId, int meta, World world) {
		if (displayLiquidLists.containsKey(liquidId)){
			HashMap<Integer, DisplayLiquidList> x = displayLiquidLists.get(liquidId);
			if (x.containsKey(meta)){
				return x.get(meta);
			}
		} else {
			displayLiquidLists.put(liquidId, new HashMap<Integer, DisplayLiquidList>());
		}
			

		DisplayLiquidList d = new DisplayLiquidList();
		displayLiquidLists.get(liquidId).put(meta, d);

		BlockInterface block = new BlockInterface();
		if (liquidId < Block.blocksList.length && Block.blocksList[liquidId] != null)
			block.texture = Block.blocksList[liquidId].blockIndexInTexture;
		else
			block.texture = Item.itemsList[liquidId].getIconFromDamage(meta);
	
		float size = Utils.pipeMaxPos - Utils.pipeMinPos;

		// render size

		for (int s = 0; s < displayLiquidStages; ++s) {
			float ratio = (float) s / (float) displayLiquidStages;

			// SIDE HORIZONTAL

			d.sideHorizontal[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.sideHorizontal[s], 4864 /* GL_COMPILE */);

			block.minX = 0.0F;
			block.minZ = Utils.pipeMinPos + 0.01F;

			block.maxX = block.minX + size / 2F + 0.01F;
			block.maxZ = block.minZ + size - 0.02F;

			block.minY = Utils.pipeMinPos + 0.01F;
			block.maxY = block.minY + (size - 0.02F) * ratio;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

			// SIDE VERTICAL

			d.sideVertical[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.sideVertical[s], 4864 /* GL_COMPILE */);

			block.minY = Utils.pipeMaxPos - 0.01;
			block.maxY = 1;

			block.minX = 0.5 - (size / 2 - 0.01) * ratio;
			block.maxX = 0.5 + (size / 2 - 0.01) * ratio;

			block.minZ = 0.5 - (size / 2 - 0.01) * ratio;
			block.maxZ = 0.5 + (size / 2 - 0.01) * ratio;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

			// CENTER HORIZONTAL

			d.centerHorizontal[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.centerHorizontal[s], 4864 /* GL_COMPILE */);

			block.minX = Utils.pipeMinPos + 0.01;
			block.minZ = Utils.pipeMinPos + 0.01;

			block.maxX = block.minX + size - 0.02;
			block.maxZ = block.minZ + size - 0.02;

			block.minY = Utils.pipeMinPos + 0.01;
			block.maxY = block.minY + (size - 0.02F) * ratio;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

			// CENTER VERTICAL

			d.centerVertical[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.centerVertical[s], 4864 /* GL_COMPILE */);

			block.minY = Utils.pipeMinPos + 0.01;
			block.maxY = Utils.pipeMaxPos - 0.01;

			block.minX = 0.5 - (size / 2 - 0.02) * ratio;
			block.maxX = 0.5 + (size / 2 - 0.02) * ratio;

			block.minZ = 0.5 - (size / 2 - 0.02) * ratio;
			block.maxZ = 0.5 + (size / 2 - 0.02) * ratio;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

		}

		return d;
	}

	boolean initialized = false;

	private void initializeDisplayPowerList(World world) {
		if (initialized)
			return;

		initialized = true;

		BlockInterface block = new BlockInterface();
		block.texture = 0 * 16 + 4;

		float size = Utils.pipeMaxPos - Utils.pipeMinPos;

		for (int s = 0; s < displayPowerStages; ++s) {
			displayPowerList[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(displayPowerList[s], 4864 /* GL_COMPILE */);

			float minSize = 0.005F;

			float unit = (size - minSize) / 2F / displayPowerStages;

			block.minY = 0.5 - (minSize / 2F) - unit * s;
			block.maxY = 0.5 + (minSize / 2F) + unit * s;

			block.minZ = 0.5 - (minSize / 2F) - unit * s;
			block.maxZ = 0.5 + (minSize / 2F) + unit * s;

			block.minX = 0;
			block.maxX = 0.5 + (minSize / 2F) + unit * s;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();
		}

		for (int i = 0; i < displayPowerStages; ++i)
			displayPowerLimits[displayPowerStages - i - 1] = maxPower
					- Math.sqrt(maxPower * maxPower / (displayPowerStages - 1) * i);
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {

		if (BuildCraftCore.render == RenderMode.NoDynamic)
			return;
			
		if(tileentity.getDistanceFrom(tileEntityRenderer.playerX, tileEntityRenderer.playerY, tileEntityRenderer.playerZ) >= renderDistanceSq)
			return;

		initializeDisplayPowerList(tileentity.worldObj);

		TileGenericPipe pipe = ((TileGenericPipe) tileentity);

		if (pipe.pipe == null)
			return;

		if (pipe.pipe.transport instanceof PipeTransportItems) 
			renderSolids(pipe.pipe, x, y, z);

		else if (pipe.pipe.transport instanceof PipeTransportLiquids) 
			renderLiquids(pipe.pipe, x, y, z);		
		
		else if (pipe.pipe.transport instanceof PipeTransportPower) 
			renderPower(pipe.pipe, x, y, z);
		
	}

	private void renderPower(Pipe pipe, double x, double y, double z) {
		PipeTransportPower pow = (PipeTransportPower) pipe.transport;

		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);

		ForgeHooksClient.bindTexture(DefaultProps.TEXTURE_BLOCKS, 0);

		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

		for (int i = 0; i < 6; ++i) {
			GL11.glPushMatrix();

			GL11.glRotatef(angleY[i], 0, 1, 0);
			GL11.glRotatef(angleZ[i], 0, 0, 1);

			if (pow.displayPower[i] >= 1.0) {
				int stage = 0;

				for (; stage < displayPowerStages; ++stage)
					if (displayPowerLimits[stage] > pow.displayPower[i])
						break;

				if (stage < displayPowerList.length)
					GL11.glCallList(displayPowerList[stage]);
				else
					GL11.glCallList(displayPowerList[displayPowerList.length - 1]);
			}

			GL11.glPopMatrix();
		}

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

	private void renderLiquids(Pipe pipe, double x, double y, double z) {
		PipeTransportLiquids liq = (PipeTransportLiquids) pipe.transport;

		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);

		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

		// sides

		boolean sides = false, above = false;

		for (int i = 0; i < 6; ++i) {
			//ILiquidTank tank = liq.getTanks()[i];
			//LiquidStack liquid = tank.getLiquid();
			LiquidStack liquid = liq.renderCache[i];
			//int amount = liquid != null ? liquid.amount : 0;
			//int amount = liquid != null ? liq.renderAmmount[i] : 0;
			
			if ( liquid != null && liquid.amount > 0) {
				DisplayLiquidList d = getListFromBuffer(liquid, pipe.worldObj);

				if (d == null)
					continue;

				int stage = (int) ((float) liquid.amount / (float) (PipeTransportLiquids.LIQUID_IN_PIPE) * (displayLiquidStages - 1));

				GL11.glPushMatrix();
				int list = 0;

				switch (Orientations.values()[i]) {
				case YPos:
					above = true;
					list = d.sideVertical[stage];
					break;
				case YNeg:
					GL11.glTranslatef(0, -0.75F, 0);
					list = d.sideVertical[stage];
					break;
				case XPos:
				case XNeg:
				case ZPos:
				case ZNeg:
					sides = true;
					GL11.glRotatef(angleY[i], 0, 1, 0);
					GL11.glRotatef(angleZ[i], 0, 0, 1);
					list = d.sideHorizontal[stage];
					break;
				default:
				}

				GL11.glCallList(list);
				GL11.glPopMatrix();
			}
		}
		// CENTER
//		ILiquidTank tank = liq.getTanks()[Orientations.Unknown.ordinal()];
//		LiquidStack liquid = tank.getLiquid();
		LiquidStack liquid = liq.renderCache[Orientations.Unknown.ordinal()];

		//int amount = liquid != null ? liquid.amount : 0; 
		//int amount = liquid != null ? liq.renderAmmount[Orientations.Unknown.ordinal()] : 0;
		if (liquid != null && liquid.amount > 0) {
			//DisplayLiquidList d = getListFromBuffer(liq.getTanks()[Orientations.Unknown.ordinal()].getLiquid(), pipe.worldObj);
			DisplayLiquidList d = getListFromBuffer(liquid, pipe.worldObj);

			if (d != null) {
				int stage = (int) ((float) liquid.amount / (float) (PipeTransportLiquids.LIQUID_IN_PIPE) * (displayLiquidStages - 1));

				if (above)
					GL11.glCallList(d.centerVertical[stage]);

				if (!above || sides)
					GL11.glCallList(d.centerHorizontal[stage]);
			}

		}

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

	public DisplayLiquidList getListFromBuffer(LiquidStack stack, World world) {

		int liquidId = stack.itemID;

		if (liquidId == 0)
			return null;

		if (liquidId < Block.blocksList.length && Block.blocksList[liquidId] != null) {
			ForgeHooksClient.bindTexture(Block.blocksList[liquidId].getTextureFile(), 0);
		} else {
			ForgeHooksClient.bindTexture(Item.itemsList[liquidId].getTextureFile(), 0);
		}
		return getDisplayLiquidLists(liquidId, stack.itemMeta, world);
	}

	private void renderSolids(Pipe pipe, double x, double y, double z) {
		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);
		
		float light = pipe.worldObj.getLightBrightness(pipe.xCoord, pipe.yCoord, pipe.zCoord);

		int count = 0;
		for (EntityData data : ((PipeTransportItems) pipe.transport).travelingEntities.values()) {
			if(count >= numItemsToRender)
				break;
				
			doRenderItem(data.item, x + data.item.getPosition().x - pipe.xCoord, y + data.item.getPosition().y - pipe.yCoord, z + data.item.getPosition().z
					- pipe.zCoord, light);
			count++;
		}					

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

	private Random random = new Random();

	public void doRenderItem(IPipedItem entityitem, double d, double d1, double d2, float f1) {

		if (entityitem == null || entityitem.getItemStack() == null)
			return;

		ItemStack itemstack = entityitem.getItemStack();
		random.setSeed(187L);

		GL11.glPushMatrix();

		byte quantity = 1;
		if (entityitem.getItemStack().stackSize > 1)
			quantity = 2;

		GL11.glTranslatef((float) d, (float) d1, (float) d2);
		GL11.glEnable(32826 /* GL_RESCALE_NORMAL_EXT */);

		IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, ItemRenderType.ENTITY);

		if (customRenderer != null) {

			GL11.glTranslatef(0, 0.25F, 0); // BC SPECIFIC
			ForgeHooksClient.bindTexture(itemstack.getItem().getTextureFile(), 0);
			float f4 = 0.25F;
			f4 = 0.5F;
			GL11.glScalef(f4, f4, f4);

			for (int j = 0; j < quantity; j++) {

				GL11.glPushMatrix();

				if (j > 0) {
					float f5 = ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / f4;
					float f7 = ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / f4;
					float f9 = ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / f4;
					GL11.glTranslatef(f5, f7, f9);
				}

				RenderPipe.dummyEntityItem.item = itemstack;

				customRenderer.renderItem(ItemRenderType.ENTITY, itemstack, renderBlocks, RenderPipe.dummyEntityItem);
				GL11.glPopMatrix();
			}

		} else if (itemstack.itemID < Block.blocksList.length && Block.blocksList[itemstack.itemID] != null 
				&& Block.blocksList[itemstack.itemID].blockID != 0
				&& RenderBlocks.renderItemIn3d(Block.blocksList[itemstack.itemID].getRenderType())) {
			GL11.glTranslatef(0, 0.25F, 0); // BC SPECIFIC

			ForgeHooksClient.bindTexture(Block.blocksList[itemstack.itemID].getTextureFile(), 0);
			float f4 = 0.25F;
			int j = Block.blocksList[itemstack.itemID].getRenderType();
			if (j == 1 || j == 19 || j == 12 || j == 2)
				f4 = 0.5F;

			GL11.glScalef(f4, f4, f4);
			for (int k = 0; k < quantity; k++) {
				GL11.glPushMatrix();

				if (k > 0) {
					float f6 = ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / f4;
					float f9 = ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / f4;
					float f11 = ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / f4;
					GL11.glTranslatef(f6, f9, f11);
				}

				float f7 = 1.0F;
				renderBlocks.renderBlockAsItem(Block.blocksList[itemstack.itemID], itemstack.getItemDamage(), f7);
				GL11.glPopMatrix();
			}

		} else {
			GL11.glTranslatef(0, 0.10F, 0); // BC SPECIFIC

			if (itemstack.getItem().requiresMultipleRenderPasses()) {
				GL11.glScalef(0.5F, 0.5F, 0.5F);
				ForgeHooksClient.bindTexture(Item.itemsList[itemstack.itemID].getTextureFile(), 0);

				for (int i = 0; i <= 1; ++i) {
					int iconIndex = itemstack.getItem().getIconFromDamageForRenderPass(itemstack.getItemDamage(), i);
					float scale = 1.0F;

					if (true) {
						int var17 = Item.itemsList[itemstack.itemID].getColorFromDamage(itemstack.getItemDamage(), i);
						float var18 = (var17 >> 16 & 255) / 255.0F;
						float var19 = (var17 >> 8 & 255) / 255.0F;
						float var20 = (var17 & 255) / 255.0F;
						GL11.glColor4f(var18 * scale, var19 * scale, var20 * scale, 1.0F);
					}

					this.drawItem(iconIndex, quantity);
				}
			} else {

				GL11.glScalef(0.5F, 0.5F, 0.5F);
				int i = itemstack.getIconIndex();
				if (itemstack.itemID < Block.blocksList.length && Block.blocksList[itemstack.itemID] != null
						&& Block.blocksList[itemstack.itemID].blockID != 0) {
					ForgeHooksClient.bindTexture(Block.blocksList[itemstack.itemID].getTextureFile(), 0);
				} else {
					ForgeHooksClient.bindTexture(Item.itemsList[itemstack.itemID].getTextureFile(), 0);
				}

				drawItem(i, quantity);
			}

		}
		GL11.glDisable(32826 /* GL_RESCALE_NORMAL_EXT */);
		GL11.glPopMatrix();
	}

	private void drawItem(int iconIndex, int quantity) {
		Tessellator tesselator = Tessellator.instance;
		float var4 = (iconIndex % 16 * 16 + 0) / 256.0F;
		float var5 = (iconIndex % 16 * 16 + 16) / 256.0F;
		float var6 = (iconIndex / 16 * 16 + 0) / 256.0F;
		float var7 = (iconIndex / 16 * 16 + 16) / 256.0F;
		float var8 = 1.0F;
		float var9 = 0.5F;
		float var10 = 0.25F;

		for (int var11 = 0; var11 < quantity; ++var11) {
			GL11.glPushMatrix();

			if (var11 > 0) {
				float var12 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
				float var13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
				float var14 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
				GL11.glTranslatef(var12, var13, var14);
			}

			GL11.glRotatef(180.0F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
			tesselator.startDrawingQuads();
			tesselator.setNormal(0.0F, 1.0F, 0.0F);
			tesselator.addVertexWithUV((0.0F - var9), (0.0F - var10), 0.0D, var4, var7);
			tesselator.addVertexWithUV((var8 - var9), (0.0F - var10), 0.0D, var5, var7);
			tesselator.addVertexWithUV((var8 - var9), (1.0F - var10), 0.0D, var5, var6);
			tesselator.addVertexWithUV((0.0F - var9), (1.0F - var10), 0.0D, var4, var6);
			tesselator.draw();
			GL11.glPopMatrix();
		}
	}
}

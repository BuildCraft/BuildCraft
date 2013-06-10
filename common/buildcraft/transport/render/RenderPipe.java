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

import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;

import org.lwjgl.opengl.GL11;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.BuildCraftTransport;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.render.LiquidRenderer;
import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderEntityBlock.BlockInterface;
import buildcraft.core.utils.Utils;
import buildcraft.transport.EntityData;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.PipeTransportLiquids;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;

public class RenderPipe extends TileEntitySpecialRenderer {

	final static private int LIQUID_STAGES = 40;

	final static private int MAX_ITEMS_TO_RENDER = 10;

	private final EntityItem dummyEntityItem = new EntityItem(null);

	private final RenderItem customRenderItem;

	private class DisplayLiquidList {

		public int[] sideHorizontal = new int[LIQUID_STAGES];
		public int[] sideVertical = new int[LIQUID_STAGES];
		public int[] centerHorizontal = new int[LIQUID_STAGES];
		public int[] centerVertical = new int[LIQUID_STAGES];
	}

	private final HashMap<Integer, HashMap<Integer, DisplayLiquidList>> displayLiquidLists = new HashMap<Integer, HashMap<Integer, DisplayLiquidList>>();

	private final int[] angleY = { 0, 0, 270, 90, 0, 180 };
	private final int[] angleZ = { 90, 270, 0, 0, 0, 0 };

	final static private int POWER_STAGES = 100;

	public int[] displayPowerList = new int[POWER_STAGES];
	public int[] displayPowerListOverload = new int[POWER_STAGES];

	public RenderPipe() {
	    customRenderItem = new RenderItem() {
	        public boolean shouldBob() {
	            return false;
	        };
	        public boolean shouldSpreadItems() {
	            return false;
	        };
	    };
	    customRenderItem.setRenderManager(RenderManager.instance);
	}

	private DisplayLiquidList getDisplayLiquidLists(int liquidId, int meta, World world) {
		if (displayLiquidLists.containsKey(liquidId)) {
			HashMap<Integer, DisplayLiquidList> x = displayLiquidLists.get(liquidId);
			if (x.containsKey(meta))
				return x.get(meta);
		} else {
			displayLiquidLists.put(liquidId, new HashMap<Integer, DisplayLiquidList>());
		}

		DisplayLiquidList d = new DisplayLiquidList();
		displayLiquidLists.get(liquidId).put(meta, d);

		BlockInterface block = new BlockInterface();

		if (liquidId < Block.blocksList.length && Block.blocksList[liquidId] != null) {
			block.baseBlock = Block.blocksList[liquidId];
		} else {
			block.baseBlock = Block.waterStill;
			block.texture = Item.itemsList[liquidId].getIconFromDamage(meta);
		}

		float size = Utils.pipeMaxPos - Utils.pipeMinPos;

		// render size

		for (int s = 0; s < LIQUID_STAGES; ++s) {
			float ratio = (float) s / (float) LIQUID_STAGES;

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
		block.texture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.Power_Normal);

		float size = Utils.pipeMaxPos - Utils.pipeMinPos;

		for (int s = 0; s < POWER_STAGES; ++s) {
			displayPowerList[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(displayPowerList[s], 4864 /* GL_COMPILE */);

			float minSize = 0.005F;

			float unit = (size - minSize) / 2F / POWER_STAGES;

			block.minY = 0.5 - (minSize / 2F) - unit * s;
			block.maxY = 0.5 + (minSize / 2F) + unit * s;

			block.minZ = 0.5 - (minSize / 2F) - unit * s;
			block.maxZ = 0.5 + (minSize / 2F) + unit * s;

			block.minX = 0;
			block.maxX = 0.5 + (minSize / 2F) + unit * s;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();
		}

		block.texture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.Power_Overload);

		size = Utils.pipeMaxPos - Utils.pipeMinPos;

		for (int s = 0; s < POWER_STAGES; ++s) {
			displayPowerListOverload[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(displayPowerListOverload[s], 4864 /* GL_COMPILE */);

			float minSize = 0.005F;

			float unit = (size - minSize) / 2F / POWER_STAGES;

			block.minY = 0.5 - (minSize / 2F) - unit * s;
			block.maxY = 0.5 + (minSize / 2F) + unit * s;

			block.minZ = 0.5 - (minSize / 2F) - unit * s;
			block.maxZ = 0.5 + (minSize / 2F) + unit * s;

			block.minX = 0;
			block.maxX = 0.5 + (minSize / 2F) + unit * s;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();
		}
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {

		if (BuildCraftCore.render == RenderMode.NoDynamic)
			return;

		initializeDisplayPowerList(tileentity.worldObj);

		TileGenericPipe pipe = ((TileGenericPipe) tileentity);

		if (pipe.pipe == null)
			return;

		if (pipe.pipe.transport instanceof PipeTransportItems) {
			renderSolids(pipe.pipe, x, y, z);
		} else if (pipe.pipe.transport instanceof PipeTransportLiquids) {
			renderLiquids(pipe.pipe, x, y, z);
		} else if (pipe.pipe.transport instanceof PipeTransportPower) {
			renderPower(pipe.pipe, x, y, z);
		}

	}

	private void renderPower(Pipe pipe, double x, double y, double z) {
		PipeTransportPower pow = (PipeTransportPower) pipe.transport;

		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);

		GL11.glTranslatef((float) x, (float) y, (float) z);		

		bindTextureByName("/terrain.png");

		int[] displayList = pow.overload ? displayPowerListOverload : displayPowerList;

		for (int i = 0; i < 6; ++i) {
			GL11.glPushMatrix();

			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
			GL11.glRotatef(angleY[i], 0, 1, 0);
			GL11.glRotatef(angleZ[i], 0, 0, 1);
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

			if (pow.displayPower[i] >= 1.0) {
				short stage = pow.displayPower[i];

				if (stage < displayList.length) {
					GL11.glCallList(displayList[stage]);
				} else {
					GL11.glCallList(displayList[displayList.length - 1]);
				}
			}

			GL11.glPopMatrix();
		}

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

	private void renderLiquids(Pipe pipe, double x, double y, double z) {
		PipeTransportLiquids liq = (PipeTransportLiquids) pipe.transport;

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glTranslatef((float) x, (float) y, (float) z);

		// sides

		boolean sides = false, above = false;

		for (int i = 0; i < 6; ++i) {
			// ILiquidTank tank = liq.getTanks()[i];
			// LiquidStack liquid = tank.getLiquid();
			LiquidStack liquid = liq.renderCache[i];
			// int amount = liquid != null ? liquid.amount : 0;
			// int amount = liquid != null ? liq.renderAmmount[i] : 0;

			if (liquid != null && liquid.amount > 0) {
				DisplayLiquidList d = getListFromBuffer(liquid, pipe.worldObj);

				if (d == null) {
					continue;
				}

				int stage = (int) ((float) liquid.amount / (float) (liq.getCapacity()) * (LIQUID_STAGES - 1));

				GL11.glPushMatrix();
				int list = 0;

				switch (ForgeDirection.VALID_DIRECTIONS[i]) {
				case UP:
					above = true;
					list = d.sideVertical[stage];
					break;
				case DOWN:
					GL11.glTranslatef(0, -0.75F, 0);
					list = d.sideVertical[stage];
					break;
				case EAST:
				case WEST:
				case SOUTH:
				case NORTH:
					sides = true;
					// Yes, this is kind of ugly, but was easier than transform the coordinates above.
					GL11.glTranslatef(0.5F, 0.0F, 0.5F);
					GL11.glRotatef(angleY[i], 0, 1, 0);
					GL11.glRotatef(angleZ[i], 0, 0, 1);
					GL11.glTranslatef(-0.5F, 0.0F, -0.5F);
					list = d.sideHorizontal[stage];
					break;
				default:
				}
				LiquidStack canon = liquid.canonical();
				if (canon == null) {
					throw new LiquidRenderer.LiquidCanonException(liquid);
				}
				bindTextureByName(canon.getTextureSheet());
				GL11.glCallList(list);
				GL11.glPopMatrix();
			}
		}
		// CENTER
		// ILiquidTank tank = liq.getTanks()[ForgeDirection.Unknown.ordinal()];
		// LiquidStack liquid = tank.getLiquid();
		LiquidStack liquid = liq.renderCache[ForgeDirection.UNKNOWN.ordinal()];

		// int amount = liquid != null ? liquid.amount : 0;
		// int amount = liquid != null ? liq.renderAmmount[ForgeDirection.Unknown.ordinal()] : 0;
		if (liquid != null && liquid.amount > 0) {
			// DisplayLiquidList d = getListFromBuffer(liq.getTanks()[ForgeDirection.Unknown.ordinal()].getLiquid(), pipe.worldObj);
			DisplayLiquidList d = getListFromBuffer(liquid, pipe.worldObj);

			if (d != null) {
				int stage = (int) ((float) liquid.amount / (float) (liq.getCapacity()) * (LIQUID_STAGES - 1));

				bindTextureByName(liquid.canonical().getTextureSheet());
				
				if (above) {
					GL11.glCallList(d.centerVertical[stage]);
				}

				if (!above || sides) {
					GL11.glCallList(d.centerHorizontal[stage]);
				}
			}

		}
		
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	private DisplayLiquidList getListFromBuffer(LiquidStack stack, World world) {

		int liquidId = stack.itemID;

		if (liquidId == 0)
			return null;

		return getDisplayLiquidLists(liquidId, stack.itemMeta, world);
	}

	private void renderSolids(Pipe pipe, double x, double y, double z) {
		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);

		float light = pipe.worldObj.getLightBrightness(pipe.xCoord, pipe.yCoord, pipe.zCoord);

		int count = 0;
		for (EntityData data : ((PipeTransportItems) pipe.transport).travelingEntities.values()) {
			if (count >= MAX_ITEMS_TO_RENDER) {
				break;
			}

			doRenderItem(data.item, x + data.item.getPosition().x - pipe.xCoord, y + data.item.getPosition().y - pipe.yCoord, z + data.item.getPosition().z
					- pipe.zCoord, light);
			count++;
		}

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

	public void doRenderItem(IPipedItem entityitem, double d, double d1, double d2, float f1) {

		if (entityitem == null || entityitem.getItemStack() == null)
			return;

		float renderScale = 0.7f;
		ItemStack itemstack = entityitem.getItemStack();
		GL11.glPushMatrix();
		GL11.glTranslatef((float) d, (float) d1, (float) d2);
		GL11.glTranslatef(0, 0.25F, 0);
		GL11.glScalef(renderScale, renderScale, renderScale);
		dummyEntityItem.setEntityItemStack(itemstack);
		customRenderItem.doRenderItem(dummyEntityItem, 0, 0, 0, 0, 0);
		GL11.glPopMatrix();
	}
}

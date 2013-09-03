/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.render;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.BuildCraftTransport;
import buildcraft.core.render.FluidRenderer;
import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderEntityBlock.BlockInterface;
import buildcraft.core.utils.EnumColor;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import com.google.common.collect.Maps;
import java.util.HashMap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class RenderPipe extends TileEntitySpecialRenderer {

	final static private int LIQUID_STAGES = 40;
	final static private int MAX_ITEMS_TO_RENDER = 10;
	private final EntityItem dummyEntityItem = new EntityItem(null);
	private final RenderItem customRenderItem;

	private class DisplayFluidList {

		public int[] sideHorizontal = new int[LIQUID_STAGES];
		public int[] sideVertical = new int[LIQUID_STAGES];
		public int[] centerHorizontal = new int[LIQUID_STAGES];
		public int[] centerVertical = new int[LIQUID_STAGES];
	}
	private final HashMap<Integer, DisplayFluidList> displayFluidLists = Maps.newHashMap();
	private final int[] angleY = {0, 0, 270, 90, 0, 180};
	private final int[] angleZ = {90, 270, 0, 0, 0, 0};
	final static private int POWER_STAGES = 100;
	public int[] displayPowerList = new int[POWER_STAGES];
	public int[] displayPowerListOverload = new int[POWER_STAGES];

	public RenderPipe() {
		customRenderItem = new RenderItem() {
			@Override
			public boolean shouldBob() {
				return false;
			}

			@Override
			public boolean shouldSpreadItems() {
				return false;
			}
		};
		customRenderItem.setRenderManager(RenderManager.instance);
	}

	private DisplayFluidList getDisplayFluidLists(int liquidId, World world) {
		if (displayFluidLists.containsKey(liquidId)) {
			return displayFluidLists.get(liquidId);
		}

		DisplayFluidList d = new DisplayFluidList();
		displayFluidLists.put(liquidId, d);

		BlockInterface block = new BlockInterface();

		Fluid fluid = FluidRegistry.getFluid(liquidId);
		if (fluid.getBlockID() > 0) {
			block.baseBlock = Block.blocksList[fluid.getBlockID()];
		} else {
			block.baseBlock = Block.waterStill;
		}
		block.texture = fluid.getStillIcon();

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

			RenderEntityBlock.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);

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

			RenderEntityBlock.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);

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

			RenderEntityBlock.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);

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

			RenderEntityBlock.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);

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
		block.texture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.Power_Normal.ordinal());

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

			RenderEntityBlock.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();
		}

		block.texture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.Power_Overload.ordinal());

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

			RenderEntityBlock.INSTANCE.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();
		}
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {

		if (BuildCraftCore.render == RenderMode.NoDynamic)
			return;

		TileGenericPipe pipe = (TileGenericPipe) tileentity;

		if (pipe.pipe == null)
			return;

		switch (pipe.getPipeType()) {
			case ITEM:
				renderSolids(pipe.pipe, x, y, z);
				break;
			case FLUID:
				renderFluids(pipe.pipe, x, y, z);
				break;
			case POWER:
				renderPower(pipe.pipe, x, y, z);
				break;
		}
	}

	private void renderPower(Pipe<PipeTransportPower> pipe, double x, double y, double z) {
		initializeDisplayPowerList(pipe.container.worldObj);

		PipeTransportPower pow = pipe.transport;

		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);

		GL11.glTranslatef((float) x, (float) y, (float) z);

		bindTexture(TextureMap.locationBlocksTexture);

		int[] displayList = pow.overload > 0 ? displayPowerListOverload : displayPowerList;

		for (int i = 0; i < 6; ++i) {
			GL11.glPushMatrix();

			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
			GL11.glRotatef(angleY[i], 0, 1, 0);
			GL11.glRotatef(angleZ[i], 0, 0, 1);
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

			if (pow.clientDisplayPower[i] >= 1.0) {
				short stage = pow.clientDisplayPower[i];

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

	private void renderFluids(Pipe<PipeTransportFluids> pipe, double x, double y, double z) {
		PipeTransportFluids trans = pipe.transport;

		boolean needsRender = false;
		for (int i = 0; i < 7; ++i) {
			FluidStack fluidStack = trans.renderCache[i];
			if (fluidStack != null && fluidStack.amount > 0) {
				needsRender = true;
				break;
			}
		}

		if (!needsRender)
			return;

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
			FluidStack fluidStack = trans.renderCache[i];

			if (fluidStack != null && fluidStack.amount > 0) {
				DisplayFluidList d = getListFromBuffer(fluidStack, pipe.container.worldObj);

				if (d == null) {
					continue;
				}

				int stage = (int) ((float) fluidStack.amount / (float) (trans.getCapacity()) * (LIQUID_STAGES - 1));

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
				bindTexture(TextureMap.locationBlocksTexture);
				FluidRenderer.setColorForFluidStack(fluidStack);
				GL11.glCallList(list);
				GL11.glPopMatrix();
			}
		}
		// CENTER
		FluidStack fluidStack = trans.renderCache[ForgeDirection.UNKNOWN.ordinal()];

		if (fluidStack != null && fluidStack.amount > 0) {
			DisplayFluidList d = getListFromBuffer(fluidStack, pipe.container.worldObj);

			if (d != null) {
				int stage = (int) ((float) fluidStack.amount / (float) (trans.getCapacity()) * (LIQUID_STAGES - 1));

				bindTexture(TextureMap.locationBlocksTexture);
				FluidRenderer.setColorForFluidStack(fluidStack);
				
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

	private DisplayFluidList getListFromBuffer(FluidStack stack, World world) {

		int liquidId = stack.fluidID;

		if (liquidId == 0)
			return null;

		return getDisplayFluidLists(liquidId, world);
	}

	private void renderSolids(Pipe<PipeTransportItems> pipe, double x, double y, double z) {
		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);

		float light = pipe.container.worldObj.getLightBrightness(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);

		int count = 0;
		for (TravelingItem item : pipe.transport.items) {
			if (count >= MAX_ITEMS_TO_RENDER) {
				break;
			}

			doRenderItem(item, x + item.xCoord - pipe.container.xCoord, y + item.yCoord - pipe.container.yCoord, z + item.zCoord - pipe.container.zCoord, light, item.color);
			count++;
		}

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

	public void doRenderItem(TravelingItem travellingItem, double x, double y, double z, float light, EnumColor color) {

		if (travellingItem == null || travellingItem.getItemStack() == null)
			return;

		float renderScale = 0.7f;
		ItemStack itemstack = travellingItem.getItemStack();
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		GL11.glTranslatef(0, 0.25F, 0);
		GL11.glScalef(renderScale, renderScale, renderScale);
		dummyEntityItem.setEntityItemStack(itemstack);
		customRenderItem.doRenderItem(dummyEntityItem, 0, 0, 0, 0, 0);
		if (color != null) {
			bindTexture(TextureMap.locationBlocksTexture);
			BlockInterface block = new BlockInterface();

			block.texture = PipeIconProvider.TYPE.ItemBox.getIcon();

			float pix = 0.0625F;

			float min = -4 * pix;
			float max = 4 * pix;

			block.minY = min;
			block.maxY = max;

			block.minZ = min;
			block.maxZ = max;

			block.minX = min;
			block.maxX = max;

			int cHex = color.getLightHex();
			float r = (float) (cHex >> 16 & 0xff) / 255F;
			float g = (float) (cHex >> 8 & 0xff) / 255F;
			float b = (float) (cHex & 0xff) / 255F;
			GL11.glColor4f(r, g, b, 1.0F);
			RenderEntityBlock.INSTANCE.renderBlock(block, null, 0, 0, 0, false, true);
		}
		GL11.glPopMatrix();
	}
}

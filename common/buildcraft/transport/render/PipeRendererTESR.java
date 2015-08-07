/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.IntHashMap;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.Position;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.items.IItemCustomPipeRender;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeWire;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.core.lib.render.RenderEntityBlock.RenderInfo;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.gates.GatePluggable;
import buildcraft.transport.utils.FluidRenderData;

public class PipeRendererTESR extends TileEntitySpecialRenderer {
	public static final PipeRendererTESR INSTANCE = new PipeRendererTESR();

	public static final int POWER_STAGES = 256;
	private static final float POWER_MAGIC = 0.7F; // Math.pow(displayPower, POWER_MAGIC)

	private static final int LIQUID_STAGES = 40;
	private static final int MAX_ITEMS_TO_RENDER = 10;

	public int[] displayPowerList = new int[POWER_STAGES];
	public int[] displayPowerListOverload = new int[POWER_STAGES];

	private final IntHashMap displayFluidLists = new IntHashMap();
	private final int[] angleY = {0, 0, 270, 90, 0, 180};
	private final int[] angleZ = {90, 270, 0, 0, 0, 0};

	private final EntityItem dummyEntityItem = new EntityItem(null);
	private final RenderItem customRenderItem;
	private boolean initialized = false;

	private class DisplayFluidList {
		public int[] sideHorizontal = new int[LIQUID_STAGES];
		public int[] sideVertical = new int[LIQUID_STAGES];
		public int[] centerHorizontal = new int[LIQUID_STAGES];
		public int[] centerVertical = new int[LIQUID_STAGES];
	}

	protected PipeRendererTESR() {
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

	public void onTextureReload() {
		if (initialized) {
			for (int i = 0; i < POWER_STAGES; i++) {
				GL11.glDeleteLists(displayPowerList[i], 1);
				GL11.glDeleteLists(displayPowerListOverload[i], 1);
			}
		}
		displayFluidLists.clearMap();

		initialized = false;
	}

	private DisplayFluidList getDisplayFluidLists(int liquidId, int skylight, int blocklight, int flags, World world) {
		int finalBlockLight = Math.max(flags & 31, blocklight);
		int listId = (liquidId & 0x3FFFF) << 13 | (flags & 0xE0 | finalBlockLight) << 5 | (skylight & 31);

		if (displayFluidLists.containsItem(listId)) {
			return (DisplayFluidList) displayFluidLists.lookup(listId);
		}

		Fluid fluid = FluidRegistry.getFluid(liquidId);

		if (fluid == null) {
			return null;
		}

		DisplayFluidList d = new DisplayFluidList();
		displayFluidLists.addKey(listId, d);

		RenderInfo block = new RenderInfo();

		if (fluid.getBlock() != null) {
			block.baseBlock = fluid.getBlock();
		} else {
			block.baseBlock = Blocks.water;
		}

		block.texture = fluid.getStillIcon();
		block.brightness = skylight << 16 | finalBlockLight;

		float size = CoreConstants.PIPE_MAX_POS - CoreConstants.PIPE_MIN_POS;

		// render size

		for (int s = 0; s < LIQUID_STAGES; ++s) {
			float ratio = (float) s / (float) LIQUID_STAGES;

			// SIDE HORIZONTAL

			d.sideHorizontal[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.sideHorizontal[s], GL11.GL_COMPILE);

			block.minX = 0.0F;
			block.minZ = CoreConstants.PIPE_MIN_POS + 0.01F;

			block.maxX = block.minX + size / 2F + 0.01F;
			block.maxZ = block.minZ + size - 0.02F;

			block.minY = CoreConstants.PIPE_MIN_POS + 0.01F;
			block.maxY = block.minY + (size - 0.02F) * ratio;

			RenderEntityBlock.INSTANCE.renderBlock(block);

			GL11.glEndList();

			// SIDE VERTICAL

			d.sideVertical[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.sideVertical[s], GL11.GL_COMPILE);

			block.minY = CoreConstants.PIPE_MAX_POS - 0.01;
			block.maxY = 1;

			block.minX = 0.5 - (size / 2 - 0.01) * ratio;
			block.maxX = 0.5 + (size / 2 - 0.01) * ratio;

			block.minZ = 0.5 - (size / 2 - 0.01) * ratio;
			block.maxZ = 0.5 + (size / 2 - 0.01) * ratio;

			RenderEntityBlock.INSTANCE.renderBlock(block);

			GL11.glEndList();

			// CENTER HORIZONTAL

			d.centerHorizontal[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.centerHorizontal[s], GL11.GL_COMPILE);

			block.minX = CoreConstants.PIPE_MIN_POS + 0.01;
			block.minZ = CoreConstants.PIPE_MIN_POS + 0.01;

			block.maxX = block.minX + size - 0.02;
			block.maxZ = block.minZ + size - 0.02;

			block.minY = CoreConstants.PIPE_MIN_POS + 0.01;
			block.maxY = block.minY + (size - 0.02F) * ratio;

			RenderEntityBlock.INSTANCE.renderBlock(block);

			GL11.glEndList();

			// CENTER VERTICAL

			d.centerVertical[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.centerVertical[s], GL11.GL_COMPILE);

			block.minY = CoreConstants.PIPE_MIN_POS + 0.01;
			block.maxY = CoreConstants.PIPE_MAX_POS - 0.01;

			block.minX = 0.5 - (size / 2 - 0.02) * ratio;
			block.maxX = 0.5 + (size / 2 - 0.02) * ratio;

			block.minZ = 0.5 - (size / 2 - 0.02) * ratio;
			block.maxZ = 0.5 + (size / 2 - 0.02) * ratio;

			RenderEntityBlock.INSTANCE.renderBlock(block);

			GL11.glEndList();

		}

		return d;
	}

	private void initializeDisplayPowerList(World world) {
		if (initialized) {
			return;
		}

		initialized = true;

		RenderInfo block = new RenderInfo();
		block.texture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.Power_Normal.ordinal());

		float size = CoreConstants.PIPE_MAX_POS - CoreConstants.PIPE_MIN_POS;

		for (int s = 0; s < POWER_STAGES; ++s) {
			displayPowerList[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(displayPowerList[s], GL11.GL_COMPILE);

			float minSize = 0.005F;

			float unit = (size - minSize) / 2F / POWER_STAGES;

			block.minY = 0.5 - (minSize / 2F) - unit * s;
			block.maxY = 0.5 + (minSize / 2F) + unit * s;

			block.minZ = 0.5 - (minSize / 2F) - unit * s;
			block.maxZ = 0.5 + (minSize / 2F) + unit * s;

			block.minX = 0;
			block.maxX = 0.5 + (minSize / 2F) + unit * s;

			RenderEntityBlock.INSTANCE.renderBlock(block);

			GL11.glEndList();
		}

		block.texture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.Power_Overload.ordinal());

		size = CoreConstants.PIPE_MAX_POS - CoreConstants.PIPE_MIN_POS;

		for (int s = 0; s < POWER_STAGES; ++s) {
			displayPowerListOverload[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(displayPowerListOverload[s], GL11.GL_COMPILE);

			float minSize = 0.005F;

			float unit = (size - minSize) / 2F / POWER_STAGES;

			block.minY = 0.5 - (minSize / 2F) - unit * s;
			block.maxY = 0.5 + (minSize / 2F) + unit * s;

			block.minZ = 0.5 - (minSize / 2F) - unit * s;
			block.maxZ = 0.5 + (minSize / 2F) + unit * s;

			block.minX = 0;
			block.maxX = 0.5 + (minSize / 2F) + unit * s;

			RenderEntityBlock.INSTANCE.renderBlock(block);

			GL11.glEndList();
		}
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		if (BuildCraftCore.render == RenderMode.NoDynamic) {
			return;
		}

		TileGenericPipe pipe = (TileGenericPipe) tileentity;

		if (pipe.pipe == null) {
			return;
		}

		renderGatesWires(pipe, x, y, z);
		renderPluggables(pipe, x, y, z);

		IPipeTile.PipeType pipeType = pipe.getPipeType();

		// do not use switch. we will be transitioning away from the enum
		if (pipeType == IPipeTile.PipeType.ITEM) {
			renderSolids(pipe.pipe, x, y, z, f);
		} else if (pipeType == IPipeTile.PipeType.FLUID) {
			renderFluids(((TileGenericPipe) CoreProxy.proxy.getServerTile(pipe)).pipe, x, y, z);
		} else if (pipeType == IPipeTile.PipeType.POWER) {
			renderPower(((TileGenericPipe) CoreProxy.proxy.getServerTile(pipe)).pipe, x, y, z);
		} /* else if (pipeType == PipeType.STRUCTURE) {
			// no object to render in a structure pipe;
		} */
	}

	private void renderGatesWires(TileGenericPipe pipe, double x, double y, double z) {
		PipeRenderState state = pipe.renderState;

		if (state.wireMatrix.hasWire(PipeWire.RED)) {
			pipeWireRender(pipe, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MIN_POS, PipeWire.RED, x, y, z);
		}

		if (state.wireMatrix.hasWire(PipeWire.BLUE)) {
			pipeWireRender(pipe, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, PipeWire.BLUE, x, y, z);
		}

		if (state.wireMatrix.hasWire(PipeWire.GREEN)) {
			pipeWireRender(pipe, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, PipeWire.GREEN, x, y, z);
		}

		if (state.wireMatrix.hasWire(PipeWire.YELLOW)) {
			pipeWireRender(pipe, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, PipeWire.YELLOW, x, y, z);
		}
	}

	private void pipeWireRender(TileGenericPipe pipe, float cx, float cy, float cz, PipeWire color, double x, double y, double z) {

		PipeRenderState state = pipe.renderState;

		float minX = CoreConstants.PIPE_MIN_POS;
		float minY = CoreConstants.PIPE_MIN_POS;
		float minZ = CoreConstants.PIPE_MIN_POS;

		float maxX = CoreConstants.PIPE_MAX_POS;
		float maxY = CoreConstants.PIPE_MAX_POS;
		float maxZ = CoreConstants.PIPE_MAX_POS;

		boolean foundX = false, foundY = false, foundZ = false;

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.WEST)) {
			minX = 0;
			foundX = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.EAST)) {
			maxX = 1;
			foundX = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.DOWN)) {
			minY = 0;
			foundY = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.UP)) {
			maxY = 1;
			foundY = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.NORTH)) {
			minZ = 0;
			foundZ = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.SOUTH)) {
			maxZ = 1;
			foundZ = true;
		}

		boolean center = false;

		if (minX == 0 && maxX != 1 && (foundY || foundZ)) {
			if (cx == CoreConstants.PIPE_MIN_POS) {
				maxX = CoreConstants.PIPE_MIN_POS;
			} else {
				center = true;
			}
		}

		if (minX != 0 && maxX == 1 && (foundY || foundZ)) {
			if (cx == CoreConstants.PIPE_MAX_POS) {
				minX = CoreConstants.PIPE_MAX_POS;
			} else {
				center = true;
			}
		}

		if (minY == 0 && maxY != 1 && (foundX || foundZ)) {
			if (cy == CoreConstants.PIPE_MIN_POS) {
				maxY = CoreConstants.PIPE_MIN_POS;
			} else {
				center = true;
			}
		}

		if (minY != 0 && maxY == 1 && (foundX || foundZ)) {
			if (cy == CoreConstants.PIPE_MAX_POS) {
				minY = CoreConstants.PIPE_MAX_POS;
			} else {
				center = true;
			}
		}

		if (minZ == 0 && maxZ != 1 && (foundX || foundY)) {
			if (cz == CoreConstants.PIPE_MIN_POS) {
				maxZ = CoreConstants.PIPE_MIN_POS;
			} else {
				center = true;
			}
		}

		if (minZ != 0 && maxZ == 1 && (foundX || foundY)) {
			if (cz == CoreConstants.PIPE_MAX_POS) {
				minZ = CoreConstants.PIPE_MAX_POS;
			} else {
				center = true;
			}
		}

		boolean found = foundX || foundY || foundZ;

		GL11.glPushMatrix();
		GL11.glColor3f(1, 1, 1);
		GL11.glTranslatef((float) x, (float) y, (float) z);

		float scale = 1.001f;
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		GL11.glScalef(scale, scale, scale);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);


		bindTexture(TextureMap.locationBlocksTexture);

		RenderInfo renderBox = new RenderInfo();
		renderBox.texture = BuildCraftTransport.instance.wireIconProvider.getIcon(state.wireMatrix.getWireIconIndex(color));
		boolean isLit = (state.wireMatrix.getWireIconIndex(color) & 1) > 0;
		
		// Z render

		if (minZ != CoreConstants.PIPE_MIN_POS || maxZ != CoreConstants.PIPE_MAX_POS || !found) {
			renderBox.setBounds(cx == CoreConstants.PIPE_MIN_POS ? cx - 0.05F : cx, cy == CoreConstants.PIPE_MIN_POS ? cy - 0.05F : cy, minZ, cx == CoreConstants.PIPE_MIN_POS ? cx
					: cx + 0.05F, cy == CoreConstants.PIPE_MIN_POS ? cy : cy + 0.05F, maxZ);
			renderLitBox(renderBox, isLit);
		}

		// X render

		if (minX != CoreConstants.PIPE_MIN_POS || maxX != CoreConstants.PIPE_MAX_POS || !found) {
			renderBox.setBounds(minX, cy == CoreConstants.PIPE_MIN_POS ? cy - 0.05F : cy, cz == CoreConstants.PIPE_MIN_POS ? cz - 0.05F : cz, maxX, cy == CoreConstants.PIPE_MIN_POS ? cy
					: cy + 0.05F, cz == CoreConstants.PIPE_MIN_POS ? cz : cz + 0.05F);
			renderLitBox(renderBox, isLit);
		}

		// Y render

		if (minY != CoreConstants.PIPE_MIN_POS || maxY != CoreConstants.PIPE_MAX_POS || !found) {
			renderBox.setBounds(cx == CoreConstants.PIPE_MIN_POS ? cx - 0.05F : cx, minY, cz == CoreConstants.PIPE_MIN_POS ? cz - 0.05F : cz, cx == CoreConstants.PIPE_MIN_POS ? cx
					: cx + 0.05F, maxY, cz == CoreConstants.PIPE_MIN_POS ? cz : cz + 0.05F);
			renderLitBox(renderBox, isLit);
		}

		if (center || !found) {
			renderBox.setBounds(cx == CoreConstants.PIPE_MIN_POS ? cx - 0.05F : cx, cy == CoreConstants.PIPE_MIN_POS ? cy - 0.05F : cy, cz == CoreConstants.PIPE_MIN_POS ? cz - 0.05F : cz,
					cx == CoreConstants.PIPE_MIN_POS ? cx : cx + 0.05F, cy == CoreConstants.PIPE_MIN_POS ? cy : cy + 0.05F, cz == CoreConstants.PIPE_MIN_POS ? cz : cz + 0.05F);
			renderLitBox(renderBox, isLit);
		}

		GL11.glPopMatrix();
	}

	private void renderPluggables(TileGenericPipe pipe, double x, double y, double z) {
		TileEntityRendererDispatcher.instance.field_147553_e.bindTexture(TextureMap.locationBlocksTexture);

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			PipePluggable pluggable = pipe.getPipePluggable(direction);
			if (pluggable != null && pluggable.getDynamicRenderer() != null) {
				pluggable.getDynamicRenderer().renderPluggable(pipe.getPipe(), direction, pluggable, x, y, z);
			}
		}
	}

	public static void renderGateStatic(RenderBlocks renderblocks, ForgeDirection direction, GatePluggable gate, ITextureStates blockStateMachine, int x, int y, int z) {
		blockStateMachine.getTextureState().set(gate.getLogic().getGateIcon());

		float trim = 0.1F;
		float[][] zeroState = new float[3][2];
		float min = CoreConstants.PIPE_MIN_POS + trim / 2F;
		float max = CoreConstants.PIPE_MAX_POS - trim / 2F;

		// X START - END
		zeroState[0][0] = min;
		zeroState[0][1] = max;
		// Y START - END
		zeroState[1][0] = CoreConstants.PIPE_MIN_POS - 0.10F;
		zeroState[1][1] = CoreConstants.PIPE_MIN_POS + 0.001F;
		// Z START - END
		zeroState[2][0] = min;
		zeroState[2][1] = max;

		float[][] rotated = MatrixTranformations.deepClone(zeroState);
		MatrixTranformations.transform(rotated, direction);

		blockStateMachine.setRenderAllSides();
		renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
		renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);
	}

	public static void renderGate(double x, double y, double z, GatePluggable gate, ForgeDirection direction) {
		GL11.glPushMatrix();
		GL11.glColor3f(1, 1, 1);
		GL11.glTranslatef((float) x, (float) y, (float) z);

		IIcon lightIcon;
		if (gate.isLit) {
			lightIcon = gate.getLogic().getIconLit();
		} else {
			lightIcon = gate.getLogic().getIconDark();
		}

		float translateCenter = 0;

		renderGate(lightIcon, 0, 0.1F, 0, 0, direction, gate.isLit, 1);

		float pulseStage = gate.getPulseStage() * 2F;

		if (gate.isPulsing || pulseStage != 0) {
			IIcon gateIcon = gate.getLogic().getGateIcon();

			// Render pulsing gate
			float amplitude = 0.10F;
			float start = 0.01F;

			if (pulseStage < 1) {
				translateCenter = (pulseStage * amplitude) + start;
			} else {
				translateCenter = amplitude - ((pulseStage - 1F) * amplitude) + start;
			}

			renderGate(gateIcon, 0, 0.13F, translateCenter, translateCenter, direction, false, 2);
			renderGate(lightIcon, 0, 0.13F, translateCenter, translateCenter, direction, gate.isLit, 0);
		}

		IIcon materialIcon = gate.getMaterial().getIconBlock();
		if (materialIcon != null) {
			renderGate(materialIcon, 1, 0.13F, translateCenter, translateCenter, direction, false, 1);
		}

		for (IGateExpansion expansion : gate.getExpansions()) {
			renderGate(expansion.getOverlayBlock(), 2, 0.13F, translateCenter, translateCenter, direction, false, 0);
		}

		GL11.glPopMatrix();
	}

	private static void renderGate(IIcon icon, int layer, float trim, float translateCenter, float extraDepth, ForgeDirection direction, boolean isLit, int sideRenderingMode) {
		RenderInfo renderBox = new RenderInfo();
		renderBox.texture = icon;

		float[][] zeroState = new float[3][2];
		float min = CoreConstants.PIPE_MIN_POS + trim / 2F;
		float max = CoreConstants.PIPE_MAX_POS - trim / 2F;

		// X START - END
		zeroState[0][0] = min;
		zeroState[0][1] = max;
		// Y START - END
		zeroState[1][0] = CoreConstants.PIPE_MIN_POS - 0.10F - 0.001F * layer;
		zeroState[1][1] = CoreConstants.PIPE_MIN_POS + 0.001F + 0.01F * layer + extraDepth;
		// Z START - END
		zeroState[2][0] = min;
		zeroState[2][1] = max;


		if (translateCenter != 0) {
			GL11.glPushMatrix();
			float xt = direction.offsetX * translateCenter, yt = direction.offsetY * translateCenter, zt = direction.offsetZ
					* translateCenter;

			GL11.glTranslatef(xt, yt, zt);
		}

		float[][] rotated = MatrixTranformations.deepClone(zeroState);
		MatrixTranformations.transform(rotated, direction);

		switch (sideRenderingMode) {
			case 0:
				renderBox.setRenderSingleSide(direction.ordinal());
				break;
			case 1:
				renderBox.setRenderSingleSide(direction.ordinal());
				renderBox.renderSide[direction.ordinal() ^ 1] = true;
				break;
			case 2:
				break;
		}

		renderBox.setBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
		renderLitBox(renderBox, isLit);
		if (translateCenter != 0) {
			GL11.glPopMatrix();
		}
	}
	
	private static void renderLitBox(RenderInfo info, boolean isLit) {
		RenderEntityBlock.INSTANCE.renderBlock(info);

		float lastX = OpenGlHelper.lastBrightnessX;
		float lastY = OpenGlHelper.lastBrightnessY;
		if (isLit) {
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
			GL11.glDepthMask(true);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680, 0);
			RenderEntityBlock.INSTANCE.renderBlock(info);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
	}

	public boolean isOpenOrientation(PipeRenderState state, ForgeDirection direction) {
		int connections = 0;

		ForgeDirection targetOrientation = ForgeDirection.UNKNOWN;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (state.pipeConnectionMatrix.isConnected(o)) {

				connections++;

				if (connections == 1) {
					targetOrientation = o;
				}
			}
		}

		if (connections > 1 || connections == 0) {
			return false;
		}

		return targetOrientation.getOpposite() == direction;
	}

	private void renderPower(Pipe<PipeTransportPower> pipe, double x, double y, double z) {
		initializeDisplayPowerList(pipe.container.getWorldObj());

		PipeTransportPower pow = pipe.transport;

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glDisable(GL11.GL_LIGHTING);

		GL11.glTranslatef((float) x, (float) y, (float) z);

		bindTexture(TextureMap.locationBlocksTexture);

		int[] displayList = pow.overload > 0 ? displayPowerListOverload : displayPowerList;

		for (int side = 0; side < 6; ++side) {
			int stage = (int) Math.ceil(Math.pow(pow.displayPower[side], POWER_MAGIC));
			if (stage >= 1) {
				if (!pipe.container.isPipeConnected(ForgeDirection.getOrientation(side))) {
					continue;
				}

				GL11.glPushMatrix();

				GL11.glTranslatef(0.5F, 0.5F, 0.5F);
				GL11.glRotatef(angleY[side], 0, 1, 0);
				GL11.glRotatef(angleZ[side], 0, 0, 1);
				float scale = 1.0F - side * 0.0001F;
				GL11.glScalef(scale, scale, scale);
				GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

				if (stage < displayList.length) {
					GL11.glCallList(displayList[stage]);
				} else {
					GL11.glCallList(displayList[displayList.length - 1]);
				}

				GL11.glPopMatrix();
			}
		}

		/*bindTexture(STRIPES_TEXTURE);

		for (int side = 0; side < 6; side += 2) {
			if (pipe.container.isPipeConnected(ForgeDirection.values()[side])) {
				GL11.glPushMatrix();

				GL11.glTranslatef(0.5F, 0.5F, 0.5F);

				GL11.glRotatef(angleY[side], 0, 1, 0);
				GL11.glRotatef(angleZ[side], 0, 0, 1);

				float scale = 1.0F - side * 0.0001F;
				GL11.glScalef(scale, scale, scale);

				float movement = (0.50F) * pipe.transport.getPistonStage(side / 2);
				GL11.glTranslatef(-0.25F - 1F / 16F - movement, -0.5F, -0.5F);

				// float factor = (float) (1.0 / 256.0);
				float factor = (float) (1.0 / 16.0);
				box.render(factor);
				GL11.glPopMatrix();
			}
		}*/

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	private void renderFluids(Pipe<PipeTransportFluids> pipe, double x, double y, double z) {
		PipeTransportFluids trans = pipe.transport;

		boolean needsRender = false;
		FluidRenderData renderData;
		if (!pipe.container.getWorldObj().isRemote) {
			renderData = trans.createServerFluidRenderData();
		} else {
			renderData = trans.renderCache;
		}

		for (int i = 0; i < 7; ++i) {
			if (renderData.amount[i] > 0) {
				needsRender = true;
				break;
			}
		}

		if (!needsRender) {
			return;
		}

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glTranslatef((float) x, (float) y, (float) z);

		int skylight = pipe.container.getWorld().getSkyBlockTypeBrightness(EnumSkyBlock.Sky, pipe.container.x(), pipe.container.y(), pipe.container.z());
		int blocklight = pipe.container.getWorld().getSkyBlockTypeBrightness(EnumSkyBlock.Block, pipe.container.x(), pipe.container.y(), pipe.container.z());

		boolean sides = false, above = false;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			int i = side.ordinal();

			if (renderData.amount[i] <= 0) {
				continue;
			}

			if (!pipe.container.isPipeConnected(side)) {
				continue;
			}

			DisplayFluidList d = getDisplayFluidLists(renderData.fluidID, skylight, blocklight,
					renderData.flags, pipe.container.getWorldObj());

			if (d == null) {
				continue;
			}

			int stage = (int) ((float) renderData.amount[i] / (float) (trans.getCapacity()) * (LIQUID_STAGES - 1));

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
			RenderUtils.setGLColorFromInt(renderData.color);
			GL11.glCallList(list);
			GL11.glPopMatrix();
		}

		if (renderData.amount[6] > 0) {
			DisplayFluidList d = getDisplayFluidLists(renderData.fluidID, skylight, blocklight,
					renderData.flags, pipe.container.getWorldObj());

			if (d != null) {
				int stage = (int) ((float) renderData.amount[6] / (float) (trans.getCapacity()) * (LIQUID_STAGES - 1));

				bindTexture(TextureMap.locationBlocksTexture);
				RenderUtils.setGLColorFromInt(renderData.color);

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

	private void renderSolids(Pipe<PipeTransportItems> pipe, double x, double y, double z, float f) {
		GL11.glPushMatrix();

		int count = 0;
		for (TravelingItem item : pipe.transport.items) {
			if (count >= MAX_ITEMS_TO_RENDER) {
				break;
			}

			Position motion = new Position(0, 0, 0, item.toCenter ? item.input : item.output);
			motion.moveForwards(item.getSpeed() * f);

			doRenderItem(item, x + item.xCoord - pipe.container.xCoord + motion.x, y + item.yCoord - pipe.container.yCoord  + motion.y, z + item.zCoord - pipe.container.zCoord  + motion.z, 0.0F, item.color);
			count++;
		}

		GL11.glPopMatrix();
	}

	private int getItemLightLevel(ItemStack stack) {
		if (stack.getItem() instanceof ItemBlock) {
			Block b = Block.getBlockFromItem(stack.getItem());
			return b.getLightValue();
		}
		return 0;
	}

	public void doRenderItem(TravelingItem travellingItem, double x, double y, double z, float light, EnumColor color) {
		if (travellingItem == null || travellingItem.getItemStack() == null) {
			return;
		}

		float renderScale = 0.7f;
		ItemStack itemstack = travellingItem.getItemStack();

		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y + 0.25F, (float) z);

		//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, skylight << 4, Math.max(blocklight, getItemLightLevel(itemstack)) << 4);

		if (travellingItem.hasDisplayList) {
			GL11.glCallList(travellingItem.displayList);
		} else {
			travellingItem.displayList = GLAllocation.generateDisplayLists(1);
			travellingItem.hasDisplayList = true;

			GL11.glNewList(travellingItem.displayList, GL11.GL_COMPILE_AND_EXECUTE);
			if (itemstack.getItem() instanceof IItemCustomPipeRender) {
				IItemCustomPipeRender render = (IItemCustomPipeRender) itemstack.getItem();
				float itemScale = render.getPipeRenderScale(itemstack);
				GL11.glScalef(renderScale * itemScale, renderScale * itemScale, renderScale * itemScale);
				itemScale = 1 / itemScale;

				if (!render.renderItemInPipe(itemstack, x, y, z)) {
					dummyEntityItem.setEntityItemStack(itemstack);
					customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);
				}

				GL11.glScalef(itemScale, itemScale, itemScale);
			} else {
				GL11.glScalef(renderScale, renderScale, renderScale);
				dummyEntityItem.setEntityItemStack(itemstack);
				customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);
			}
			GL11.glEndList();
		}

		if (color != null) {
			bindTexture(TextureMap.locationBlocksTexture);
			RenderInfo block = new RenderInfo();

			block.texture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.ItemBox.ordinal());

			float pix = 0.0625F;
			float min = -4 * pix;
			float max = 4 * pix;

			block.minY = min;
			block.maxY = max;

			block.minZ = min;
			block.maxZ = max;

			block.minX = min;
			block.maxX = max;

			RenderUtils.setGLColorFromInt(color.getLightHex());
			RenderEntityBlock.INSTANCE.renderBlock(block);
		}

		GL11.glPopMatrix();
	}
}

/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeWire;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.core.lib.render.RenderEntityBlock.RenderInfo;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.gates.GatePluggable;

public class PipeRendererTESR extends TileEntitySpecialRenderer {
	public static final PipeRendererTESR INSTANCE = new PipeRendererTESR();

	protected PipeRendererTESR() {
	}

	public void onTextureReload() {
		PipeTransportPowerRenderer.clear();
		PipeTransportFluidsRenderer.clear();
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

		PipeTransportRenderer renderer = PipeTransportRenderer.RENDERER_MAP.get(pipe.pipe.transport.getClass());
		if (renderer != null) {
			renderer.render(renderer.useServerTileIfPresent() ? (Pipe) (((IPipeTile) CoreProxy.proxy.getServerTile(pipe)).getPipe()) : pipe.pipe, x, y, z, f);
		}
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
}

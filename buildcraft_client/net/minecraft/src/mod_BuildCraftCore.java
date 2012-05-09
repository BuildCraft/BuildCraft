/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */


package net.minecraft.src;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.src.buildcraft.api.IBlockPipe;
import net.minecraft.src.buildcraft.api.IPipe;
import net.minecraft.src.buildcraft.api.IPipe.DrawingState;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.ClassMapping;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.EntityEnergyLaser;
import net.minecraft.src.buildcraft.core.EntityLaser;
import net.minecraft.src.buildcraft.core.EntityRobot;
import net.minecraft.src.buildcraft.core.IInventoryRenderer;
import net.minecraft.src.buildcraft.core.ITileBufferHolder;
import net.minecraft.src.buildcraft.core.PersistentTile;
import net.minecraft.src.buildcraft.core.PersistentWorld;
import net.minecraft.src.buildcraft.core.RenderEnergyLaser;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;
import net.minecraft.src.buildcraft.core.RenderLaser;
import net.minecraft.src.buildcraft.core.RenderRobot;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.forge.NetworkMod;

import org.lwjgl.opengl.GL11;

public class mod_BuildCraftCore extends NetworkMod {

	public static mod_BuildCraftCore instance;

	BuildCraftCore proxy = new BuildCraftCore();

	public static class EntityRenderIndex {
		public EntityRenderIndex(Block block, int damage) {
			this.block = block;
			this.damage = damage;
		}

		@Override
		public int hashCode() {
			return block.hashCode() + damage;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof EntityRenderIndex))
				return false;

			EntityRenderIndex i = (EntityRenderIndex) o;

			return i.block == block && i.damage == damage;
		}

		Block block;
		int damage;
	}

	public static HashMap<EntityRenderIndex, IInventoryRenderer> blockByEntityRenders = new HashMap<EntityRenderIndex, IInventoryRenderer>();

	public static boolean initialized = false;

	public mod_BuildCraftCore() {
		instance = this;
	}

	public static void initialize() {
		BuildCraftCore.initialize();

		if (!initialized) {
			initializeMarkerMatrix();

			MinecraftForgeClient
			.preloadTexture(BuildCraftCore.customBuildCraftTexture);
			MinecraftForgeClient
			.preloadTexture(BuildCraftCore.customBuildCraftSprites);
			MinecraftForgeClient
			.preloadTexture(BuildCraftCore.externalBuildCraftTexture);
			
			initialized = true;
		}
	}

	@Override
	public void modsLoaded() {
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeModel(this);
		ModLoader.setInGameHook(this, true, true);
	}

	@Override
	public String getVersion() {
		return version();
	}

	public static String version() {
		return DefaultProps.VERSION;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addRenderer(Map map) {
		map.put(EntityBlock.class, new RenderEntityBlock());
		map.put (EntityLaser.class, new RenderLaser());
		map.put (EntityEnergyLaser.class, new RenderEnergyLaser());
		map.put (EntityRobot.class, new RenderRobot());
	}

	@Override
	public boolean renderWorldBlock(RenderBlocks renderblocks,
			IBlockAccess iblockaccess, int i, int j, int k, Block block, int l) {

		if (block.getRenderType() == BuildCraftCore.blockByEntityModel) {
			// renderblocks.renderStandardBlock(block, i, j, k);

		} else if (block.getRenderType() == BuildCraftCore.markerModel) {
			Tessellator tessellator = Tessellator.instance;
			float f = block.getBlockBrightness(iblockaccess, i, j, k);
			if (Block.lightValue[block.blockID] > 0)
				f = 1.0F;
			tessellator.setColorOpaque_F(f, f, f);
			renderMarkerWithMeta(iblockaccess, block, i, j, k,
					iblockaccess.getBlockMetadata(i, j, k));
		} else if (block.getRenderType() == BuildCraftCore.pipeModel) {
			PersistentTile tile = PersistentWorld.getWorld(iblockaccess)
					.getTile(new BlockIndex(i, j, k));

			if (tile == null || !(tile instanceof IPipe))
				legacyPipeRender(renderblocks, iblockaccess, i, j, k, block, l);
			else
				pipeRender(renderblocks, iblockaccess, tile.tile, (IPipe) tile, block, l);
		} else if (block.getRenderType() == BuildCraftCore.oilModel)
			renderblocks.renderBlockFluids(block, i, j, k);

		return true;
	}

	private void pipeRender (RenderBlocks renderblocks,
			IBlockAccess iblockaccess, TileEntity tile, IPipe pipe, Block block, int l) {
		ITileBufferHolder holder = (ITileBufferHolder) tile;

		float minSize = Utils.pipeMinPos;
		float maxSize = Utils.pipeMaxPos;

		pipe.setDrawingState(DrawingState.DrawingPipe);

		pipe.prepareTextureFor(Orientations.Unknown);
		block.setBlockBounds(minSize, minSize, minSize, maxSize, maxSize,
				maxSize);
		renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);

		if (Utils.checkPipesConnections(tile, holder.getTile(Orientations.XNeg))) {
			pipe.prepareTextureFor(Orientations.XNeg);
			block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize,
					maxSize);
			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (Utils.checkPipesConnections(tile, holder.getTile(Orientations.XPos))) {
			pipe.prepareTextureFor(Orientations.XPos);
			block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize,
					maxSize);
			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (Utils.checkPipesConnections(tile, holder.getTile(Orientations.YNeg))) {
			pipe.prepareTextureFor(Orientations.YNeg);
			block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize,
					maxSize);
			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (Utils.checkPipesConnections(tile, holder.getTile(Orientations.YPos))) {
			pipe.prepareTextureFor(Orientations.YPos);
			block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F,
					maxSize);
			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (Utils.checkPipesConnections(tile, holder.getTile(Orientations.ZNeg))) {
			pipe.prepareTextureFor(Orientations.ZNeg);
			block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize,
					minSize);
			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (Utils.checkPipesConnections(tile, holder.getTile(Orientations.ZPos))) {
			pipe.prepareTextureFor(Orientations.ZPos);
			block.setBlockBounds(minSize, minSize, maxSize, maxSize,
					maxSize, 1.0F);
			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		pipe.prepareTextureFor(Orientations.Unknown);
		MinecraftForgeClient.bindTexture(BuildCraftCore.customBuildCraftTexture);
		
		if (pipe.isWired(IPipe.WireColor.Red)) {
			pipe.setDrawingState(DrawingState.DrawingRedWire);
			pipeRedstoneRender(renderblocks, iblockaccess, tile, pipe, block, l,
					Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMinPos, IPipe.WireColor.Red);
		}

		if (pipe.isWired(IPipe.WireColor.Blue)) {
			pipe.setDrawingState(DrawingState.DrawingBlueWire);
			pipeRedstoneRender(renderblocks, iblockaccess, tile, pipe, block, l,
					Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos, IPipe.WireColor.Blue);
		}

		if (pipe.isWired(IPipe.WireColor.Green)) {
			pipe.setDrawingState(DrawingState.DrawingGreenWire);
			pipeRedstoneRender(renderblocks, iblockaccess, tile, pipe, block, l,
					Utils.pipeMaxPos, Utils.pipeMinPos, Utils.pipeMinPos, IPipe.WireColor.Green);
		}

		if (pipe.isWired(IPipe.WireColor.Yellow)) {
			pipe.setDrawingState(DrawingState.DrawingYellowWire);
			pipeRedstoneRender(renderblocks, iblockaccess, tile, pipe, block, l,
					Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, IPipe.WireColor.Yellow);
		}

		if (pipe.hasInterface())
			pipeInterfaceRender(renderblocks, iblockaccess, tile, pipe, block, l);
	}

	private boolean isConnectedWiredPipe (IPipe pipe, TileEntity tile2, IPipe.WireColor color) {
		return pipe.isWireConnectedTo(tile2, color);
	}

	private void pipeRedstoneRender(RenderBlocks renderblocks,
			IBlockAccess iblockaccess, TileEntity tile, IPipe pipe, Block block, int l,
			float cx, float cy, float cz, IPipe.WireColor color) {

		ITileBufferHolder holder = (ITileBufferHolder) tile;

		float minX = Utils.pipeMinPos;
		float minY = Utils.pipeMinPos;
		float minZ = Utils.pipeMinPos;

		float maxX = Utils.pipeMaxPos;
		float maxY = Utils.pipeMaxPos;
		float maxZ = Utils.pipeMaxPos;

		boolean foundX = false, foundY = false, foundZ = false;

		if (isConnectedWiredPipe(pipe, holder.getTile(Orientations.XNeg), color)) {
			minX = 0;
			foundX = true;
		}

		if (isConnectedWiredPipe(pipe, holder.getTile(Orientations.XPos), color)) {
			maxX = 1;
			foundX = true;
		}

		if (isConnectedWiredPipe(pipe, holder.getTile(Orientations.YNeg), color)) {
			minY = 0;
			foundY = true;
		}

		if (isConnectedWiredPipe(pipe, holder.getTile(Orientations.YPos), color)) {
			maxY = 1;
			foundY = true;
		}

		if (isConnectedWiredPipe(pipe, holder.getTile(Orientations.ZNeg), color)) {
			minZ = 0;
			foundZ = true;
		}

		if (isConnectedWiredPipe(pipe, holder.getTile(Orientations.ZPos), color)) {
			maxZ = 1;
			foundZ = true;
		}

		boolean center = false;

		if (minX == 0 && maxX != 1 && (foundY || foundZ))
			if (cx == Utils.pipeMinPos)
				maxX = Utils.pipeMinPos;
			else
				center = true;

		if (minX != 0 && maxX == 1 && (foundY || foundZ))
			if (cx == Utils.pipeMaxPos)
				minX = Utils.pipeMaxPos;
			else
				center = true;

		if (minY == 0 && maxY != 1 && (foundX || foundZ))
			if (cy == Utils.pipeMinPos)
				maxY = Utils.pipeMinPos;
			else
				center = true;

		if (minY != 0 && maxY == 1 && (foundX || foundZ))
			if (cy == Utils.pipeMaxPos)
				minY = Utils.pipeMaxPos;
			else
				center = true;

		if (minZ == 0 && maxZ != 1 && (foundX || foundY))
			if (cz == Utils.pipeMinPos)
				maxZ = Utils.pipeMinPos;
			else
				center = true;

		if (minZ != 0 && maxZ == 1 && (foundX || foundY))
			if (cz == Utils.pipeMaxPos)
				minZ = Utils.pipeMaxPos;
			else
				center = true;

		boolean found = foundX || foundY || foundZ;

		// Z render

		if (minZ != Utils.pipeMinPos || maxZ != Utils.pipeMaxPos || !found) {
			block.setBlockBounds
			(cx == Utils.pipeMinPos ? cx - 0.05F : cx,
			 cy == Utils.pipeMinPos ? cy - 0.05F : cy,
			 minZ,
			 cx == Utils.pipeMinPos ? cx : cx + 0.05F,
			 cy == Utils.pipeMinPos ? cy : cy + 0.05F,
			 maxZ);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		// X render

		if (minX != Utils.pipeMinPos || maxX != Utils.pipeMaxPos || !found) {
			block.setBlockBounds
			(minX,
			 cy == Utils.pipeMinPos ? cy - 0.05F : cy,
			 cz == Utils.pipeMinPos ? cz - 0.05F : cz,
			 maxX,
			 cy == Utils.pipeMinPos ? cy : cy + 0.05F,
			 cz == Utils.pipeMinPos ? cz : cz + 0.05F);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		// Y render

		if (minY != Utils.pipeMinPos || maxY != Utils.pipeMaxPos || !found) {
			block.setBlockBounds
			(cx == Utils.pipeMinPos ? cx - 0.05F : cx,
			 minY,
			 cz == Utils.pipeMinPos ? cz - 0.05F : cz,
			 cx == Utils.pipeMinPos ? cx : cx + 0.05F,
			 maxY,
			 cz == Utils.pipeMinPos ? cz : cz + 0.05F);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (center || !found) {
			block.setBlockBounds
			(cx == Utils.pipeMinPos ? cx - 0.05F : cx,
			 cy == Utils.pipeMinPos ? cy - 0.05F : cy,
			 cz == Utils.pipeMinPos ? cz - 0.05F : cz,
			 cx == Utils.pipeMinPos ? cx : cx + 0.05F,
			 cy == Utils.pipeMinPos ? cy : cy + 0.05F,
			 cz == Utils.pipeMinPos ? cz : cz + 0.05F);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

	}

	private void pipeInterfaceRender (RenderBlocks renderblocks,
			IBlockAccess iblockaccess, TileEntity tile, IPipe pipe, Block block, int l) {

		ITileBufferHolder holder = (ITileBufferHolder) tile;

		pipe.setDrawingState(DrawingState.DrawingGate);

		float min = Utils.pipeMinPos + 0.05F;
		float max = Utils.pipeMaxPos - 0.05F;

		if (!Utils.checkPipesConnections(tile, holder.getTile(Orientations.XNeg))) {
			block.setBlockBounds
			(Utils.pipeMinPos - 0.10F,
			 min,
			 min,
			 Utils.pipeMinPos,
			 max,
			 max);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (!Utils.checkPipesConnections(tile, holder.getTile(Orientations.XPos))) {
			block.setBlockBounds
			(Utils.pipeMaxPos,
			 min,
			 min,
			 Utils.pipeMaxPos + 0.10F,
			 max,
			 max);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (!Utils.checkPipesConnections(tile, holder.getTile(Orientations.YNeg))) {
			block.setBlockBounds
			(min,
			 Utils.pipeMinPos - 0.10F,
			 min,
			 max,
			 Utils.pipeMinPos,
			 max);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (!Utils.checkPipesConnections(tile, holder.getTile(Orientations.YPos))) {
			block.setBlockBounds
			(min,
			 Utils.pipeMaxPos,
			 min,
			 max,
			 Utils.pipeMaxPos + 0.10F,
			 max);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (!Utils.checkPipesConnections(tile, holder.getTile(Orientations.ZNeg))) {
			block.setBlockBounds
			(min,
			 min,
			 Utils.pipeMinPos - 0.10F,
			 max,
			 max,
			 Utils.pipeMinPos);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (!Utils.checkPipesConnections(tile, holder.getTile(Orientations.ZPos))) {
			block.setBlockBounds
			(min,
			 min,
			 Utils.pipeMaxPos,
			 max,
			 max,
			 Utils.pipeMaxPos + 0.10F);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}
	}


	private void legacyPipeRender (RenderBlocks renderblocks,
			IBlockAccess iblockaccess, int i, int j, int k, Block block, int l) {
		float minSize = Utils.pipeMinPos;
		float maxSize = Utils.pipeMaxPos;

		((IBlockPipe) block).prepareTextureFor(iblockaccess, i, j, k,
				Orientations.Unknown);
		block.setBlockBounds(minSize, minSize, minSize, maxSize, maxSize,
				maxSize);
		renderblocks.renderStandardBlock(block, i, j, k);

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i - 1, j, k)) {
			((IBlockPipe) block).prepareTextureFor(iblockaccess, i, j, k,
					Orientations.XNeg);
			block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize,
					maxSize);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i + 1, j, k)) {
			((IBlockPipe) block).prepareTextureFor(iblockaccess, i, j, k,
					Orientations.XPos);
			block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize,
					maxSize);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i, j - 1, k)) {
			((IBlockPipe) block).prepareTextureFor(iblockaccess, i, j, k,
					Orientations.YNeg);
			block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize,
					maxSize);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i, j + 1, k)) {
			((IBlockPipe) block).prepareTextureFor(iblockaccess, i, j, k,
					Orientations.YPos);
			block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F,
					maxSize);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i, j, k - 1)) {
			((IBlockPipe) block).prepareTextureFor(iblockaccess, i, j, k,
					Orientations.ZNeg);
			block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize,
					minSize);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i, j, k + 1)) {
			((IBlockPipe) block).prepareTextureFor(iblockaccess, i, j, k,
					Orientations.ZPos);
			block.setBlockBounds(minSize, minSize, maxSize, maxSize,
					maxSize, 1.0F);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		((IBlockPipe) block).prepareTextureFor(iblockaccess, i, j, k,
				Orientations.Unknown);
	}

	RenderItem itemRenderer = new RenderItem();

	@Override
	public void renderInvBlock(RenderBlocks renderblocks, Block block, int i,
			int j) {
		if (block.getRenderType() == BuildCraftCore.blockByEntityModel) {

			EntityRenderIndex index = new EntityRenderIndex(block, i);

			if (blockByEntityRenders.containsKey(index))
				blockByEntityRenders.get(index).inventoryRender(-0.5, -0.5,
						-0.5, 0, 0);
		} else if (block.getRenderType() == BuildCraftCore.markerModel) {
			// Do nothing here...
		} else if (block.getRenderType() == BuildCraftCore.pipeModel) {
			Tessellator tessellator = Tessellator.instance;

			block.setBlockBounds(Utils.pipeMinPos, 0.0F, Utils.pipeMinPos,
					Utils.pipeMaxPos, 1.0F, Utils.pipeMaxPos);
			block.setBlockBoundsForItemRender();
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, -1F, 0.0F);
			renderblocks.renderBottomFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(0, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 1.0F, 0.0F);
			renderblocks.renderTopFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(1, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, -1F);
			renderblocks.renderEastFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(2, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, 1.0F);
			renderblocks.renderWestFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(3, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(-1F, 0.0F, 0.0F);
			renderblocks.renderNorthFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(4, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(1.0F, 0.0F, 0.0F);
			renderblocks.renderSouthFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(5, i));
			tessellator.draw();
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	public static double frontX[][][] = new double[6][3][4];
	public static double frontZ[][][] = new double[6][3][4];
	public static double frontY[][][] = new double[6][3][4];

	public static double[][] safeClone(double[][] d) {
		double ret[][] = new double[d.length][d[0].length];

		for (int i = 0; i < d.length; ++i)
			for (int j = 0; j < d[0].length; ++j)
				ret[i][j] = d[i][j];

		return ret;
	}

	public static void initializeMarkerMatrix() {
		double frontXBase[][] = { { -0.0625, -0.0625, -0.0625, -0.0625 },
				{ 1, 0, 0, 1 }, { -0.5, -0.5, 0.5, 0.5 } };

		frontX[3] = safeClone(frontXBase);
		rotateFace(frontX[3]);
		rotateFace(frontX[3]);
		rotateFace(frontX[3]);

		frontX[4] = safeClone(frontXBase);
		rotateFace(frontX[4]);

		frontX[5] = safeClone(frontXBase);

		frontX[0] = safeClone(frontXBase);
		rotateFace(frontX[0]);
		rotateFace(frontX[0]);

		double frontZBase[][] = { { -0.5, -0.5, 0.5, 0.5 }, { 1, 0, 0, 1 },
				{ 0.0625, 0.0625, 0.0625, 0.0625 } };

		frontZ[5] = safeClone(frontZBase);

		frontZ[1] = safeClone(frontZBase);
		rotateFace(frontZ[1]);
		rotateFace(frontZ[1]);
		rotateFace(frontZ[1]);

		frontZ[2] = safeClone(frontZBase);
		rotateFace(frontZ[2]);

		frontZ[0] = safeClone(frontZBase);
		rotateFace(frontZ[0]);
		rotateFace(frontZ[0]);

		double frontYBase[][] = { { -0.5, -0.5, 0.5, 0.5 },
				{ -0.0625, -0.0625, -0.0625, -0.0625 },
				{ 0.5, -0.5, -0.5, 0.5 } };

		frontY[4] = safeClone(frontYBase);
		rotateFace(frontY[4]);
		rotateFace(frontY[4]);

		frontY[3] = safeClone(frontYBase);

		frontY[2] = safeClone(frontYBase);
		rotateFace(frontY[2]);

		frontY[1] = safeClone(frontYBase);
		rotateFace(frontY[1]);
		rotateFace(frontY[1]);
		rotateFace(frontY[1]);

	}

	public void renderMarkerWithMeta(IBlockAccess iblockaccess, Block block, double x, double y, double z,
			int meta) {
		Tessellator tessellator = Tessellator.instance;

		int xCoord = (int) x;
		int yCoord = (int) y;
		int zCoord = (int) z;

		int i = block.getBlockTexture(iblockaccess, xCoord, yCoord, zCoord, 1);

		int m = meta;
		int j = (i & 0xf) << 4;
		int k = i & 0xf0;
		float f = j / 256F;
		float f1 = (j + 15.99F) / 256F;
		float f2 = k / 256F;
		float f3 = (k + 15.99F) / 256F;
		double d5 = f + 0.02734375D;
		double d6 = f2 + 0.0234375D;
		double d7 = f + 0.02734375D;
		double d8 = f2 + 0.0234375D;
		x += 0.5D;
		z += 0.5D;

		double s = 0.0625D;

		if (meta == 5) {
			tessellator.addVertexWithUV(x - s, y + 0.5 + s, z - s, d5, d6);
			tessellator.addVertexWithUV(x - s, y + 0.5 + s, z + s, d5, d8);
			tessellator.addVertexWithUV(x + s, y + 0.5 + s, z + s, d7, d8);
			tessellator.addVertexWithUV(x + s, y + 0.5 + s, z - s, d7, d6);
		} else if (meta == 0) {
			tessellator.addVertexWithUV(x + s, y + 0.5 - s, z - s, d7, d6);
			tessellator.addVertexWithUV(x + s, y + 0.5 - s, z + s, d7, d8);
			tessellator.addVertexWithUV(x - s, y + 0.5 - s, z + s, d5, d8);
			tessellator.addVertexWithUV(x - s, y + 0.5 - s, z - s, d5, d6);
		} else if (meta == 2) {
			tessellator.addVertexWithUV(x - s, y + 0.5 - s, z - s, d5, d6);
			tessellator.addVertexWithUV(x - s, y + 0.5 - s, z + s, d5, d8);
			tessellator.addVertexWithUV(x - s, y + 0.5 + s, z + s, d7, d8);
			tessellator.addVertexWithUV(x - s, y + 0.5 + s, z - s, d7, d6);
		} else if (meta == 1) {
			tessellator.addVertexWithUV(x + s, y + 0.5 + s, z - s, d7, d6);
			tessellator.addVertexWithUV(x + s, y + 0.5 + s, z + s, d7, d8);
			tessellator.addVertexWithUV(x + s, y + 0.5 - s, z + s, d5, d8);
			tessellator.addVertexWithUV(x + s, y + 0.5 - s, z - s, d5, d6);
		} else if (meta == 3) {
			tessellator.addVertexWithUV(x - s, y + 0.5 - s, z + s, d5, d6);
			tessellator.addVertexWithUV(x + s, y + 0.5 - s, z + s, d5, d8);
			tessellator.addVertexWithUV(x + s, y + 0.5 + s, z + s, d7, d8);
			tessellator.addVertexWithUV(x - s, y + 0.5 + s, z + s, d7, d6);
		} else if (meta == 4) {
			tessellator.addVertexWithUV(x - s, y + 0.5 + s, z - s, d7, d6);
			tessellator.addVertexWithUV(x + s, y + 0.5 + s, z - s, d7, d8);
			tessellator.addVertexWithUV(x + s, y + 0.5 - s, z - s, d5, d8);
			tessellator.addVertexWithUV(x - s, y + 0.5 - s, z - s, d5, d6);
		}

		i = block.getBlockTexture(iblockaccess, xCoord, yCoord, zCoord, 0);

		j = (i & 0xf) << 4;
		k = i & 0xf0;
		f = j / 256F;
		f1 = (j + 15.99F) / 256F;
		f2 = k / 256F;
		f3 = (k + 15.99F) / 256F;
		d5 = f + 0.02734375D;
		d6 = f2 + 0.0234375D;
		d7 = f + 0.02734375D;
		d8 = f2 + 0.0234375D;

		if (meta == 5 || meta == 4 || meta == 3 || meta == 0) {
			tessellator.addVertexWithUV(x + frontX[m][0][0], y
					+ frontX[m][1][0], z + frontX[m][2][0], f, f2);
			tessellator.addVertexWithUV(x + frontX[m][0][1], y
					+ frontX[m][1][1], z + frontX[m][2][1], f, f3);
			tessellator.addVertexWithUV(x + frontX[m][0][2], y
					+ frontX[m][1][2], z + frontX[m][2][2], f1, f3);
			tessellator.addVertexWithUV(x + frontX[m][0][3], y
					+ frontX[m][1][3], z + frontX[m][2][3], f1, f2);

			tessellator.addVertexWithUV(x - frontX[m][0][3], y
					+ frontX[m][1][3], z + frontX[m][2][3], f1, f2);
			tessellator.addVertexWithUV(x - frontX[m][0][2], y
					+ frontX[m][1][2], z + frontX[m][2][2], f1, f3);
			tessellator.addVertexWithUV(x - frontX[m][0][1], y
					+ frontX[m][1][1], z + frontX[m][2][1], f, f3);
			tessellator.addVertexWithUV(x - frontX[m][0][0], y
					+ frontX[m][1][0], z + frontX[m][2][0], f, f2);
		}

		if (meta == 5 || meta == 2 || meta == 1 || meta == 0) {
			tessellator.addVertexWithUV(x + frontZ[m][0][0], y
					+ frontZ[m][1][0], z + frontZ[m][2][0], f, f2);
			tessellator.addVertexWithUV(x + frontZ[m][0][1], y
					+ frontZ[m][1][1], z + frontZ[m][2][1], f, f3);
			tessellator.addVertexWithUV(x + frontZ[m][0][2], y
					+ frontZ[m][1][2], z + frontZ[m][2][2], f1, f3);
			tessellator.addVertexWithUV(x + frontZ[m][0][3], y
					+ frontZ[m][1][3], z + frontZ[m][2][3], f1, f2);

			tessellator.addVertexWithUV(x + frontZ[m][0][3], y
					+ frontZ[m][1][3], z - frontZ[m][2][3], f1, f2);
			tessellator.addVertexWithUV(x + frontZ[m][0][2], y
					+ frontZ[m][1][2], z - frontZ[m][2][2], f1, f3);
			tessellator.addVertexWithUV(x + frontZ[m][0][1], y
					+ frontZ[m][1][1], z - frontZ[m][2][1], f, f3);
			tessellator.addVertexWithUV(x + frontZ[m][0][0], y
					+ frontZ[m][1][0], z - frontZ[m][2][0], f, f2);
		}

		if (meta == 4 || meta == 3 || meta == 2 || meta == 1) {
			tessellator.addVertexWithUV(x + frontY[m][0][0], y + 0.5
					+ frontY[m][1][0], z + frontY[m][2][0], f, f2);
			tessellator.addVertexWithUV(x + frontY[m][0][1], y + 0.5
					+ frontY[m][1][1], z + frontY[m][2][1], f, f3);
			tessellator.addVertexWithUV(x + frontY[m][0][2], y + 0.5
					+ frontY[m][1][2], z + frontY[m][2][2], f1, f3);
			tessellator.addVertexWithUV(x + frontY[m][0][3], y + 0.5
					+ frontY[m][1][3], z + frontY[m][2][3], f1, f2);

			tessellator.addVertexWithUV(x + frontY[m][0][3], y + 0.5
					- frontY[m][1][3], z + frontY[m][2][3], f1, f2);
			tessellator.addVertexWithUV(x + frontY[m][0][2], y + 0.5
					- frontY[m][1][2], z + frontY[m][2][2], f1, f3);
			tessellator.addVertexWithUV(x + frontY[m][0][1], y + 0.5
					- frontY[m][1][1], z + frontY[m][2][1], f, f3);
			tessellator.addVertexWithUV(x + frontY[m][0][0], y + 0.5
					- frontY[m][1][0], z + frontY[m][2][0], f, f2);
		}
	}

	private static void rotateFace(double[][] face) {
		for (int j = 0; j < 3; ++j) {
			double tmp = face[j][0];
			face[j][0] = face[j][1];
			face[j][1] = face[j][2];
			face[j][2] = face[j][3];
			face[j][3] = tmp;
		}
	}

	long lastReport = 0;

	@Override
	public boolean onTickInGame(float f, Minecraft minecraft) {
		if (BuildCraftCore.trackNetworkUsage) {
			Date d = new Date();

			if (d.getTime() - lastReport > 10000) {
				lastReport = d.getTime();
				int bytes = ClassMapping.report();
				System.out.println ("BuildCraft bandwidth = " + (bytes / 10) + " bytes / second");
				System.out.println ();
			}
		}

		return true;
	}

	/*
	@Override
	public void handlePacket(Packet230ModLoader packet) {
		switch (PacketIds.values()[packet.packetType]) {
		case TileDescription:
			Utils.handleDescriptionPacket(packet, ModLoader.getMinecraftInstance().theWorld);
			break;
		case TileUpdate:
			Utils.handleUpdatePacket(packet,  ModLoader.getMinecraftInstance().theWorld);
			break;

		}
	} */

	@Override
	public void load() {
		BuildCraftCore.load();
	}

	@Override public boolean clientSideRequired() { return true; }
	@Override public boolean serverSideRequired() { return true; }

}

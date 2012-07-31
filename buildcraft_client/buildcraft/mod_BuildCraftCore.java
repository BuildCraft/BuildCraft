/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.RenderItem;
import net.minecraft.src.Tessellator;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.forge.NetworkMod;

import org.lwjgl.opengl.GL11;

import buildcraft.BuildCraftCore;
import buildcraft.core.ClassMapping;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityBlock;
import buildcraft.core.EntityEnergyLaser;
import buildcraft.core.EntityLaser;
import buildcraft.core.EntityRobot;
import buildcraft.core.IInventoryRenderer;
import buildcraft.core.RenderEnergyLaser;
import buildcraft.core.RenderEntityBlock;
import buildcraft.core.RenderLaser;
import buildcraft.core.RenderRobot;
import buildcraft.core.Utils;
import buildcraft.core.utils.Localization;

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

			MinecraftForgeClient.preloadTexture(DefaultProps.TEXTURE_BLOCKS);
			MinecraftForgeClient.preloadTexture(DefaultProps.TEXTURE_ITEMS);
			
			//Initialize localization
			Localization.addLocalization("/lang/buildcraft/", DefaultProps.DEFAULT_LANGUAGE);

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
		map.put(EntityLaser.class, new RenderLaser());
		map.put(EntityEnergyLaser.class, new RenderEnergyLaser());
		map.put(EntityRobot.class, new RenderRobot());
	}

	@Override
	public boolean renderWorldBlock(RenderBlocks renderblocks, IBlockAccess iblockaccess, int i, int j, int k, Block block, int l) {

		if (block.getRenderType() == BuildCraftCore.blockByEntityModel) {
			// renderblocks.renderStandardBlock(block, i, j, k);

		} else if (block.getRenderType() == BuildCraftCore.markerModel) {
			
			Tessellator tessellator = Tessellator.instance;
			float f = block.getBlockBrightness(iblockaccess, i, j, k);
			if (Block.lightValue[block.blockID] > 0)
				f = 1.0F;
			tessellator.setColorOpaque_F(f, f, f);
			renderMarkerWithMeta(iblockaccess, block, i, j, k, iblockaccess.getBlockMetadata(i, j, k));
			
		} else if (block.getRenderType() == BuildCraftCore.legacyPipeModel) {
			
				legacyPipeRender(renderblocks, iblockaccess, i, j, k, block, l);
			
		} else if (block.getRenderType() == BuildCraftCore.oilModel)
			renderblocks.renderBlockFluids(block, i, j, k);

		return true;
	}

	private void legacyPipeRender(RenderBlocks renderblocks, IBlockAccess iblockaccess, int i, int j, int k, Block block, int l) {
		float minSize = Utils.pipeMinPos;
		float maxSize = Utils.pipeMaxPos;

		block.setBlockBounds(minSize, minSize, minSize, maxSize, maxSize, maxSize);
		renderblocks.renderStandardBlock(block, i, j, k);

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i - 1, j, k)) {
			block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize, maxSize);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i + 1, j, k)) {
			block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize, maxSize);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i, j - 1, k)) {
			block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize, maxSize);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i, j + 1, k)) {
			block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F, maxSize);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i, j, k - 1)) {
			block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize, minSize);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i, j, k + 1)) {
			block.setBlockBounds(minSize, minSize, maxSize, maxSize, maxSize, 1.0F);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	RenderItem itemRenderer = new RenderItem();

	@Override
	public void renderInvBlock(RenderBlocks renderblocks, Block block, int i, int j) {
		if (block.getRenderType() == BuildCraftCore.blockByEntityModel) {

			EntityRenderIndex index = new EntityRenderIndex(block, i);

			if (blockByEntityRenders.containsKey(index))
				blockByEntityRenders.get(index).inventoryRender(-0.5, -0.5, -0.5, 0, 0);
		} else if (block.getRenderType() == BuildCraftCore.markerModel) {
			// Do nothing here...
		} else if (block.getRenderType() == BuildCraftCore.legacyPipeModel) {
			Tessellator tessellator = Tessellator.instance;

			block.setBlockBounds(Utils.pipeMinPos, 0.0F, Utils.pipeMinPos, Utils.pipeMaxPos, 1.0F, Utils.pipeMaxPos);
			block.setBlockBoundsForItemRender();
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, -1F, 0.0F);
			renderblocks.renderBottomFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(0, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 1.0F, 0.0F);
			renderblocks.renderTopFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(1, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, -1F);
			renderblocks.renderEastFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(2, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, 1.0F);
			renderblocks.renderWestFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(3, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(-1F, 0.0F, 0.0F);
			renderblocks.renderNorthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(4, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(1.0F, 0.0F, 0.0F);
			renderblocks.renderSouthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(5, i));
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
		double frontXBase[][] = { { -0.0625, -0.0625, -0.0625, -0.0625 }, { 1, 0, 0, 1 }, { -0.5, -0.5, 0.5, 0.5 } };

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

		double frontZBase[][] = { { -0.5, -0.5, 0.5, 0.5 }, { 1, 0, 0, 1 }, { 0.0625, 0.0625, 0.0625, 0.0625 } };

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

		double frontYBase[][] = { { -0.5, -0.5, 0.5, 0.5 }, { -0.0625, -0.0625, -0.0625, -0.0625 }, { 0.5, -0.5, -0.5, 0.5 } };

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

	public void renderMarkerWithMeta(IBlockAccess iblockaccess, Block block, double x, double y, double z, int meta) {
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

		tessellator.setBrightness(block.getMixedBrightnessForBlock(iblockaccess, xCoord, yCoord, zCoord));

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
			tessellator.addVertexWithUV(x + frontX[m][0][0], y + frontX[m][1][0], z + frontX[m][2][0], f, f2);
			tessellator.addVertexWithUV(x + frontX[m][0][1], y + frontX[m][1][1], z + frontX[m][2][1], f, f3);
			tessellator.addVertexWithUV(x + frontX[m][0][2], y + frontX[m][1][2], z + frontX[m][2][2], f1, f3);
			tessellator.addVertexWithUV(x + frontX[m][0][3], y + frontX[m][1][3], z + frontX[m][2][3], f1, f2);

			tessellator.addVertexWithUV(x - frontX[m][0][3], y + frontX[m][1][3], z + frontX[m][2][3], f1, f2);
			tessellator.addVertexWithUV(x - frontX[m][0][2], y + frontX[m][1][2], z + frontX[m][2][2], f1, f3);
			tessellator.addVertexWithUV(x - frontX[m][0][1], y + frontX[m][1][1], z + frontX[m][2][1], f, f3);
			tessellator.addVertexWithUV(x - frontX[m][0][0], y + frontX[m][1][0], z + frontX[m][2][0], f, f2);
		}

		if (meta == 5 || meta == 2 || meta == 1 || meta == 0) {
			tessellator.addVertexWithUV(x + frontZ[m][0][0], y + frontZ[m][1][0], z + frontZ[m][2][0], f, f2);
			tessellator.addVertexWithUV(x + frontZ[m][0][1], y + frontZ[m][1][1], z + frontZ[m][2][1], f, f3);
			tessellator.addVertexWithUV(x + frontZ[m][0][2], y + frontZ[m][1][2], z + frontZ[m][2][2], f1, f3);
			tessellator.addVertexWithUV(x + frontZ[m][0][3], y + frontZ[m][1][3], z + frontZ[m][2][3], f1, f2);

			tessellator.addVertexWithUV(x + frontZ[m][0][3], y + frontZ[m][1][3], z - frontZ[m][2][3], f1, f2);
			tessellator.addVertexWithUV(x + frontZ[m][0][2], y + frontZ[m][1][2], z - frontZ[m][2][2], f1, f3);
			tessellator.addVertexWithUV(x + frontZ[m][0][1], y + frontZ[m][1][1], z - frontZ[m][2][1], f, f3);
			tessellator.addVertexWithUV(x + frontZ[m][0][0], y + frontZ[m][1][0], z - frontZ[m][2][0], f, f2);
		}

		if (meta == 4 || meta == 3 || meta == 2 || meta == 1) {
			tessellator.addVertexWithUV(x + frontY[m][0][0], y + 0.5 + frontY[m][1][0], z + frontY[m][2][0], f, f2);
			tessellator.addVertexWithUV(x + frontY[m][0][1], y + 0.5 + frontY[m][1][1], z + frontY[m][2][1], f, f3);
			tessellator.addVertexWithUV(x + frontY[m][0][2], y + 0.5 + frontY[m][1][2], z + frontY[m][2][2], f1, f3);
			tessellator.addVertexWithUV(x + frontY[m][0][3], y + 0.5 + frontY[m][1][3], z + frontY[m][2][3], f1, f2);

			tessellator.addVertexWithUV(x + frontY[m][0][3], y + 0.5 - frontY[m][1][3], z + frontY[m][2][3], f1, f2);
			tessellator.addVertexWithUV(x + frontY[m][0][2], y + 0.5 - frontY[m][1][2], z + frontY[m][2][2], f1, f3);
			tessellator.addVertexWithUV(x + frontY[m][0][1], y + 0.5 - frontY[m][1][1], z + frontY[m][2][1], f, f3);
			tessellator.addVertexWithUV(x + frontY[m][0][0], y + 0.5 - frontY[m][1][0], z + frontY[m][2][0], f, f2);
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
				System.out.println("BuildCraft bandwidth = " + (bytes / 10) + " bytes / second");
				System.out.println();
			}
		}

		return true;
	}

	/*
	 * @Override public void handlePacket(Packet230ModLoader packet) { switch
	 * (PacketIds.values()[packet.packetType]) { case TileDescription:
	 * Utils.handleDescriptionPacket(packet,
	 * ModLoader.getMinecraftInstance().theWorld); break; case TileUpdate:
	 * Utils.handleUpdatePacket(packet,
	 * ModLoader.getMinecraftInstance().theWorld); break;
	 * 
	 * } }
	 */

	@Override
	public void load() {
		BuildCraftCore.load();
	}

	@Override
	public boolean clientSideRequired() {
		return true;
	}

	@Override
	public boolean serverSideRequired() {
		return true;
	}

}

package buildcraft.transport.render;

import org.lwjgl.opengl.GL11;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.IntHashMap;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import buildcraft.core.CoreConstants;
import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.utils.FluidRenderData;

public class PipeTransportFluidsRenderer extends PipeTransportRenderer<PipeTransportFluids> {
	private static final IntHashMap displayFluidLists = new IntHashMap();
	private static final TIntHashSet displayFluidListsSet = new TIntHashSet();

	private static final int LIQUID_STAGES = 40;
	private static final int[] angleY = {0, 0, 270, 90, 0, 180};
	private static final int[] angleZ = {90, 270, 0, 0, 0, 0};

	private class DisplayFluidList {
		public int[] sideHorizontal = new int[LIQUID_STAGES];
		public int[] sideVertical = new int[LIQUID_STAGES];
		public int[] centerHorizontal = new int[LIQUID_STAGES];
		public int[] centerVertical = new int[LIQUID_STAGES];
	}

	protected static void clear() {
		TIntIterator i = displayFluidListsSet.iterator();

		while (i.hasNext()) {
			GL11.glDeleteLists(i.next(), 1);
		}

		displayFluidListsSet.clear();
		displayFluidLists.clearMap();
	}

	@Override
	public boolean useServerTileIfPresent() {
		return false;
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
		displayFluidListsSet.add(listId);

		RenderEntityBlock.RenderInfo block = new RenderEntityBlock.RenderInfo();

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

	@Override
	public void render(Pipe<PipeTransportFluids> pipe, double x, double y, double z, float f) {
		PipeTransportFluids trans = pipe.transport;

		boolean needsRender = false;
		FluidRenderData renderData = trans.renderCache;

		for (int i = 0; i < 7; ++i) {
			if (renderData.amount[i] > 0) {
				needsRender = true;
				break;
			}
		}

		if (!needsRender) {
			return;
		}
        
        if (pipe.container == null) {
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
}

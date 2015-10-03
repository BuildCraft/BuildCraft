package buildcraft.transport.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;

public class PipeTransportPowerRenderer extends PipeTransportRenderer<PipeTransportPower> {
	public static final int POWER_STAGES = 256;
	public static final int[] displayPowerList = new int[POWER_STAGES];
	public static final int[] displayPowerListOverload = new int[POWER_STAGES];
	private static final int[] angleY = {0, 0, 270, 90, 0, 180};
	private static final int[] angleZ = {90, 270, 0, 0, 0, 0};
	private static final float POWER_MAGIC = 0.7F; // Math.pow(displayPower, POWER_MAGIC)

	private static boolean initialized = false;

	protected static void clear() {
		if (initialized) {
			for (int i = 0; i < POWER_STAGES; i++) {
				GL11.glDeleteLists(displayPowerList[i], 1);
				GL11.glDeleteLists(displayPowerListOverload[i], 1);
			}
		}

		initialized = false;
	}

	public boolean useServerTileIfPresent() {
		return true;
	}

	private void initializeDisplayPowerList(World world) {
		if (initialized) {
			return;
		}

		initialized = true;

		RenderEntityBlock.RenderInfo block = new RenderEntityBlock.RenderInfo();
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
	public void render(Pipe<PipeTransportPower> pipe, double x, double y, double z, float f) {
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
}

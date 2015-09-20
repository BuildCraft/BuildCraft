package buildcraft.transport.pluggable;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.transport.PipeIconProvider;

public class PlugPluggable extends PipePluggable {
	protected static final class PlugPluggableRenderer implements IPipePluggableRenderer {
		public static final IPipePluggableRenderer INSTANCE = new PlugPluggableRenderer();
		private float zFightOffset = 1 / 4096.0F;

		@Override
		public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, ITextureStates blockStateMachine, int renderPass, int x, int y, int z) {
			if (renderPass != 0) {
				return;
			}

			float[][] zeroState = new float[3][2];

			// X START - END
			zeroState[0][0] = 0.25F + zFightOffset;
			zeroState[0][1] = 0.75F - zFightOffset;
			// Y START - END
			zeroState[1][0] = 0.125F;
			zeroState[1][1] = 0.251F;
			// Z START - END
			zeroState[2][0] = 0.25F + zFightOffset;
			zeroState[2][1] = 0.75F - zFightOffset;

			blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipePlug.ordinal())); // Structure Pipe

			float[][] rotated = MatrixTranformations.deepClone(zeroState);
			MatrixTranformations.transform(rotated, side);

			renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
			renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);

			// X START - END
			zeroState[0][0] = 0.25F + 0.125F / 2 + zFightOffset;
			zeroState[0][1] = 0.75F - 0.125F / 2 + zFightOffset;
			// Y START - END
			zeroState[1][0] = 0.25F;
			zeroState[1][1] = 0.25F + 0.125F;
			// Z START - END
			zeroState[2][0] = 0.25F + 0.125F / 2;
			zeroState[2][1] = 0.75F - 0.125F / 2;

			rotated = MatrixTranformations.deepClone(zeroState);
			MatrixTranformations.transform(rotated, side);

			renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
			renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);
		}
	}

	public PlugPluggable() {

	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

	}

	@Override
	public ItemStack[] getDropItems(IPipeTile pipe) {
		return new ItemStack[]{new ItemStack(BuildCraftTransport.plugItem)};
	}

	@Override
	public boolean isBlocking(IPipeTile pipe, ForgeDirection direction) {
		return true;
	}

	@Override
	public AxisAlignedBB getBoundingBox(ForgeDirection side) {
		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = 0.25F;
		bounds[0][1] = 0.75F;
		// Y START - END
		bounds[1][0] = 0.125F;
		bounds[1][1] = 0.251F;
		// Z START - END
		bounds[2][0] = 0.25F;
		bounds[2][1] = 0.75F;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getBoundingBox(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	@Override
	public IPipePluggableRenderer getRenderer() {
		return PlugPluggableRenderer.INSTANCE;
	}

	@Override
	public void writeData(ByteBuf data) {

	}

	@Override
	public void readData(ByteBuf data) {

	}

	@Override
	public boolean requiresRenderUpdate(PipePluggable o) {
		return false;
	}
}

package buildcraft.transport.pluggable;

import org.lwjgl.opengl.GL11;
import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableDynamicRenderer;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.render.FakeBlock;
import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.core.lib.utils.MatrixTransformations;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.PipeIconProvider;

public class BreakerPluggable extends PipePluggable {
	private IPipeTile container;
	private boolean set;

	protected static final class BreakerPluggableRenderer implements IPipePluggableRenderer, IPipePluggableDynamicRenderer {
		@Override
		public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, ITextureStates blockStateMachine, int renderPass, int x, int y, int z) {
			if (renderPass != 0) {
				return;
			}

			float[][] zeroState = new float[3][2];

			FakeBlock.INSTANCE.getTextureState().set(PipeIconProvider.TYPE.PipeBreaker.getIcon());

			// X START - END
			zeroState[2][0] = 0.25F;
			zeroState[2][1] = 0.75F;
			// Y START - END
			zeroState[1][0] = 0.1875F;
			zeroState[1][1] = 0.251F;
			// Z START - END
			zeroState[0][0] = 0.3125F;
			zeroState[0][1] = 0.6875F;

			float[][] rotated = MatrixTransformations.deepClone(zeroState);
			MatrixTransformations.transform(rotated, side);

			renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
			renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);
		}

		@Override
		public void renderPluggable(IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, double x, double y, double z) {
			GL11.glPushMatrix();
			GL11.glColor3f(1, 1, 1);
			GL11.glTranslatef((float) x, (float) y, (float) z);

			BreakerPluggable breakerPluggable = (BreakerPluggable) pipePluggable;
			boolean invert = !(breakerPluggable.set ^ (side.ordinal() < 2));

			RenderEntityBlock.RenderInfo renderBox = new RenderEntityBlock.RenderInfo();
			renderBox.texture = PipeIconProvider.TYPE.PipeBreakerHandle.getIcon();

			renderBox.setRenderAllSides();
			renderBox.renderSide[side.ordinal() ^ 1] = false;

			float[][] zeroState = new float[3][2];

			// Z START - END
			zeroState[2][0] = 0.3125F + (invert ? 0.3125F : 0);
			zeroState[2][1] = zeroState[2][0] + 0.0625F;
			// Y START - END
			zeroState[1][0] = 0.125F + 0.03125F;
			zeroState[1][1] = 0.1875F;
			// X START - END
			zeroState[0][0] = 0.375F;
			zeroState[0][1] = 0.625F;

			float[][] rotated = MatrixTransformations.deepClone(zeroState);
			MatrixTransformations.transform(rotated, side);

			renderBox.setBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
			RenderEntityBlock.INSTANCE.renderBlock(renderBox);

			// Z START - END
			zeroState[2][0] = 0.375F + (invert ? 0.125F : 0);
			zeroState[2][1] = zeroState[2][0] + 0.125F;
			// X START - END
			zeroState[0][0] = 0.375F;
			zeroState[0][1] = zeroState[0][0] + 0.0625F;

			rotated = MatrixTransformations.deepClone(zeroState);
			MatrixTransformations.transform(rotated, side);

			renderBox.setBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
			RenderEntityBlock.INSTANCE.renderBlock(renderBox);

			// X START - END
			zeroState[0][1] = 0.625F;
			zeroState[0][0] = zeroState[0][1] - 0.0625F;

			rotated = MatrixTransformations.deepClone(zeroState);
			MatrixTransformations.transform(rotated, side);

			renderBox.setBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
			RenderEntityBlock.INSTANCE.renderBlock(renderBox);
			GL11.glPopMatrix();
		}
	}

	public boolean isSet() {
		return set;
	}

	public BreakerPluggable() {

	}

	@Override
	public void validate(IPipeTile pipe, ForgeDirection direction) {
		this.container = pipe;
	}

	@Override
	public void invalidate() {
		this.container = null;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("set", set);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		set = nbt.getBoolean("set");
	}

	@Override
	public ItemStack[] getDropItems(IPipeTile pipe) {
		return new ItemStack[]{new ItemStack(BuildCraftTransport.breakerItem)};
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
		bounds[1][0] = 0.125F + 0.03125F;
		bounds[1][1] = 0.251F;
		// Z START - END
		bounds[2][0] = 0.3125F;
		bounds[2][1] = 0.6875F;

		MatrixTransformations.transform(bounds, side);
		return AxisAlignedBB.getBoundingBox(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IPipePluggableRenderer getRenderer() {
		return new BreakerPluggableRenderer();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IPipePluggableDynamicRenderer getDynamicRenderer() {
		return new BreakerPluggableRenderer();
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeBoolean(set);
	}

	@Override
	public void readData(ByteBuf data) {
		set = data.readBoolean();
	}

	@Override
	public boolean requiresRenderUpdate(PipePluggable o) {
		return false;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, ForgeDirection direction) {
		if (!player.isSneaking() && !Utils.isFakePlayer(player)) {
			set = !set;
			if (container != null) {
				container.scheduleRenderUpdate();
			}
			return true;
		}
		return false;
	}
}

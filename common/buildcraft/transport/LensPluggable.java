package buildcraft.transport;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeContainer;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.utils.ColorUtils;
import buildcraft.core.utils.MatrixTranformations;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.render.FakeBlock;

public class LensPluggable extends PipePluggable {
	@SideOnly(Side.CLIENT)
	public final LensPluggableRenderer RENDERER = new LensPluggableRenderer();

	private int color;
	private ForgeDirection side;

	public class LensPluggableRenderer implements IPipePluggableRenderer {
		private static final float zFightOffset = 1 / 4096.0F;

		@Override
		public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, ITextureStates blockStateMachine, int renderPass, int x, int y, int z) {
			float[][] zeroState = new float[3][2];

			// X START - END
			zeroState[0][0] = 0.1875F;
			zeroState[0][1] = 0.8125F;
			// Y START - END
			zeroState[1][0] = 0.000F;
			zeroState[1][1] = 0.125F;
			// Z START - END
			zeroState[2][0] = 0.1875F;
			zeroState[2][1] = 0.8125F;

			if (renderPass == 1) {
				for (int i = 0; i < 3; i++) {
					zeroState[i][0] += zFightOffset;
					zeroState[i][1] -= zFightOffset;
				}
				blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeLensOverlay.ordinal()));
				((FakeBlock) blockStateMachine).setColor(ColorUtils.getRGBColor(15 - ((LensPluggable) pipePluggable).color));
			} else {
				blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeLens.ordinal()));
			}

			float[][] rotated = MatrixTranformations.deepClone(zeroState);
			MatrixTranformations.transform(rotated, side);

			renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
			renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);

			((FakeBlock) blockStateMachine).setColor(0xFFFFFF);
		}
	}

	public LensPluggable() {

	}

	public LensPluggable(ItemStack stack) {
		color = stack.getItemDamage() & 15;
	}

	@Override
	public void validate(IPipeContainer pipe, ForgeDirection direction) {
		side = direction;
	}

	@Override
	public ItemStack[] getDropItems(IPipeContainer pipe) {
		return new ItemStack[]{ new ItemStack(BuildCraftTransport.lensItem, 1, color) };
	}

	@Override
	public boolean isBlocking(IPipeContainer pipe, ForgeDirection direction) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(ForgeDirection side) {
		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = 0.25F - 0.0625F;
		bounds[0][1] = 0.75F + 0.0625F;
		// Y START - END
		bounds[1][0] = 0.000F;
		bounds[1][1] = 0.125F;
		// Z START - END
		bounds[2][0] = 0.25F - 0.0625F;
		bounds[2][1] = 0.75F + 0.0625F;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getBoundingBox(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	@Override
	public IPipePluggableRenderer getRenderer() {
		return RENDERER;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		color = tag.getByte("c");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		tag.setByte("c", (byte) color);
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeByte(color);
	}

	@Override
	public void readData(ByteBuf data) {
		color = data.readByte();
	}

	private void color(TravelingItem item) {
		if ((item.toCenter && item.input.getOpposite() == side)
				|| (!item.toCenter && item.output == side)) {
			item.color = EnumColor.fromId(color);
		}
	}

	public void eventHandler(PipeEventItem.ReachedEnd event) {
		color(event.item);
	}

	public void eventHandler(PipeEventItem.Entered event) {
		color(event.item);
	}
}

package buildcraft.transport.pluggable;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.render.FakeBlock;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;

public class LensPluggable extends PipePluggable {
	public int color;
	public boolean isFilter;
	protected IPipeTile container;
	private ForgeDirection side;

	private static final class LensPluggableRenderer implements IPipePluggableRenderer {
		public static final IPipePluggableRenderer INSTANCE = new LensPluggableRenderer();
		private static final float zFightOffset = 1 / 4096.0F;

		private LensPluggableRenderer() {

		}

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
				int color = ((LensPluggable) pipePluggable).color;

				blockStateMachine.setRenderMask(1 << side.ordinal() | (1 << (side.ordinal() ^ 1)));

				for (int i = 0; i < 3; i++) {
					zeroState[i][0] += zFightOffset;
					zeroState[i][1] -= zFightOffset;
				}

				if (color == -1) {
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeLensClearOverlay.ordinal()));
				} else {
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeLensOverlay.ordinal()));
					((FakeBlock) blockStateMachine).setColor(ColorUtils.getRGBColor(15 - color));
				}

				blockStateMachine.setRenderAllSides();
			} else {
				if (((LensPluggable) pipePluggable).isFilter) {
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeFilter.ordinal()));
				} else {
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeLens.ordinal()));
				}
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
		isFilter = stack.getItemDamage() >= 16;
		if (stack.getItemDamage() >= 32) {
			isFilter = stack.getItemDamage() == 33;
			color = -1;
		}
	}

	@Override
	public void validate(IPipeTile pipe, ForgeDirection direction) {
		this.container = pipe;
		this.side = direction;
	}

	@Override
	public void invalidate() {
		this.container = null;
		this.side = ForgeDirection.UNKNOWN;
	}

	@Override
	public ItemStack[] getDropItems(IPipeTile pipe) {
		int meta = color | (isFilter ? 16 : 0);
		if (color == -1) {
			meta = isFilter ? 33 : 32;
		}

		return new ItemStack[]{new ItemStack(BuildCraftTransport.lensItem, 1, meta)};
	}

	@Override
	public boolean isBlocking(IPipeTile pipe, ForgeDirection direction) {
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
		return LensPluggableRenderer.INSTANCE;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		color = tag.getByte("c");
		isFilter = tag.getBoolean("f");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		tag.setByte("c", (byte) color);
		tag.setBoolean("f", isFilter);
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeByte(((color + 1) & 0x1F) | (isFilter ? 0x20 : 0));
	}

	@Override
	public void readData(ByteBuf data) {
		int flags = data.readUnsignedByte();
		color = (flags & 0x1F) - 1;
		isFilter = (flags & 0x20) > 0;
	}

	@Override
	public boolean requiresRenderUpdate(PipePluggable o) {
		LensPluggable other = (LensPluggable) o;
		return other.color != color || other.isFilter != isFilter;
	}

	private void color(TravelingItem item) {
		if ((item.toCenter && item.input.getOpposite() == side)
				|| (!item.toCenter && item.output == side)) {
			if (color == -1) {
				item.color = null;
			} else {
				item.color = EnumColor.fromId(color);
			}
		}
	}

	public void eventHandler(PipeEventItem.ReachedEnd event) {
		if (!isFilter) {
			color(event.item);
		}
	}

	public void eventHandler(PipeEventItem.Entered event) {
		if (!isFilter) {
			color(event.item);
		}
	}
}

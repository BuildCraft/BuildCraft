package buildcraft.transport.pluggable;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;

import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyHandler;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.render.FakeBlock;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.transport.PipeIconProvider;

public class PowerAdapterPluggable extends PipePluggable implements IEnergyHandler {
	private static final int MAX_POWER = 40;
	private IPipeTile container;

	protected static final class PowerAdapterPluggableRenderer implements IPipePluggableRenderer {
		private float zFightOffset = 1 / 4096.0F;

		@Override
		public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, ITextureStates blockStateMachine, int renderPass, int x, int y, int z) {
			if (renderPass != 0) {
				return;
			}

			float[][] zeroState = new float[3][2];

			IIcon[] icons = FakeBlock.INSTANCE.getTextureState().popArray();
			int bottom = side.ordinal();

			for (int i = 0; i < 6; i++) {
				icons[i] = BuildCraftTransport.instance.pipeIconProvider.getIcon(
						(i & 6) == (bottom & 6) ? PipeIconProvider.TYPE.PipePowerAdapterBottom.ordinal() : PipeIconProvider.TYPE.PipePowerAdapterSide.ordinal()
				);
			}

			// X START - END
			zeroState[0][0] = 0.1875F;
			zeroState[0][1] = 0.8125F;
			// Y START - END
			zeroState[1][0] = 0.000F;
			zeroState[1][1] = 0.1251F;
			// Z START - END
			zeroState[2][0] = 0.1875F;
			zeroState[2][1] = 0.8125F;

			float[][] rotated = MatrixTranformations.deepClone(zeroState);
			MatrixTranformations.transform(rotated, side);

			renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
			renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);

			icons[bottom] = icons[bottom ^ 1] = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipePowerAdapterTop.ordinal());

			// X START - END
			zeroState[0][0] = 0.25F + zFightOffset;
			zeroState[0][1] = 0.75F - zFightOffset;
			// Y START - END
			zeroState[1][0] = 0.125F;
			zeroState[1][1] = 0.25F + zFightOffset;
			// Z START - END
			zeroState[2][0] = 0.25F + zFightOffset;
			zeroState[2][1] = 0.75F - zFightOffset;

			rotated = MatrixTranformations.deepClone(zeroState);
			MatrixTranformations.transform(rotated, side);

			renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
			renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);

			FakeBlock.INSTANCE.getTextureState().pushArray();
		}
	}

	public PowerAdapterPluggable() {

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

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

	}

	@Override
	public ItemStack[] getDropItems(IPipeTile pipe) {
		return new ItemStack[]{new ItemStack(BuildCraftTransport.powerAdapterItem)};
	}

	@Override
	public boolean isBlocking(IPipeTile pipe, ForgeDirection direction) {
		return true;
	}

	@Override
	public AxisAlignedBB getBoundingBox(ForgeDirection side) {
		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = 0.1875F;
		bounds[0][1] = 0.8125F;
		// Y START - END
		bounds[1][0] = 0.000F;
		bounds[1][1] = 0.251F;
		// Z START - END
		bounds[2][0] = 0.1875F;
		bounds[2][1] = 0.8125F;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getBoundingBox(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	@Override
	public IPipePluggableRenderer getRenderer() {
		return new PowerAdapterPluggableRenderer();
	}

	@Override
	public void writeData(ByteBuf data) {

	}

	@Override
	public void readData(ByteBuf data) {

	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		int maxR = Math.min(MAX_POWER, maxReceive);
		if (container != null && container.getPipe() instanceof IEnergyHandler) {
			int energyCanReceive = ((IEnergyHandler) container.getPipe()).receiveEnergy(from, maxR, true);
			if (!simulate) {
				return ((IEnergyHandler) container.getPipe()).receiveEnergy(from, energyCanReceive, false);
			} else {
				return energyCanReceive;
			}
		}
		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		if (container.getPipe() instanceof IEnergyHandler) {
			return ((IEnergyHandler) container.getPipe()).getEnergyStored(from);
		} else {
			return 0;
		}
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		if (container.getPipe() instanceof IEnergyHandler) {
			return ((IEnergyHandler) container.getPipe()).getMaxEnergyStored(from);
		} else {
			return 0;
		}
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public boolean requiresRenderUpdate(PipePluggable o) {
		return false;
	}
}

package buildcraft.robots;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyReceiver;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableItem;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.utils.MatrixTranformations;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.TileGenericPipe;

public class RobotStationPluggable extends PipePluggable implements IPipePluggableItem, IEnergyReceiver {
	public class RobotStationPluggableRenderer implements IPipePluggableRenderer {
		private float zFightOffset = 1 / 4096.0F;

		private void robotStationPartRender(RenderBlocks renderblocks, RobotStationState state,
												ForgeDirection side, ITextureStates blockStateMachine, int x, int y, int z,
												float xStart, float xEnd, float yStart, float yEnd, float zStart,
												float zEnd) {

			float[][] zeroState = new float[3][2];
			// X START - END
			zeroState[0][0] = xStart + zFightOffset;
			zeroState[0][1] = xEnd - zFightOffset;
			// Y START - END
			zeroState[1][0] = yStart;
			zeroState[1][1] = yEnd;
			// Z START - END
			zeroState[2][0] = zStart + zFightOffset;
			zeroState[2][1] = zEnd - zFightOffset;

			switch (state) {
				case None:
				case Available:
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider
							.getIcon(PipeIconProvider.TYPE.PipeRobotStation.ordinal()));
					break;
				case Reserved:
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider
							.getIcon(PipeIconProvider.TYPE.PipeRobotStationReserved.ordinal()));
					break;
				case Linked:
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider
							.getIcon(PipeIconProvider.TYPE.PipeRobotStationLinked.ordinal()));
					break;
			}

			float[][] rotated = MatrixTranformations.deepClone(zeroState);
			MatrixTranformations.transform(rotated, side);

			renderblocks.setRenderBounds(rotated[0][0], rotated[1][0],
					rotated[2][0], rotated[0][1], rotated[1][1],
					rotated[2][1]);
			renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);
		}

		@Override
		public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, ITextureStates blockStateMachine, int renderPass, int x, int y, int z) {
			if (renderPass != 0) {
				return;
			}

			RobotStationState state = ((RobotStationPluggable) pipePluggable).renderState;

			//float width = 0.075F;

			robotStationPartRender (renderblocks, state, side, blockStateMachine, x, y, z,
					0.45F, 0.55F,
					0.0F, 0.224F,
					0.45F, 0.55F);


		/*pipeRobotStationPartRender (renderblocks, block, state, x, y, z,
				0.25F, 0.75F,
				0.025F, 0.224F,
				0.25F, 0.25F + width);

		pipeRobotStationPartRender (renderblocks, block, state, x, y, z,
				0.25F, 0.75F,
				0.025F, 0.224F,
				0.75F - width, 0.75F);

		pipeRobotStationPartRender (renderblocks, block, state, x, y, z,
				0.25F, 0.25F + width,
				0.025F, 0.224F,
				0.25F + width, 0.75F - width);

		pipeRobotStationPartRender (renderblocks, block, state, x, y, z,
				0.75F - width, 0.75F,
				0.025F, 0.224F,
				0.25F + width, 0.75F - width);*/

			float[][] zeroState = new float[3][2];


			// X START - END
			zeroState[0][0] = 0.25F + zFightOffset;
			zeroState[0][1] = 0.75F - zFightOffset;
			// Y START - END
			zeroState[1][0] = 0.225F;
			zeroState[1][1] = 0.251F;
			// Z START - END
			zeroState[2][0] = 0.25F + zFightOffset;
			zeroState[2][1] = 0.75F - zFightOffset;

			switch(state) {
				case None:
				case Available:
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider
							.getIcon(PipeIconProvider.TYPE.PipeRobotStation.ordinal()));
					break;
				case Reserved:
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider
							.getIcon(PipeIconProvider.TYPE.PipeRobotStationReserved.ordinal()));
					break;
				case Linked:
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider
							.getIcon(PipeIconProvider.TYPE.PipeRobotStationLinked.ordinal()));
					break;
			}

			float[][] rotated = MatrixTranformations.deepClone(zeroState);
			MatrixTranformations.transform(rotated, side);

			renderblocks.setRenderBounds(rotated[0][0], rotated[1][0],
					rotated[2][0], rotated[0][1], rotated[1][1],
					rotated[2][1]);
			renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);
		}
	}
	public static enum RobotStationState {
		None,
		Available,
		Reserved,
		Linked
	}

	private RobotStationState renderState;
	private DockingStation station;
	private boolean isValid = false;

	public RobotStationPluggable() {

	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

	}

	@Override
	public ItemStack[] getDropItems(IPipeTile pipe) {
		return new ItemStack[] { new ItemStack(BuildCraftTransport.robotStationItem) };
	}

	public DockingStation getStation() {
		return station;
	}

	@Override
	public boolean isBlocking(IPipeTile pipe, ForgeDirection direction) {
		return true;
	}

	@Override
	public void invalidate() {
		if (station != null
				&& station.getPipe() != null
				&& !station.getPipe().getWorld().isRemote) {
			RobotRegistry.getRegistry(station.world).removeStation(station);
			isValid = false;
		}
	}

	@Override
	public void validate(IPipeTile pipe, ForgeDirection direction) {
		TileGenericPipe gPipe = (TileGenericPipe) pipe;
		if (!isValid && !gPipe.getWorld().isRemote) {
			station = (DockingStation)
					RobotRegistry.getRegistry(gPipe.getWorld()).getStation(
					gPipe.xCoord,
					gPipe.yCoord,
					gPipe.zCoord,
					direction);

			if (station == null) {
				station = new DockingStation(gPipe, direction);
				RobotRegistry.getRegistry(gPipe.getWorld()).registerStation(station);
			}

			isValid = true;
		}
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

	public RobotStationState getRenderState() {
		return renderState;
	}

	@Override
	public IPipePluggableRenderer getRenderer() {
		return new RobotStationPluggableRenderer();
	}

	@Override
	public void writeData(ByteBuf data) {
		this.renderState = station.isTaken()
				? (station.isMainStation() ? RobotStationState.Linked : RobotStationState.Reserved)
				: RobotStationState.Available;
		data.writeByte(getRenderState().ordinal());
	}

	@Override
	public void readData(ByteBuf data) {
		this.renderState = RobotStationState.values()[data.readUnsignedByte()];
	}

	@Override
	public PipePluggable createPipePluggable(IPipe pipe, ForgeDirection side, ItemStack stack) {
		return new RobotStationPluggable();
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		if (station != null && station.isTaken() && station.robotTaking() != null && station.robotTaking().getBattery() != null
				&& station.robotTaking().getDockingStation() == station) {
			return station.robotTaking().getBattery().receiveEnergy(maxReceive, simulate);
		}
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return 0;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}
}

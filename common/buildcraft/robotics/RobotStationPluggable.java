package buildcraft.robotics;

import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyReceiver;
import buildcraft.BuildCraftRobotics;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IDockingStationProvider;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableItem;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.transport.PipeIconProvider;

public class RobotStationPluggable extends PipePluggable implements IPipePluggableItem, IEnergyReceiver, IDebuggable,
		IDockingStationProvider {
	public class RobotStationPluggableRenderer implements IPipePluggableRenderer {
		private float zFightOffset = 1 / 4096.0F;

		@Override
		public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, ITextureStates blockStateMachine, int renderPass, int x, int y, int z) {
			if (renderPass != 0) {
				return;
			}

			RobotStationState state = ((RobotStationPluggable) pipePluggable).getRenderState();

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


			float[][] zeroState = new float[3][2];
			// X START - END
			zeroState[0][0] = 0.4325F;
			zeroState[0][1] = 0.5675F;
			// Y START - END
			zeroState[1][0] = 0F;
			zeroState[1][1] = 0.1875F + zFightOffset;
			// Z START - END
			zeroState[2][0] = 0.4325F;
			zeroState[2][1] = 0.5675F;

			float[][] rotated = MatrixTranformations.deepClone(zeroState);
			MatrixTranformations.transform(rotated, side);

			renderblocks.setRenderBounds(rotated[0][0], rotated[1][0],
					rotated[2][0], rotated[0][1], rotated[1][1],
					rotated[2][1]);
			renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);

			// X START - END
			zeroState[0][0] = 0.25F;
			zeroState[0][1] = 0.75F;
			// Y START - END
			zeroState[1][0] = 0.1875F;
			zeroState[1][1] = 0.25F + zFightOffset;
			// Z START - END
			zeroState[2][0] = 0.25F;
			zeroState[2][1] = 0.75F;

			rotated = MatrixTranformations.deepClone(zeroState);
			MatrixTranformations.transform(rotated, side);

			renderblocks.setRenderBounds(rotated[0][0], rotated[1][0],
					rotated[2][0], rotated[0][1], rotated[1][1],
					rotated[2][1]);
			renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);
		}
	}

	public enum RobotStationState {
		None,
		Available,
		Reserved,
		Linked
	}

	private RobotStationState renderState;
	private DockingStationPipe station;
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
		return new ItemStack[]{new ItemStack(BuildCraftRobotics.robotStationItem)};
	}

	@Override
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
			RobotManager.registryProvider.getRegistry(station.world).removeStation(station);
			isValid = false;
		}
	}

	@Override
	public void validate(IPipeTile pipe, ForgeDirection direction) {
		if (!isValid && !pipe.getWorld().isRemote) {
			station = (DockingStationPipe)
					RobotManager.registryProvider.getRegistry(pipe.getWorld()).getStation(
							pipe.x(),
							pipe.y(),
							pipe.z(),
							direction);

			if (station == null) {
				station = new DockingStationPipe(pipe, direction);
				RobotManager.registryProvider.getRegistry(pipe.getWorld()).registerStation(station);
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

	private void refreshRenderState() {
		this.renderState = station.isTaken()
				? (station.isMainStation() ? RobotStationState.Linked : RobotStationState.Reserved)
				: RobotStationState.Available;
	}

	public RobotStationState getRenderState() {
		if (renderState == null) {
			renderState = RobotStationState.None;
		}
		return renderState;
	}

	@Override
	public IPipePluggableRenderer getRenderer() {
		return new RobotStationPluggableRenderer();
	}

	@Override
	public void writeData(ByteBuf data) {
		refreshRenderState();
		data.writeByte(getRenderState().ordinal());
	}

	@Override
	public boolean requiresRenderUpdate(PipePluggable o) {
		return getRenderState() != ((RobotStationPluggable) o).getRenderState();
	}

	@Override
	public void readData(ByteBuf data) {
		try {
			this.renderState = RobotStationState.values()[data.readUnsignedByte()];
		} catch (ArrayIndexOutOfBoundsException e) {
			this.renderState = RobotStationState.None;
		}
	}

	@Override
	public PipePluggable createPipePluggable(IPipe pipe, ForgeDirection side, ItemStack stack) {
		return new RobotStationPluggable();
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		if (station != null && station.robotTaking() != null && station.robotTaking().getBattery() != null
				&& station.robotTaking().getDockingStation() == station) {
			return ((EntityRobot) station.robotTaking()).receiveEnergy(maxReceive, simulate);
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

	@Override
	public void getDebugInfo(List<String> info, ForgeDirection side, ItemStack debugger, EntityPlayer player) {
		if (station == null) {
			info.add("RobotStationPluggable: No station found!");
		} else {
			refreshRenderState();
			info.add("Docking Station (side " + side.name() + ", " + getRenderState().name() + ")");
			if (station.robotTaking() != null && station.robotTaking() instanceof IDebuggable) {
				((IDebuggable) station.robotTaking()).getDebugInfo(info, ForgeDirection.UNKNOWN, debugger, player);
			}
		}
	}
}

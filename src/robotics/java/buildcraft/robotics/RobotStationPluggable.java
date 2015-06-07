package buildcraft.robotics;

import io.netty.buffer.ByteBuf;

import java.util.List;

import cofh.api.energy.IEnergyReceiver;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

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
import buildcraft.transport.BuildCraftTransport;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.TileGenericPipe;

public class RobotStationPluggable extends PipePluggable implements IPipePluggableItem, IEnergyReceiver, IDebuggable, IDockingStationProvider {
    public class RobotStationPluggableRenderer implements IPipePluggableRenderer {
        private float zFightOffset = 1 / 4096.0F;

        @Override
        public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, EnumFacing side, PipePluggable pipePluggable,
                ITextureStates blockStateMachine, int renderPass, BlockPos pos) {
            if (renderPass != 0) {
                return;
            }

            RobotStationState state = ((RobotStationPluggable) pipePluggable).renderState;

            switch (state) {
                case None:
                case Available:
                    blockStateMachine.getTextureState().set(
                        BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeRobotStation.ordinal()));
                    break;
                case Reserved:
                    blockStateMachine.getTextureState().set(
                        BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeRobotStationReserved.ordinal()));
                    break;
                case Linked:
                    blockStateMachine.getTextureState().set(
                        BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeRobotStationLinked.ordinal()));
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

            renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
            renderblocks.renderStandardBlock(blockStateMachine.getBlock(), pos);

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

            renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
            renderblocks.renderStandardBlock(blockStateMachine.getBlock(), pos);
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
        return new ItemStack[] { new ItemStack(BuildCraftRobotics.robotStationItem) };
    }

    @Override
    public DockingStation getStation() {
        return station;
    }

    @Override
    public boolean isBlocking(IPipeTile pipe, EnumFacing direction) {
        return true;
    }

    @Override
    public void invalidate() {
        if (station != null && station.getPipe() != null && !station.getPipe().getWorld().isRemote) {
            RobotManager.registryProvider.getRegistry(station.world).removeStation(station);
            isValid = false;
        }
    }

    @Override
    public void validate(IPipeTile pipe, EnumFacing direction) {
        TileGenericPipe gPipe = (TileGenericPipe) pipe;
        if (!isValid && !gPipe.getWorld().isRemote) {
            station =
                (DockingStationPipe) RobotManager.registryProvider.getRegistry(gPipe.getWorld()).getStation(gPipe.xCoord, gPipe.yCoord, gPipe.zCoord,
                    direction);

            if (station == null) {
                station = new DockingStationPipe(gPipe, direction);
                RobotManager.registryProvider.getRegistry(gPipe.getWorld()).registerStation(station);
            }

            isValid = true;
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(EnumFacing side) {
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
        this.renderState =
            station.isTaken() ? (station.isMainStation() ? RobotStationState.Linked : RobotStationState.Reserved) : RobotStationState.Available;
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
        refreshRenderState();
        data.writeByte(getRenderState().ordinal());
    }

    @Override
    public boolean requiresRenderUpdate(PipePluggable o) {
        return renderState != ((RobotStationPluggable) o).renderState;
    }

    @Override
    public void readData(ByteBuf data) {
        this.renderState = RobotStationState.values()[data.readUnsignedByte()];
    }

    @Override
    public PipePluggable createPipePluggable(IPipe pipe, EnumFacing side, ItemStack stack) {
        return new RobotStationPluggable();
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        if (station != null && station.robotTaking() != null && station.robotTaking().getBattery() != null
            && station.robotTaking().getDockingStation() == station) {
            return ((EntityRobot) station.robotTaking()).receiveEnergy(maxReceive, simulate);
        }
        return 0;
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return 0;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return 0;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    @Override
    public void getDebugInfo(List<String> info, EnumFacing side, ItemStack debugger, EntityPlayer player) {
        if (station == null) {
            info.add("RobotStationPluggable: No station found!");
        } else {
            refreshRenderState();
            info.add("Docking Station (side " + side.name() + ", " + renderState.name() + ")");
            if (station.robotTaking() != null && station.robotTaking() instanceof IDebuggable) {
                ((IDebuggable) station.robotTaking()).getDebugInfo(info, EnumFacing.UNKNOWN, debugger, player);
            }
        }
    }
}

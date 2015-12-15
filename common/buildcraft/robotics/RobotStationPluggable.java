package buildcraft.robotics;

import java.util.List;
import java.util.Locale;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

import cofh.api.energy.IEnergyReceiver;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IDockingStationProvider;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableItem;
import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.robotics.render.RobotStationRenderer;

import io.netty.buffer.ByteBuf;

public class RobotStationPluggable extends PipePluggable implements IPipePluggableItem, IEnergyReceiver, IDebuggable, IDockingStationProvider {
    public enum EnumRobotStationState {
        None,
        Available,
        Reserved,
        Linked;

        public String getTextureSuffix() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private EnumRobotStationState renderState;
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
        if (!isValid && !pipe.getWorld().isRemote) {
            station = (DockingStationPipe) RobotManager.registryProvider.getRegistry(pipe.getWorld()).getStation(((TileEntity) pipe).getPos(),
                    direction);

            if (station == null) {
                station = new DockingStationPipe(pipe, direction);
                RobotManager.registryProvider.getRegistry(pipe.getWorld()).registerStation(station);
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
        return new AxisAlignedBB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
    }

    private void refreshRenderState() {
        this.renderState = station.isTaken() ? (station.isMainStation() ? EnumRobotStationState.Linked : EnumRobotStationState.Reserved)
            : EnumRobotStationState.Available;
    }

    public EnumRobotStationState getRenderState() {
        if (renderState == null) {
            renderState = EnumRobotStationState.None;
        }
        return renderState;
    }

    @Override
    public IPipePluggableStaticRenderer getRenderer() {
        return RobotStationRenderer.INSTANCE;
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
            this.renderState = EnumRobotStationState.values()[data.readUnsignedByte()];
        } catch (ArrayIndexOutOfBoundsException e) {
            this.renderState = EnumRobotStationState.None;
        }
    }

    @Override
    public PipePluggable createPipePluggable(IPipe pipe, EnumFacing side, ItemStack stack) {
        return new RobotStationPluggable();
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        if (station != null && station.robotTaking() != null && station.robotTaking().getBattery() != null && station.robotTaking()
                .getDockingStation() == station) {
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
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        if (station == null) {
            left.add("RobotStationPluggable: No station found!");
        } else {
            refreshRenderState();
            left.add("Docking Station (side " + side.name() + ", " + renderState.name() + ")");
            if (station.robotTaking() != null && station.robotTaking() instanceof IDebuggable) {
                ((IDebuggable) station.robotTaking()).getDebugInfo(left, right, side);
            }
        }
    }
}

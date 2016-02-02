package buildcraft.transport.pluggable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

import cofh.api.energy.IEnergyHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.utils.MatrixTranformations;

import io.netty.buffer.ByteBuf;

public class PowerAdapterPluggable extends PipePluggable implements IEnergyHandler {
    private static final int MAX_POWER = 40;
    private IPipeTile container;

    public PowerAdapterPluggable() {

    }

    @Override
    public void validate(IPipeTile pipe, EnumFacing direction) {
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
        return new ItemStack[] { new ItemStack(BuildCraftTransport.powerAdapterItem) };
    }

    @Override
    public boolean isBlocking(IPipeTile pipe, EnumFacing direction) {
        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(EnumFacing side) {
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
        return new AxisAlignedBB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
    }

    @Override
    public IPipePluggableStaticRenderer getRenderer() {
        return PowerAdapterModel.INSTANCE;
    }

    @Override
    public void writeData(ByteBuf data) {

    }

    @Override
    public void readData(ByteBuf data) {

    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
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
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        if (container.getPipe() instanceof IEnergyHandler) {
            return ((IEnergyHandler) container.getPipe()).getEnergyStored(from);
        } else {
            return 0;
        }
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        if (container.getPipe() instanceof IEnergyHandler) {
            return ((IEnergyHandler) container.getPipe()).getMaxEnergyStored(from);
        } else {
            return 0;
        }
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    @Override
    public boolean requiresRenderUpdate(PipePluggable o) {
        return false;
    }
}

package buildcraft.transport.transformer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import buildcraft.core.lib.RFBattery;

public class TileTransformer extends TileEntity implements ITickable, IEnergyHandler, IEnergyProvider, IEnergyReceiver {
    private final int[] POWER_LEVELS = {320, 1280, 5120, 81920};
    private EnumFacing facing = EnumFacing.EAST;
    private boolean stepUp;
    private int power;

    public TileTransformer() {

    }

    public TileTransformer(World world) {
        this.worldObj = world;
    }

    public BlockTransformer.EnumVoltage getVoltage() {
        return BlockTransformer.EnumVoltage.VALUES[getBlockMetadata()];
    }

    public EnumFacing getFacing() {
        return facing;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("facing", (byte) facing.ordinal());
        return new S35PacketUpdateTileEntity(getPos(), getBlockMetadata(), tag);
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.S35PacketUpdateTileEntity pkt) {
        if (pkt.getNbtCompound().hasKey("facing")) {
            facing = EnumFacing.getFront(pkt.getNbtCompound().getByte("facing"));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        facing = tag.hasKey("facing") ? EnumFacing.getFront(tag.getByte("facing")) : EnumFacing.EAST;
        power = tag.getInteger("power");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setByte("facing", (byte) facing.ordinal());
        tag.setInteger("power", power);
    }

    @Override
    public void update() {
        if (worldObj.isRemote) {
            return;
        }

        stepUp = worldObj.isBlockPowered(getPos());

        if (power >= getExtractionPower()) {
            IEnergyReceiver[] receivers = new IEnergyReceiver[6];
            int[] pulls = new int[6];
            int totalPull = 0;
            int countPull = 0;

            for (EnumFacing side : EnumFacing.VALUES) {
                if (isExtractionSide(side)) {
                    TileEntity tile = worldObj.getTileEntity(getPos().offset(side));
                    if (tile instanceof IEnergyReceiver) {
                        receivers[side.ordinal()] = (IEnergyReceiver) tile;
                        int p = ((IEnergyReceiver) tile).receiveEnergy(side.getOpposite(), getExtractionPower(), true);
                        pulls[side.ordinal()] = p;
                        totalPull += p;
                        if (p > 0) {
                            countPull++;
                        }
                    }
                }
            }

            for (EnumFacing side : EnumFacing.VALUES) {
                if (pulls[side.ordinal()] > 0) {
                    int p = getExtractionPower() * countPull * pulls[side.ordinal()] / totalPull;
                    p = Math.min(p, Math.min(getExtractionPower(), power));
                    power -= receivers[side.ordinal()].receiveEnergy(side.getOpposite(), p, false);
                }
            }
        }
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return power;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return POWER_LEVELS[getVoltage().ordinal() + 1] * 4;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    private boolean isExtractionSide(EnumFacing facing) {
        return !stepUp ^ getFacing() == facing;
    }

    private int getExtractionPower() {
        return stepUp ? POWER_LEVELS[getVoltage().ordinal() + 1] : POWER_LEVELS[getVoltage().ordinal()];
    }

    private int getInsertionPower() {
        return stepUp ? POWER_LEVELS[getVoltage().ordinal()] : POWER_LEVELS[getVoltage().ordinal() + 1];
    }

    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        if (isExtractionSide(from)) {
            if (getEnergyStored(from) < getExtractionPower()) {
                return 0;
            }
            int delta = Math.min(getEnergyStored(from), maxExtract);
            if (!simulate) {
                power -= delta;
            }
            return delta;
        }

        return 0;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        if (!isExtractionSide(from)) {
            int delta = Math.min(getMaxEnergyStored(from) - getEnergyStored(from), maxReceive);
            if (!simulate) {
                power += delta;
            }
            return delta;
        }

        return 0;
    }

    public void setFacing(EnumFacing facing) {
        this.facing = facing;
    }

    public void rotateFacing(EnumFacing axis) {
        if (facing == axis) {
            facing = axis.getOpposite();
        } else {
            facing = axis;
        }

        markDirty();
        worldObj.markBlockForUpdate(pos);
    }
}

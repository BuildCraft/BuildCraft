package buildcraft.factory.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.fluids.SingleUseTank;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.tile.TileBC_Neptune;

import javax.annotation.Nullable;

public class TileTank extends TileBC_Neptune implements ITickable, IDebuggable {
    public Tank tank = new SingleUseTank("tank", 16000, this);

    // ITickable

    @Override
    public void update() {
        if (worldObj.isRemote) {
            return;
        }

        TileEntity tileDown = worldObj.getTileEntity(pos.down());
        if (tileDown != null && tileDown instanceof TileTank) {
            TileTank tile = (TileTank) tileDown;
            int used = tile.tank.fill(tank.getFluid(), true);

            if (used > 0) {
                tank.drain(used, true);
                sendNetworkUpdate(NET_RENDER_DATA);
                tile.sendNetworkUpdate(NET_RENDER_DATA);
            }
        }

        sendNetworkUpdate(NET_RENDER_DATA); // TODO: optimize
    }

    // NBT

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tank.deserializeNBT(nbt.getCompoundTag("tank"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("tank", tank.serializeNBT());
        return nbt;
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER && id == NET_RENDER_DATA) {
            tank.writeToBuffer(buffer);
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
        if (side == Side.CLIENT && id == NET_RENDER_DATA) {
            tank.readFromBuffer(buffer);
        }
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("fluid = " + tank.getDebugString());
    }

    // Capabilities

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            //noinspection unchecked
            return (T) new IFluidHandler() {
                @Override
                public IFluidTankProperties[] getTankProperties() {
                    return tank.getTankProperties();
                }

                private Tank getTank(BlockPos currentPos) {
                    TileTank tile = (worldObj.getTileEntity(currentPos) instanceof TileTank) ? (TileTank) worldObj.getTileEntity(currentPos) : null;
                    if(tile != null && (tile.tank.getFluidType() == tank.getFluidType() || tile.tank.getFluidType() == null || tank.getFluidType() == null)) {
                        return tile.tank;
                    }
                    return null;
                }

                private List<Tank> getTanks() {
                    List<Tank> tanks = new ArrayList<>();
                    BlockPos currentPos = pos;
                    while(true) {
                        Tank tank = getTank(currentPos);
                        if(tank != null) {
                            tanks.add(tank);
                        } else {
                            break;
                        }
                        currentPos = currentPos.up();
                    }
                    currentPos = pos.down();
                    while(true) {
                        Tank tank = getTank(currentPos);
                        if(tank != null) {
                            tanks.add(tank);
                        } else {
                            break;
                        }
                        currentPos = currentPos.down();
                    }
                    return tanks;
                }

                @Override
                public int fill(FluidStack resource, boolean doFill) {
                    int result = 0;
                    FluidStack copy = resource.copy();
                    for(Tank tank : getTanks()) {
                        int filled = tank.fill(copy, doFill);
                        result += filled;
                        copy.amount -= filled;
                        if(copy.amount <= 0) {
                            break;
                        }
                    }
                    return result;
                }

                @Nullable
                @Override
                public FluidStack drain(FluidStack resource, boolean doDrain) {
                    FluidStack result = null;
                    for(Tank tank : getTanks()) {
                        FluidStack drained = tank.drain(resource, doDrain);
                        if(result == null) {
                            result = drained;
                        } else if(drained != null) {
                            result.amount += drained.amount;
                        }
                    }
                    return result;
                }

                @Nullable
                @Override
                public FluidStack drain(int maxDrain, boolean doDrain) {
                    FluidStack result = null;
                    for(Tank tank : getTanks()) {
                        FluidStack drained = tank.drain(maxDrain, doDrain);
                        if(result == null) {
                            result = drained;
                        } else if(drained != null) {
                            result.amount += drained.amount;
                        }
                    }
                    return result;
                }
            };
        }
        return super.getCapability(capability, facing);
    }
}

package buildcraft.factory.tile;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.fluids.IHasTank;
import buildcraft.lib.fluids.SingleUseTank;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class TileTank extends TileBC_Neptune implements ITickable, IDebuggable, IHasTank {
    public static final int NET_TANK = 10;

    public SingleUseTank tank = new SingleUseTank("tank", 16000, this);

    // ITickable

    @Override
    public void update() {
        if(worldObj.isRemote) {
            return;
        }

        TileEntity tileDown = worldObj.getTileEntity(pos.down());
        if(tileDown != null && tileDown instanceof TileTank) {
            TileTank tile = (TileTank) tileDown;
            int used = tile.tank.fill(tank.getFluid(), true);

            if (used > 0) {
                tank.drain(used, true);
                sendNetworkUpdate(NET_TANK);
                tile.sendNetworkUpdate(NET_TANK);
            }
        }

        sendNetworkUpdate(NET_TANK); // TODO: optimize
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

    // Netwokring

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if(side == Side.SERVER && id == NET_TANK) {
            tank.writeToBuffer(buffer);
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
        if(side == Side.CLIENT && id == NET_TANK) {
            tank.readFromBuffer(buffer);
        }
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("fluid = " + tank.getDebugString());
    }

    // IHasTank

    @Override
    public Tank getTank() {
        return tank;
    }
}

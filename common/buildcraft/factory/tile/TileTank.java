package buildcraft.factory.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.fluids.SingleUseTank;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.tile.TileBC_Neptune;

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
            return (T) tank;
        }
        return super.getCapability(capability, facing);
    }
}

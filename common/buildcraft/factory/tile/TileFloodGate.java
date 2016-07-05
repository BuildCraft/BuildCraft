package buildcraft.factory.tile;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.fluids.TankUtils;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TileFloodGate extends TileBC_Neptune implements ITickable, IDebuggable {
    public static final EnumFacing[] SIDE_INDEXES = new EnumFacing[]{EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
    public static final int[] REBUILD_DELAYS = new int[]{128, 256, 512, 1024, 2048, 4096, 8192, 16384};
    public static final int NET_FLOOD_GATE = 10;

    private boolean[] sidesBlocked = new boolean[5];
    private final Tank tank = new Tank("tank", 2000, this);

    public static int getIndexFromSide(EnumFacing side) {
        return Arrays.binarySearch(SIDE_INDEXES, side);
    }

    public boolean isSideBlocked(EnumFacing side) {
        return sidesBlocked[getIndexFromSide(side)];
    }

    public void setSideBlocked(EnumFacing side, boolean blocked) {
        sidesBlocked[getIndexFromSide(side)] = blocked;
    }

    // ITickable

    @Override
    public void update() {
        if(worldObj.isRemote) {
            return;
        }

        TankUtils.popFluidAround(worldObj, pos);

        sendNetworkUpdate(NET_FLOOD_GATE); // TODO: optimize
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("fluid = " + tank.getDebugString());
        String[] sides = new String[5];
        for(int i = 0; i < sidesBlocked.length; i++) {
            sides[i] = SIDE_INDEXES[i].toString().toLowerCase() + "(" + sidesBlocked[i] + ")";
        }
        left.add("sides = " + String.join(" ", (CharSequence[]) sides));
    }

    // NBT

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tank.readFromNBT(nbt);
        for(int i = 0; i < sidesBlocked.length; i++) {
            nbt.setBoolean("sides_blocked_" + i, sidesBlocked[i]);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        tank.writeToNBT(nbt);
        for(int i = 0; i < sidesBlocked.length; i++) {
            sidesBlocked[i] = nbt.getBoolean("sides_blocked_" + i);
        }
        return nbt;
    }

    // Netwokring

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if(side == Side.SERVER && id == NET_FLOOD_GATE) {
            tank.writeToBuffer(buffer);
            MessageUtil.writeBooleanArray(buffer, sidesBlocked);
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
        if(side == Side.CLIENT && id == NET_FLOOD_GATE) {
            tank.readFromBuffer(buffer);
            sidesBlocked = MessageUtil.readBooleanArray(buffer, sidesBlocked.length);
        }
    }

    // Capabilities

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) tank;
        }
        return super.getCapability(capability, facing);
    }
}

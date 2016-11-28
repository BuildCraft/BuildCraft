package buildcraft.silicon.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.mj.ILaserTarget;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBCInventory_Neptune;

abstract class TileLaserTableBase extends TileBCInventory_Neptune implements ILaserTarget, IHasWork, ITickable, IDebuggable {
    public long power;

    @Override
    public boolean requiresLaserPower() {
        return hasWork();
    }

    @Override
    public void receiveLaserPower(long microJoules) {
        power += microJoules;
    }

    @Override
    public boolean isInvalidTarget() {
        return !hasWork();
    }

    @Override
    public void update() {
        if (worldObj.isRemote) {
            return;
        }

        if (!hasWork()) {
            power = 0;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setLong("power", power);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        power = nbt.getLong("power");
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (id == NET_GUI_DATA) {
            buffer.writeLong(power);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (id == NET_GUI_DATA) {
            power = buffer.readLong();
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("power - " + power);
    }
}

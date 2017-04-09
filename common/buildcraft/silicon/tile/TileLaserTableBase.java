package buildcraft.silicon.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.mj.ILaserTarget;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.data.AverageLong;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

public abstract class TileLaserTableBase extends TileBC_Neptune implements ILaserTarget, IHasWork, ITickable, IDebuggable {
    private static final long MJ_FLOW_ROUND = MjAPI.MJ / 10;
    private final AverageLong avgPower = new AverageLong(120);
    public long avgPowerClient;
    public long power;

    @Override
    public boolean requiresLaserPower() {
        return hasWork();
    }

    @Override
    public void receiveLaserPower(long microJoules) {
        power += microJoules;
        avgPower.push(microJoules);
    }

    @Override
    public boolean isInvalidTarget() {
        return !hasWork();
    }

    @Override
    public void update() {
        avgPower.tick();
        if (world.isRemote) {
            return;
        }

        if (!hasWork()) {
            power = 0;
            avgPower.clear();
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
            double avg = avgPower.getAverage();
            long pwrAvg = Math.round(avg);
            long div = pwrAvg / MJ_FLOW_ROUND;
            long mod = pwrAvg % MJ_FLOW_ROUND;
            int mj = (int) (div) + ((mod > MJ_FLOW_ROUND / 2) ? 1 : 0);
            buffer.writeInt(mj);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (id == NET_GUI_DATA) {
            power = buffer.readLong();
            avgPowerClient = buffer.readInt() * MJ_FLOW_ROUND;
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("power - " + LocaleUtil.localizeMj(power));
    }
}

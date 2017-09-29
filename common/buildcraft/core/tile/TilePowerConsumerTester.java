package buildcraft.core.tile;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.tile.TileBC_Neptune;

public class TilePowerConsumerTester extends TileBC_Neptune implements IMjReceiver, IDebuggable {

    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(this);
    private long lastReceived;
    private long totalReceived;

    public TilePowerConsumerTester() {
        caps.addProvider(mjCaps);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        lastReceived = nbt.getLong("last");
        totalReceived = nbt.getLong("total");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setLong("last", lastReceived);
        nbt.setLong("total", totalReceived);
        return nbt;
    }

    // IMjReceiver

    @Override
    public boolean canConnect(IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return 100000 * MjAPI.MJ;
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        if (!simulate) {
            lastReceived = microJoules;
            totalReceived += microJoules;
        }
        return 0;
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("Last received = " + LocaleUtil.localizeMj(lastReceived));
        left.add("Total received = " + LocaleUtil.localizeMj(totalReceived));
    }
}

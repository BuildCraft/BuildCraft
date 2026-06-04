package buildcraft.core.tile;

import java.util.List;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraft.util.ITickable;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.tile.TileBC_Neptune;

public class TilePowerConsumerTester extends TileBC_Neptune implements IMjReceiver, ITickable, IDebuggable {

    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(this);
    private long lastReceived;
    private long nextTickReceived;
    private long lastTickReceived;
    private long totalReceived;

    public TilePowerConsumerTester() {
        caps.addProvider(mjCaps);
    }

    @Override
    public void readFromNBT(NbtCompound nbt) {
        super.readFromNBT(nbt);
        lastReceived = nbt.getLong("last");
        nextTickReceived = nbt.getLong("nt");
        lastTickReceived = nbt.getLong("lt");
        totalReceived = nbt.getLong("total");
    }

    @Override
    public NbtCompound writeToNBT(NbtCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.putLong("last", lastReceived);
        nbt.putLong("nt", nextTickReceived);
        nbt.putLong("lt", lastTickReceived);
        nbt.putLong("total", totalReceived);
        return nbt;
    }

    // ITickable

    @Override
    public void update() {
        lastTickReceived = nextTickReceived;
        nextTickReceived = 0;
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
            nextTickReceived += microJoules;
            totalReceived += microJoules;
        }
        return 0;
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("");
        left.add("Last received = " + LocaleUtil.localizeMj(lastReceived));
        left.add("Tick received = " + LocaleUtil.localizeMj(lastTickReceived));
        left.add("Total received = " + LocaleUtil.localizeMj(totalReceived));
    }
}

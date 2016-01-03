package buildcraft.core.power_bc8;

import java.util.function.Predicate;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.power.bc8.EnumPowerBar;
import buildcraft.api.power.bc8.IPowerConnection;
import buildcraft.api.power.bc8.IPowerConnection.IPowerConsumer;
import buildcraft.api.power.bc8.IPowerTunnel;
import buildcraft.api.power.bc8.PowerAPI_BC8.IPowerNetwork;

public class PowerNetwork implements IPowerNetwork {
    @Override
    public void addConnection(IPowerConnection connection) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeConnection(IPowerConnection connection) {
        // TODO Auto-generated method stub

    }

    @Override
    public IPowerTunnel requestTunnel(IPowerConsumer consumer, EnumPowerBar type, int units, Predicate<IPowerConnection> connectionFilter) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPowerTunnel loadTunnel(IPowerConsumer consumer, NBTTagCompound compound) {
        return null;
    }
}

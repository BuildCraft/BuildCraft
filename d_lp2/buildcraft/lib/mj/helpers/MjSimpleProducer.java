package buildcraft.lib.mj.helpers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.mj.*;

public abstract class MjSimpleProducer extends MjSimpleMachine implements IMjMachineProducer {
    private final EnumMjPowerType powerType;
    private final List<IMjConnection> powerRemovingConnections = new ArrayList<>();
    private int milliWattsRemovedCache = 0, milliWattsRemovedLast = 0;

    public MjSimpleProducer(TileEntity tile, IConnectionLogic logic, EnumFacing[] faces, EnumMjPowerType powerType) {
        super(tile, logic, faces);
        this.powerType = powerType;
    }

    private void recalcMilliWattsRemoved() {
        int val = 0;
        for (IMjConnection connection : powerRemovingConnections) {
            val += connection.milliWattsIn();
        }
        milliWattsRemovedCache = val;
    }

    @Override
    public boolean onConnectionCreate(IMjConnection connection) {
        if (connection.getProducer() == this) {// Just check to be on the safe side.
            powerRemovingConnections.add(connection);
            recalcMilliWattsRemoved();
            return true;
        }
        return false;
    }

    @Override
    public void onConnectionActivate(IMjConnection connection) {}

    @Override
    public void onConnectionBroken(IMjConnection connection) {
        powerRemovingConnections.remove(connection);
        recalcMilliWattsRemoved();
    }

    @Override
    public void tick() {
        super.tick();
        if (!canUpdate()) return;
        if (milliWattsRemovedLast != milliWattsRemovedCache) {
            milliWattsRemovedLast = milliWattsRemovedCache;
            int maxSuppliable = getMaxCurrentlySuppliable();
            if (maxSuppliable < milliWattsRemovedCache) {
                // Uh-oh, we are using up too much power. Perhaps the producer just ran out of fuel.
                int supplied = 0;
                List<IMjConnection> connections = new ArrayList<>(powerRemovingConnections);
                for (IMjConnection connection : connections) {
                    supplied += connection.milliWattsIn();
                    if (supplied > maxSuppliable) {
                        connection.breakConnection();
                    }
                }
            } else {
                setCurrentUsed(milliWattsRemovedCache);
            }
        }
    }

    @Override
    public EnumMjPowerType getPowerType() {
        return powerType;
    }

    @Override
    public boolean canProduceFor(IMjRequest request, List<IMjMachine> machinesSoFar) {
        return true;
    }

    @Override
    public int getSuppliable(IMjRequest request) {
        int available = getMaxCurrentlySuppliable() - milliWattsRemovedCache;
        return Math.min(available, request.getMilliWatts());
    }

    public abstract void setCurrentUsed(int milliwatts);

    public abstract int getMaxCurrentlySuppliable();
}

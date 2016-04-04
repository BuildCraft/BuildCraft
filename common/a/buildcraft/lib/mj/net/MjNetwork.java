package a.buildcraft.lib.mj.net;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.world.World;

import a.buildcraft.api.mj.*;

public enum MjNetwork implements IMjNetwork {
    INSTANCE;

    /** Keep a small cache of machines to make lookups quicker. (otherwise we have to go down to searching the
     * underlying world instance, and thats not as fast) */
    private Map<MjMachineIdentifier, IMjMachine> machineCache;
    /** Use a queue to keep the machine cache clean between ticks. */
    private Deque<IMjMachine> removedMachines;
    /** Keep a queue of pending requests. */
    private Deque<IMjRequest> pendingRequests;
    private Deque<IMjConnection> brokenConnections;
    /** Remember ALL active connections, so that we can save+load them from disk properly. */
    private List<IMjConnection> activeConnections;

    private MjNetwork() {
        machineCache = new ConcurrentHashMap<>();
        removedMachines = new ConcurrentLinkedDeque<>();
        pendingRequests = new ConcurrentLinkedDeque<>();
        activeConnections = new CopyOnWriteArrayList<>();
        brokenConnections = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void removeMachine(IMjMachine machine) {
        refreshMachine(machine);
        removedMachines.add(machine);
    }

    @Override
    public void refreshMachine(IMjMachine machine) {
        for (MjMachineIdentifier ident : machine.getIdentifiers()) {
            machineCache.remove(ident);
        }
    }

    @Override
    public boolean connectionExists(IMjConnection connection) {
        return activeConnections.contains(connection);
    }

    @Override
    public boolean requestExists(IMjRequest request) {
        return pendingRequests.contains(request);
    }

    @Override
    public IMjRequest makeRequest(int milliWatts, IMjMachineConsumer requester) {
        MjRequest req = new MjRequest(milliWatts, requester);
        pendingRequests.add(req);
        return req;
    }

    public void breakConnection(IMjConnection connection) {
        brokenConnections.add(connection);
    }

    public void tick(World world) {
        if (world.isRemote) return;
        for (IMjRequest pending : pendingRequests) {
//            IMj
        }

        activeConnections.removeAll(brokenConnections);
        for (IMjConnection broken : brokenConnections) {
            broken.getProducer().onConnectionBroken(broken);
            broken.getConsumer().onConnectionBroken(broken);
            for (IMjMachine machine : broken.getConductors()) {
                machine.onConnectionBroken(broken);
            }
        }
        brokenConnections.clear();
    }
}

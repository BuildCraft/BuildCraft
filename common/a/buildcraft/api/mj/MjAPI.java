package a.buildcraft.api.mj;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import buildcraft.api.APIHelper;

public class MjAPI {
    @CapabilityInject(IMjMachine.class)
    public static final Capability<IMjMachine> CAP_MACHINE = null;
    public static final IMjNetwork NET_INSTANCE = APIHelper.getInstance("buildcraft.core.mj.net.MjNetwork", IMjNetwork.class, Void.INSTANCE);

    // Void instance of the network if BC isn't loaded.
    private enum Void implements IMjNetwork {
        INSTANCE;

        @Override
        public void addOddMachine(IMjMachine machine) {}

        @Override
        public void removeMachine(IMjMachine machine) {}

        @Override
        public void refreshMachine(IMjMachine machine) {}

        @Override
        public boolean connectionExists(IMjConnection connection) {
            return false;
        }

        @Override
        public boolean requestExists(IMjRequest request) {
            return false;
        }

        @Override
        public IMjRequest makeRequest(int milliWatts, IMjMachineConsumer requester) {
            return null;
        }
    }
}

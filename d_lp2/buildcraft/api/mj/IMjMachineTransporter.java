package buildcraft.api.mj;

import java.util.List;

public interface IMjMachineTransporter extends IMjMachine {
    EnumMjPowerType getPowerType();

    boolean canTransportFrom(IMjRequest request, List<IMjMachine> machinesSoFar);

    /** Gets how long it will take to setup a connection across this machine. (in ticks) */
    int getSetupTime(IMjRequest request, List<IMjMachine> machinesSoFar);

    /** Gets the traversal loss policy for this transporter */
    TraversalLossPolicy getTraversalLossPolicy();
}

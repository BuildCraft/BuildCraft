package buildcraft.core.mj.api;

import java.util.List;

public interface IMjMachineTransporter extends IMjMachine {
    EnumMjPowerType getPowerType();

    boolean canTransportFrom(IMjRequest request, List<IMjMachine> machinesSoFar);

    /** Gets how long it will take to setup a connection across this machine.
     * 
     * @param request The requestor
     * @param machinesSoFar The machines from the requestor searching towards a potential */
    int getSetupTime(IMjRequest request, List<IMjMachine> machinesSoFar);

    /** Gets how much power will be added to the request in order to traverse this connection. Most pipes will return
     * 0. */
    int getMaintenacePowerCost(IMjRequest request, List<IMjMachine> machinesSoFar);
}

package buildcraft.api.mj;

import java.util.List;

public interface IMjMachineTransporter extends IMjMachine {
    EnumMjPowerType getPowerType();

    boolean canTransportFrom(IMjRequest request, List<IMjMachine> machinesSoFar);

    /** Gets how long it will take to setup a connection across this machine.
     * 
     * @param request The requestor
     * @param machinesSoFar The machines from the requestor searching towards a potential */
    int getSetupTime(IMjRequest request, List<IMjMachine> machinesSoFar);

    /** Gets how much power will be added to the request in order to traverse this connection. Most pipes will return 0.
     * 
     * @param request The request. Note that this won't be the same object that was passed to the
     *            {@link IMjMachineConsumer} when it requested power. */
    int getMaintenacePowerCost(IMjRequest request, List<IMjMachine> machinesSoFar);

    /** Gets how much power will be subtracted from the given number of milliwatts. This is used when calculated back
     * from the producer, if the producer could not completely fulfil the request. This *must* exactly mirror
     * {@link #getMaintenacePowerCost(IMjRequest, List)} but backwards, so if you received 100 and returned 10 (for the
     * other method) then if you were given 110 you *must* return 10 from this one. (if you don't then the wrong amounts
     * will be sent through) */
    int getMaintenancePowerCostBack(IMjRequest request, List<IMjMachine> transporters, IMjMachineProducer producer);
}

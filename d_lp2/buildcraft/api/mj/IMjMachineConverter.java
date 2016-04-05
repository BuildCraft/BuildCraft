package buildcraft.api.mj;

import java.util.List;

public interface IMjMachineConverter extends IMjMachine {
    EnumMjPowerType getPowerTypeIn();

    EnumMjPowerType getPowerTypeOut();

    /** Checks to see if this converter can transfer power from the input machine to the output machine. */
    boolean canConvertAccross(MjMachineIdentifier from, MjMachineIdentifier to);

    /** Gets how long it will take to setup a connection across this machine. */
    int getSetupTime(IMjRequest request, List<IMjMachine> machinesSoFar);

    /** Gets how much power will be added to the request in order to convert from the input to the output. */
    int getMaintenacePowerCost(IMjRequest request, List<IMjMachine> machinesSoFar);

    /** Gets how much power will be subtracted from the given number of milliwatts. This is used when calculated back
     * from the producer, if the producer could not completely fulfil the request. This *must* exactly mirror
     * {@link #getMaintenacePowerCost(IMjRequest, List)} but backwards, so if you received 100 and returned 10 (for the
     * other method) then if you were given 110 you *must* return 10 from this one. (if you don't then the wrong amounts
     * will be sent through) */
    int getMaintenancePowerCostBack(IMjRequest request, List<IMjMachine> transporters, IMjMachineProducer producer);
}

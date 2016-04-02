package alexiil.mc.mod.buildcraft.mj.api;

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
}

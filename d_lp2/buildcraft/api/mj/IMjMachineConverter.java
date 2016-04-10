package buildcraft.api.mj;

import java.util.List;

public interface IMjMachineConverter extends IMjMachine {
    EnumMjPowerType getPowerTypeIn();

    EnumMjPowerType getPowerTypeOut();

    /** Checks to see if this converter can transfer power from the input machine to the output machine. */
    boolean canConvertAccross(MjMachineIdentifier from, MjMachineIdentifier to);

    /** Gets how long it will take to setup a connection across this machine. (in ticks) */
    int getSetupTime(IMjRequest request, List<IMjMachine> machinesSoFar);

    /** Gets the traversal loss policy for this transporter */
    TraversalLossPolicy getTraversalLossPolicy();
}

package buildcraft.core.mj.api;

import java.util.List;

public interface IMjMachineProducer extends IMjMachine {
    /** Checks to see if this producer can supply some of the power required for this request.
     * 
     * @param request
     * @return The amount of milliwatts that this producer can supply to the request. Returning a value less than or
     *         equal to zero means this this will be ignored. */
    int getSuppliable(IMjRequest request);

    EnumMjPowerType getPowerType();

    boolean canProduceFor(IMjRequest request, List<IMjMachine> machinesSoFar);
}

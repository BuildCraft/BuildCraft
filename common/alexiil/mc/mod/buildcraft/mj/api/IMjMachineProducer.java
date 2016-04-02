package alexiil.mc.mod.buildcraft.mj.api;

import java.util.List;

public interface IMjMachineProducer extends IMjMachine {
    int canSupplyAll(IMjRequest request);

    EnumMjPowerType getPowerType();

    boolean canProduceFor(IMjRequest request, List<IMjMachine> machinesSoFar);
}

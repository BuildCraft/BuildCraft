package alexiil.mc.mod.buildcraft.mj.api;

import javax.annotation.Nonnull;

public interface IMjRequest {
    int getMilliWatts();

    IMjMachineConsumer getRequester();
}

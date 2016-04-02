package buildcraft.core.mj.api;

public interface IMjRequest {
    int getMilliWatts();

    IMjMachineConsumer getRequester();
}

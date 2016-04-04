package a.buildcraft.api.mj;

public interface IMjRequest {
    int getMilliWatts();

    IMjMachineConsumer getRequester();
}

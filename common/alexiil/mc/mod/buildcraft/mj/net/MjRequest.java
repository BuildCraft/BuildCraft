package alexiil.mc.mod.buildcraft.mj.net;

import alexiil.mc.mod.buildcraft.mj.api.IMjMachineConsumer;
import alexiil.mc.mod.buildcraft.mj.api.IMjRequest;

public class MjRequest implements IMjRequest {
    private final int milliWatts;
    private final IMjMachineConsumer requester;

    public MjRequest(int milliWatts, IMjMachineConsumer requester) {
        this.milliWatts = milliWatts;
        if (requester == null) throw new NullPointerException("requester");
        this.requester = requester;
    }

    @Override
    public int getMilliWatts() {
        return milliWatts;
    }

    @Override
    public IMjMachineConsumer getRequester() {
        return requester;
    }
}

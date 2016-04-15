package buildcraft.api.mj;

public class MjPowerRequest {
    public static final MjPowerRequest NO_REQUEST = new MjPowerRequest(MjAPI.VoidRequestor.INSTANCE, 0);

    private final IMjReciever requestor;
    private final int milliJoulesRequested;

    public MjPowerRequest(IMjReciever requestor, int milliJoulesRequested) {
        this.requestor = requestor;
        this.milliJoulesRequested = milliJoulesRequested;
    }

    public IMjReciever getRequestor() {
        return requestor;
    }

    public int getMilliJoulesRequested() {
        return milliJoulesRequested;
    }
}

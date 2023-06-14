package buildcraft.core.lib.network;

public enum PacketSide {
    CLIENT_ONLY(true, false),
    SERVER_ONLY(false, true),
    BOTH_SIDES(true, true);

    private final boolean validOnClient;
    private final boolean validOnServer;

    PacketSide(boolean validOnClient, boolean validOnServer) {
        this.validOnClient = validOnClient;
        this.validOnServer = validOnServer;
    }

    public boolean isValidOnClient() {
        return validOnClient;
    }

    public boolean isValidOnServer() {
        return validOnServer;
    }

    public boolean contains(PacketSide other) {
        return this == BOTH_SIDES || this == other;
    }
}

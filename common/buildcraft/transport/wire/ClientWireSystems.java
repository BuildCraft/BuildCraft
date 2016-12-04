package buildcraft.transport.wire;

import java.util.HashMap;
import java.util.Map;

public enum ClientWireSystems {
    INSTANCE;

    public final Map<Integer, WireSystem> wireSystems = new HashMap<>();
}

package buildcraft.lib.engine;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;

public class EngineConnector implements IMjConnector {
    public final boolean redstoneOnly;

    public EngineConnector(boolean redstoneOnly) {
        this.redstoneOnly = redstoneOnly;
    }

    @Override
    public boolean canConnect(IMjConnector other) {
        if (redstoneOnly) {
            return other instanceof IMjRedstoneReceiver;
        }
        return true;
    }
}

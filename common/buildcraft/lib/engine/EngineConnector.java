package buildcraft.lib.engine;

import javax.annotation.Nonnull;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjConnectorType;

public class EngineConnector implements IMjConnector {
    @Nonnull
    private final IMjConnectorType type;

    public EngineConnector(@Nonnull IMjConnectorType type) {
        this.type = type;
    }

    @Override
    public IMjConnectorType getType() {
        return type;
    }

    @Override
    public boolean canConnect(IMjConnector other) {
        return type.getSimpleType().canSendTo(other.getType().getSimpleType());
    }
}

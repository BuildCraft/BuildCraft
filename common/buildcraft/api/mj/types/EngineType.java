package buildcraft.api.mj.types;

import javax.annotation.Nonnull;

import buildcraft.api.mj.IMjConnectorType;
import buildcraft.api.mj.MjSimpleType;

/** Provides all of the different engine types within buildcraft. */
public enum EngineType implements IMjConnectorType {
    REDSTONE(false),
    STIRLING(true),
    COMBUSTION(true),
    CREATIVE(true);

    @Nonnull
    private final MjSimpleType simple;

    private EngineType(boolean isKinetic) {
        this.simple = isKinetic ? MjSimpleType.KN_PRODUCER_ENGINE : MjSimpleType.RS_PRODUCER_ENGINE;
    }

    @Override
    public boolean is(IMjConnectorType other) {
        return this == other || simple.is(other);
    }

    @Override
    public MjSimpleType getSimpleType() {
        return simple;
    }
}

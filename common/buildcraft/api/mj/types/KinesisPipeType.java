package buildcraft.api.mj.types;

import buildcraft.api.mj.IMjConnectorType;
import buildcraft.api.mj.MjSimpleType;

/** Provides all of the different kinesis pipe types within buildcraft. */
public enum KinesisPipeType implements IMjConnectorType {
    WOODEN,
    COBBLESTONE,
    STONE,
    QUARTZ,
    IRON,
    GOLD,
    DIAMOND;

    @Override
    public boolean is(IMjConnectorType other) {
        return this == other || MjSimpleType.KN_TRANSPORT_PIPE.is(other);
    }

    @Override
    public MjSimpleType getSimpleType() {
        return MjSimpleType.KN_TRANSPORT_PIPE;
    }
}

package buildcraft.api.mj.types;

import javax.annotation.Nonnull;

import buildcraft.api.mj.IMjConnectorType;
import buildcraft.api.mj.MjSimpleType;

public enum MachineType implements IMjConnectorType {
    PIPE_ITEM_WOOD(false),
    PIPE_ITEM_EMERALD(false),

    PIPE_FLUID_WOOD(false),
    PIPE_FLUID_EMERALD(false),

    PIPE_POWER_WOOD(false),

    AUTO_CRAFT_ITEM(false),
    AUTO_CRAFT_FLUID(false),

    PUMP(false),
    BUILDER(true),
    FILLER(true),
    QUARRY(true);

    @Nonnull
    private final MjSimpleType simple;

    private MachineType(boolean isKinetic) {
        this.simple = isKinetic ? MjSimpleType.KN_COSUMER_MACHINE : MjSimpleType.RS_CONSUMER_MACHINE;
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

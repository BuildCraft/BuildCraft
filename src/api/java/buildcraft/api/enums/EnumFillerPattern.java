package buildcraft.api.enums;

import net.minecraft.util.IStringSerializable;

public enum EnumFillerPattern implements IStringSerializable {
    NONE,
    BOX,
    CLEAR,
    CYLINDER,
    FILL,
    FLATTEN,
    FRAME,
    HORIZON,
    PYRAMID,
    STAIRS;

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getName() {
        return name().toLowerCase();
    }
}

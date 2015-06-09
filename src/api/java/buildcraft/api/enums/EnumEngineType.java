package buildcraft.api.enums;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;

public enum EnumEngineType implements IStringSerializable {
    WOOD,
    STONE,
    IRON,
    CREATIVE;

    @Override
    public String getName() {
        return name();
    }

    public String getModelName() {
        return getName().toLowerCase(Locale.ROOT);
    }
}

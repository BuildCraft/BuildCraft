package buildcraft.api.enums;

import org.apache.commons.lang3.StringUtils;

public enum EnumEnergyStage {
    BLUE,
    GREEN,
    YELLOW,
    RED,
    OVERHEAT;
    public static final EnumEnergyStage[] VALUES = values();

    public String getModelName() {
        return StringUtils.lowerCase(name());
    }
}

package buildcraft.lib.recipe.refinery;

import java.util.Locale;

public enum EnumHeatExchangeRecipeType {
    COOLABLE,
    HEATABLE;

    public String getlowerName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static EnumHeatExchangeRecipeType byName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }
}

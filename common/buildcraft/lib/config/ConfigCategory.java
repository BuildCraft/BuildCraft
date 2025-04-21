package buildcraft.lib.config;

import java.util.function.Supplier;

public class ConfigCategory<T> implements Supplier<T> {
    private final String fullPath;
    private final Supplier<T> supplier;
    private final EnumRestartRequirement worldRestart;
    private String lang;

    public ConfigCategory(String fullPath, Supplier<T> supplier, EnumRestartRequirement worldRestart) {
        this.fullPath = fullPath;
        this.supplier = supplier;
        this.worldRestart = worldRestart;
    }

    @Override
    public T get() {
        return supplier.get();
    }

    public void setLanguageKey(String lang) {
        this.lang = lang;
    }

    public String getFullPath() {
        return fullPath;
    }
}

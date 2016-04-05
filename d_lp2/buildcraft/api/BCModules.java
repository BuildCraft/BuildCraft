package buildcraft.api;

import java.util.Locale;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

import buildcraft.api.core.BCLog;

public enum BCModules {
    // Base module for all BC. Includes LIB
    CORE,
    // Potentially optional modules for adding more BC functionality
    BUILDERS,
    ENERGY,
    FACTORY,
    ROBOTICS,
    SILICON,
    TRANSPORT,
    // Optional module for compatibility with other mods
    COMPAT;

    private final String modid;

    private BCModules() {
        this.modid = "buildcraft" + name().toLowerCase(Locale.ROOT);
    }

    public static void init() {}

    public String getModID() {
        return modid;
    }

    public boolean isLoaded() {
        return Loader.isModLoaded(modid);
    }

    static {
        if (!Loader.instance().hasReachedState(LoaderState.CONSTRUCTING)) {
            throw new RuntimeException("Accessed BC modules too early! You can only use them from construction onwards!");
        }
        for (BCModules module : values()) {
            if (module.isLoaded()) {
                BCLog.logger.info("[module-api] Module " + module.name().toLowerCase(Locale.ROOT) + " is loaded!");
            }
        }
    }
}

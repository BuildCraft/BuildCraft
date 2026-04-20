package ct.buildcraft.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;


public enum BCModules implements IBuildCraftMod {
    LIB,
    // Base module for all BC.
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

    public static final BCModules[] VALUES = values();
    private static boolean hasChecked = false;
    private static BCModules[] loadedModules, missingModules;

    public final String lowerCaseName = name().toLowerCase(Locale.ROOT);
    // Bit hacky, but it works as this is all english
    public final String camelCaseName = name().charAt(0) + lowerCaseName.substring(1);
    private final String modId = "buildcraft" + lowerCaseName;
    private boolean loaded;

    private static void checkLoadStatus() {
        if (hasChecked) {
            return;
        }
        load0();
    }

    /** Performs the actual loading of {@link #checkLoadStatus()}, except this is thread safe. */
    private static synchronized void load0() {
        if (hasChecked) {
            return;
        }/*
        if (!Loader.instance().hasReachedState(LoaderState.PREINITIALIZATION)) {
        	FMLLoader.
            throw new RuntimeException("You can only use BCModules.isLoaded from pre-init onwards!");
        }*/
        List<BCModules> found = new ArrayList<>(), missing = new ArrayList<>();
        for (BCModules module : VALUES) {
            module.loaded = ModList.get().isLoaded(module.modId);
            if (module.loaded) {
                found.add(module);
            } else {
                missing.add(module);
            }
        }
        loadedModules = found.toArray(new BCModules[0]);
        missingModules = missing.toArray(new BCModules[0]);
        hasChecked = true;
    }

    @Nullable
    public static BCModules getBcMod(String testModId) {
        for (BCModules mod : VALUES) {
            if (mod.modId.equals(testModId)) {
                return mod;
            }
        }
        return null;
    }

    public static boolean isBcMod(String testModId) {
        return getBcMod(testModId) != null;
    }

    public static BCModules[] getLoadedModules() {
        checkLoadStatus();
        return loadedModules;
    }

    public static BCModules[] getMissingModules() {
        checkLoadStatus();
        return missingModules;
    }

    @Override
    public String getModId() {
        return modId;
    }

    public boolean isLoaded() {
        checkLoadStatus();
        return loaded;
    }

    public ResourceLocation createLocation(String path) {
        return new ResourceLocation(getModId(), path);
    }

    public ModelResourceLocation createModelLocation(String path, String variant) {
        return new ModelResourceLocation(getModId() + ":" + path + "#" + variant);
    }

    public ModelResourceLocation createModelLocation(String pathAndVariant) {
        return new ModelResourceLocation(getModId() + ":" + pathAndVariant);
    }
}

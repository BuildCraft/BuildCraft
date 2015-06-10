package buildcraft.api.core;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraftforge.common.config.Property;

/** Use this to access the various config options. It is recommended that you use this as opposed to the variables in the
 * mod, as users may only install the modules that they want, and not the one you may have refereed to. */
public class ConfigAccessor {
    public enum EMod {
        CORE,
        BUILDERS,
        ENERGY,
        FACTORY,
        ROBITICS,
        SILICON,
        TRANSPORT
    }

    private static Map<EMod, IBuildCraftMod> mods = Maps.newHashMap();

    public static Property getOption(EMod mod, String name) {
        if (mods.containsKey(mod)) {
            return mods.get(mod).getOption(name);
        } else {
            return null;
        }
    }

    public static boolean getBoolean(EMod mod, String name, boolean default_) {
        Property prop = getOption(mod, name);
        if (prop == null) {
            return default_;
        } else {
            return prop.getBoolean(default_);
        }
    }

    /** WARNING: INTERNAL USE ONLY! */
    public static void addMod(EMod mod, IBuildCraftMod actual) {
        mods.put(mod, actual);
    }
}

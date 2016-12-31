package buildcraft.lib.misc;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ConfigUtil {
    /** Sets a good default language key for all of the properties contained in the given configuration */
    public static void setLang(Configuration cfg) {
        for (String s : cfg.getCategoryNames()) {
            ConfigCategory cat = cfg.getCategory(s);
            ConfigCategory p = cat;
            while (p != null) {
                p.setLanguageKey("config." + p.getQualifiedName());
                p = p.parent;
            }
            for (Property prop : cat.values()) {
                prop.setLanguageKey(cat.getLanguagekey() + "." + prop.getName());
            }
        }
    }
}

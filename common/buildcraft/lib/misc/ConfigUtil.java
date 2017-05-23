/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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

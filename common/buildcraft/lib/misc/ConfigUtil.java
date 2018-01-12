/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import buildcraft.api.core.BCLog;

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

    public static <E extends Enum<E>> void setEnumProperty(Property prop, E[] possible) {
        String[] validValues = new String[possible.length];
        for (int i = 0; i < possible.length; i++) {
            validValues[i] = possible[i].name().toLowerCase(Locale.ROOT);
        }
        prop.setValidValues(validValues);
    }

    public static <E extends Enum<E>> E parseEnumForConfig(Property prop, E defaultOption) {
        return parseEnumForConfig(prop, defaultOption.getDeclaringClass().getEnumConstants(), defaultOption);
    }

    public static <E extends Enum<E>> E parseEnumForConfig(Property prop, E[] possible, E defaultOption) {
        Queue<E> match = new LinkedList<>();
        Collections.addAll(match, possible);
        Queue<char[]> lowerCaseNames = new LinkedList<>();
        for (E val : match) {
            lowerCaseNames.add(val.name().toLowerCase(Locale.ROOT).toCharArray());
        }
        char[] chars = prop.getString().toLowerCase(Locale.ROOT).toCharArray();
        for (int i = 0; i < chars.length; i++) {
            Iterator<E> iter = match.iterator();
            Iterator<char[]> iterNames = lowerCaseNames.iterator();
            while (iter.hasNext()) {
                iter.next();
                char[] name = iterNames.next();
                if (name.length < i || name[i] != chars[i]) {
                    iter.remove();
                    iterNames.remove();
                }
            }
            if (match.size() == 1) {
                return getAssumingEqual(prop, match.peek());
            }
        }
        return getAssumingEqual(prop, defaultOption);
    }

    private static <E extends Enum<E>> E getAssumingEqual(Property prop, E mode) {
        String value = prop.getString();
        if (!mode.name().equalsIgnoreCase(value)) {
            BCLog.logger.warn("[lib.config] Unknown " + mode.getClass().getSimpleName() + " for " + prop.getName()
                + " '" + value + "', assuming " + mode.name());
        }
        return mode;
    }
}

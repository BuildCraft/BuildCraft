/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.lib.config.ConfigCategory;
import buildcraft.lib.config.Configuration;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigUtil {
    /** Sets a good default language key for all of the properties contained in the given configuration */
    public static void setLang(Configuration cfg) {
//        for (String s : cfg.getCategoryNames()) {
//            ConfigCategory cat = cfg.getCategory(s);
//            ConfigCategory p = cat;
//            while (p != null) {
//                p.setLanguageKey("config." + p.getQualifiedName());
//                p = p.parent;
//            }
//            for (Property prop : cat.values()) {
//                prop.setLanguageKey(cat.getLanguagekey() + "." + prop.getName());
//            }
//        }
        for (ConfigCategory<?> cat : cfg.getAll()) {
            String catPath = cat.getFullPath();
            while (!"".equals(catPath)) {
                cat.setLanguageKey("config." + catPath);
                String[] splitArray = catPath.split("\\.");
                List<String> splitList = Lists.newArrayList(catPath.split("\\."));
                splitList.remove(splitArray.length - 1);
                catPath = splitList.stream().collect(Collectors.joining("."));
            }
        }
    }

//    public static <E extends Enum<E>> void setEnumProperty(Property prop, E[] possible) {
//        String[] validValues = new String[possible.length];
//        for (int i = 0; i < possible.length; i++) {
//            validValues[i] = possible[i].name().toLowerCase(Locale.ROOT);
//        }
//        prop.setValidValues(validValues);
//    }

//    public static <E extends Enum<E>> E parseEnumForConfig(Property prop, E defaultOption) {
//        return parseEnumForConfig(prop, defaultOption.getDeclaringClass().getEnumConstants(), defaultOption);
//    }

//    public static <E extends Enum<E>> E parseEnumForConfig(Property prop, E[] possible, E defaultOption) {
//        Queue<E> match = new LinkedList<>();
//        Collections.addAll(match, possible);
//        Queue<char[]> lowerCaseNames = new LinkedList<>();
//        for (E val : match) {
//            lowerCaseNames.add(val.name().toLowerCase(Locale.ROOT).toCharArray());
//        }
//        char[] chars = prop.getString().toLowerCase(Locale.ROOT).toCharArray();
//        for (int i = 0; i < chars.length; i++) {
//            Iterator<E> iter = match.iterator();
//            Iterator<char[]> iterNames = lowerCaseNames.iterator();
//            while (iter.hasNext()) {
//                iter.next();
//                char[] name = iterNames.next();
//                if (name.length < i || name[i] != chars[i]) {
//                    iter.remove();
//                    iterNames.remove();
//                }
//            }
//            if (match.size() == 1) {
//                return getAssumingEqual(prop, match.peek(), possible);
//            }
//        }
//        return getAssumingEqual(prop, defaultOption, possible);
//    }

//    private static <E extends Enum<E>> E getAssumingEqual(Property prop, E mode, E[] possible) {
//        String value = prop.getString();
//        if (!mode.name().equalsIgnoreCase(value)) {
//            BCLog.logger.warn("[lib.config] Unknown " + mode.getClass().getSimpleName() + " for " + prop.getName()
//                    + " '" + value + "', assuming " + mode.name());
//            BCLog.logger.warn("[lib.config] Possible values:");
//            for (E e : possible) {
//                BCLog.logger.info("[lib.config]  - '" + e.name().toLowerCase(Locale.ROOT));
//            }
//        }
//        return mode;
//    }
}

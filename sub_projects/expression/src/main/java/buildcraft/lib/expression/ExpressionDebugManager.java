/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import java.util.function.Consumer;

/** Holds the debugging mechanisms for this project. The most useful part of this (for users) is the {@link #logger}
 * field: changing this will allow rerouting logging away from {@link System#out} */
public class ExpressionDebugManager {

    /** Modifiable field to enable or disable debugging for testing. */
    public static boolean debug = false;

    /** Customisable logger to use instead of {@link System#out}. Set by BCLib automatically to
     * <code>BCLog.logger::info</code> */
    public static Consumer<String> logger = System.out::println;

    private static String debugIndentCache = "";

    public static void debugStart(String text) {
        if (debug) {
            ExpressionDebugManager.debugPrintln(text);
            debugIndentCache += "  ";
        }
    }

    public static void debugEnd(String text) {
        if (debug) {
            if (debugIndentCache.length() > 1) {
                debugIndentCache = debugIndentCache.substring(2);
            } else if (debugIndentCache.length() > 0) {
                debugIndentCache = "";
            }
            ExpressionDebugManager.debugPrintln(text);
        }
    }

    public static void debugPrintln(String text) {
        if (debug) {
            logger.accept(debugIndentCache + text);
        }
    }

    public static void debugNodeClass(Class<?> clazz) {
        if (debug) {
            debugPrintln("Unknown node class detected!");
            debugNodeClass0(clazz, " ", true);
        }
    }

    private static void debugNodeClass0(Class<?> clazz, String indent, boolean isBase) {
        if (clazz == null) return;
        String before = isBase ? "" : (clazz.isInterface() ? "implements " : "extends ");
        debugPrintln(indent + before + clazz.getName());
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != Object.class) {
            debugNodeClass0(superClazz, indent + " ", false);
        }
        for (Class<?> inter : clazz.getInterfaces()) {
            debugNodeClass0(inter, indent + " ", false);
        }
    }
}

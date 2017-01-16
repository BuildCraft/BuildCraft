package buildcraft.lib.expression;

import java.util.function.Consumer;

/** Holds the debugging mechanisms for this */
public class ExpressionDebugManager {

    /** Modifiable field to enable or disable debugging for testing. */
    public static boolean debug = false;

    /** Customisable logger to use instead of {@link System#out}. Set by BCLib automatically to
     * <code>BCLog.logger::info</code> */
    public static Consumer<String> logger = null;

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
            if (logger != null) {
                logger.accept(debugIndentCache + text);
            } else {
                // When using a test
                System.out.println(debugIndentCache + text);
            }
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

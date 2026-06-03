// STUB(R.Chen): net.minecraft.util.StringUtils removed in 1.20 — compile shim.
package net.minecraft.util;

/** @deprecated Use {@code org.apache.commons.lang3.StringUtils} or MC 1.20 text utilities instead. */
public final class StringUtils {
    private StringUtils() {}
    public static boolean isNullOrEmpty(String s) { return s == null || s.isEmpty(); }
    public static String stripControlCodes(String s) { return s.replaceAll("§.", ""); }
    public static String nullToEmpty(String s) { return s == null ? "" : s; }
    public static String getTranslationKey(String key) { return key; }
}

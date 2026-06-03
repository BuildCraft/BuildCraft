// STUB(R.Chen): net.minecraft.util.text.translation.I18n removed in 1.13+.
package net.minecraft.util.text.translation;

/** @deprecated Use {@code net.minecraft.client.resource.language.I18n} instead. */
@Deprecated
public final class I18n {
    private I18n() {}
    public static String translateToLocal(String key) { return key; }
    public static String translateToLocalFormatted(String key, Object... args) { return key; }
    public static boolean canTranslate(String key) { return false; }
}

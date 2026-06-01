// STUB(R.Chen): 1.12 I18n → use Text.translatable() in 1.20.1.
package net.minecraft.client.resources;

public class I18n {
    public static String format(String key, Object... args) {
        // TODO(R.Chen): use Text.translatable(key, args).getString()
        return key;
    }
    public static String translateToLocal(String key) { return key; }
    public static String translateToLocalFormatted(String key, Object... args) { return key; }
    public static boolean hasKey(String key) { return false; }
}

// STUB(R.Chen): LanguageMap removed in 1.13+ — compile shim.
package net.minecraft.client.resources;

import java.util.HashMap;
import java.util.Map;

public class LanguageMap {
    private static final LanguageMap INSTANCE = new LanguageMap();
    private final Map<String, String> map = new HashMap<>();

    public static LanguageMap getInstance() { return INSTANCE; }

    public String translateKey(String key) { return map.getOrDefault(key, key); }
    public boolean isKeyTranslated(String key) { return map.containsKey(key); }
    public String translateKeyFormat(String key, Object... args) {
        return String.format(translateKey(key), args);
    }
}

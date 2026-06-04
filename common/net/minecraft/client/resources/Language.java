// STUB(R.Chen): 1.12 Language — compile shim.
package net.minecraft.client.resources;

public class Language {
    private final String code;
    private final String name;
    public Language(String code, String region, String name, boolean rtl) { this.code = code; this.name = name; }
    public String getLanguageCode() { return code; }
    public String toString() { return name; }
}

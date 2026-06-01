// STUB(R.Chen): Forge ConfigElement — compile shim.
package net.minecraftforge.common.config;

import java.util.List;

public class ConfigElement {
    private final Object element;

    public ConfigElement(Object element) { this.element = element; }

    public String getName() { return ""; }
    public String getComment() { return ""; }
    public boolean requiresMcRestart() { return false; }
    public boolean requiresWorldRestart() { return false; }
    public List<Object> getChildElements() { return null; }
}

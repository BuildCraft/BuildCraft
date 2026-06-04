// STUB(R.Chen): Forge ConfigCategory — compile shim.
package net.minecraftforge.common.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigCategory {
    private final String name;
    private final Map<String, Property> properties = new LinkedHashMap<>();

    public ConfigCategory(String name) { this.name = name; }

    public String getName() { return name; }
    public boolean containsKey(String key) { return properties.containsKey(key); }
    public Property get(String key) { return properties.get(key); }
    public void put(String key, Property prop) { properties.put(key, prop); }
    public Collection<Property> values() { return properties.values(); }
    public List<Property> getOrderedValues() { return new ArrayList<>(properties.values()); }
    public boolean isChild() { return false; }
    public String getQualifiedName() { return name; }
    public void setComment(String comment) {}
    public String getComment() { return ""; }
}

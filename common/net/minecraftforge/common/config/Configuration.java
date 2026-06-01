// STUB(R.Chen): Forge Configuration — compile shim. TODO: migrate to Fabric Auto Config or similar.
package net.minecraftforge.common.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
    public static final String CATEGORY_GENERAL = "general";

    private final File file;

    public Configuration(File file) { this.file = file; }
    public Configuration(File file, String version) { this.file = file; }

    public Property get(String category, String key, boolean defaultValue) {
        return new Property(key, String.valueOf(defaultValue), Property.Type.BOOLEAN);
    }
    public Property get(String category, String key, int defaultValue) {
        return new Property(key, String.valueOf(defaultValue), Property.Type.INTEGER);
    }
    public Property get(String category, String key, double defaultValue) {
        return new Property(key, String.valueOf(defaultValue), Property.Type.DOUBLE);
    }
    public Property get(String category, String key, String defaultValue) {
        return new Property(key, defaultValue, Property.Type.STRING);
    }
    public Property get(String category, String key, String defaultValue, String comment) {
        Property p = get(category, key, defaultValue);
        p.setComment(comment);
        return p;
    }
    public Property get(String category, String key, boolean defaultValue, String comment) {
        Property p = get(category, key, defaultValue);
        p.setComment(comment);
        return p;
    }
    public Property get(String category, String key, int defaultValue, String comment) {
        Property p = get(category, key, defaultValue);
        p.setComment(comment);
        return p;
    }
    public Property get(String category, String key, double defaultValue, String comment) {
        Property p = get(category, key, defaultValue);
        p.setComment(comment);
        return p;
    }
    public Property get(String category, String key, String[] defaultValues) {
        return new Property(key, String.join(",", defaultValues), Property.Type.STRING);
    }

    public ConfigCategory getCategory(String category) { return new ConfigCategory(category); }
    public boolean hasCategory(String category) { return false; }
    public void removeCategory(ConfigCategory category) {}

    public void load() {}
    public void save() {}
    public boolean hasChanged() { return false; }
    public List<String> getCategoryNames() { return new ArrayList<>(); }
}

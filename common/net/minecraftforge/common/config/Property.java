// STUB(R.Chen): Forge Configuration.Property — compile shim. TODO: migrate to Fabric Auto Config or similar.
package net.minecraftforge.common.config;

public class Property {
    public enum Type { STRING, INTEGER, BOOLEAN, DOUBLE, COLOR, MOD_ID }

    private String comment = "";
    private String value;
    private final Type type;
    private String[] validValues;

    public Property(String name, String value, Type type) {
        this.value = value;
        this.type = type;
    }

    public boolean getBoolean() { return Boolean.parseBoolean(value); }
    public boolean getBoolean(boolean defaultValue) {
        try { return Boolean.parseBoolean(value); } catch (Exception e) { return defaultValue; }
    }
    public int getInt() { try { return Integer.parseInt(value); } catch (Exception e) { return 0; } }
    public int getInt(int defaultValue) { try { return Integer.parseInt(value); } catch (Exception e) { return defaultValue; } }
    public double getDouble() { try { return Double.parseDouble(value); } catch (Exception e) { return 0.0; } }
    public double getDouble(double defaultValue) { try { return Double.parseDouble(value); } catch (Exception e) { return defaultValue; } }
    public String getString() { return value != null ? value : ""; }

    public void setValue(String value) { this.value = value; }
    public void set(boolean value) { this.value = String.valueOf(value); }
    public void set(int value) { this.value = String.valueOf(value); }
    public void set(double value) { this.value = String.valueOf(value); }
    public void set(String value) { this.value = value; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String[] getValidValues() { return validValues; }
    public void setValidValues(String[] validValues) { this.validValues = validValues; }

    public boolean requiresMcRestart() { return false; }
    public boolean requiresWorldRestart() { return false; }
    public void setRequiresMcRestart(boolean value) {}
    public void setRequiresWorldRestart(boolean value) {}

    public void setToDefault() {}
    public void setDefaultValue(String value) {}
    public void setDefaultValue(boolean value) {}
    public void setDefaultValue(int value) {}
    public void setDefaultValue(double value) {}
    public void setValueEntries(String[] values) { this.validValues = values; }

    public String getType() { return type.name(); }
    public String getName() { return ""; }
    public String getDefault() { return ""; }
    public boolean hasChanged() { return false; }
}

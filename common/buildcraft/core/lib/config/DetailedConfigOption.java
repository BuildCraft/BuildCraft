package buildcraft.core.lib.config;

import buildcraft.api.core.BCLog;

public class DetailedConfigOption {
    final String id, defaultVal;
    String cache;
    boolean hasWarned;
    private boolean cacheBoolean;
    private long cacheLong;
    private double cacheDouble;
    // private Expression cacheExpression;

    public DetailedConfigOption(String name, String defultVal) {
        this.id = name;
        this.defaultVal = defultVal;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        return ((DetailedConfigOption) obj).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getAsString() {
        DetailedConfigManager.INSTANCE.refresh(this);
        return cache;
    }

    public boolean getAsBoolean() {
        if (DetailedConfigManager.INSTANCE.refresh(this)) {
            cacheBoolean = "true".equals(cache);
        }
        return cacheBoolean;
    }

    public long getAsLong() {
        if (DetailedConfigManager.INSTANCE.refresh(this)) {
            try {
                cacheLong = Long.parseLong(cache);
            } catch (NumberFormatException nfe) {
                BCLog.logger.warn("Invalid option for " + id + ":" + cache + ", wanted an integer!");
                cacheLong = 0;
            }
        }
        return cacheLong;
    }

    public int getAsInt() {
        return (int) getAsLong();
    }

    public char getAsChar() {
        return (char) getAsLong();
    }

    public byte getAsByte() {
        return (byte) getAsLong();
    }

    public double getAsDouble() {
        if (DetailedConfigManager.INSTANCE.refresh(this)) {
            try {
                cacheDouble = Double.parseDouble(cache);
            } catch (NumberFormatException nfe) {
                BCLog.logger.warn("Invalid option for " + id + ":" + cache + ", wanted a floating-point!");
                cacheDouble = 0;
            }
        }
        return cacheDouble;
    }

    public float getAsFloat() {
        return (float) getAsDouble();
    }

    // public Expression getAsExpression() {
    // // Compile expression
    // }
}

package buildcraft.core.lib.config;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BCLog;
import buildcraft.core.lib.config.ExpressionCompiler.InvalidExpressionException;

public class DetailedConfigOption {
    private static final Set<DetailedConfigOption> allRegistered = new HashSet<>();

    private final String id, defaultVal;
    String cache;
    boolean hasWarned;
    private boolean cacheBoolean;
    private long cacheLong;
    private double cacheDouble;
    private Expression cacheExpression;

    public DetailedConfigOption(String name, String defultVal) {
        this.id = name;
        this.defaultVal = defultVal;
        allRegistered.add(this);
    }

    public static void reloadAll() {
        allRegistered.forEach(r -> r.reload());
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

    protected void reload() {
        cache = null;
        cacheExpression = null;
    }

    public final String defaultValue() {
        return defaultVal;
    }

    protected boolean refresh() {
        return BuildCraftCore.detailedConfigManager.refresh(this, id);
    }

    public String getAsString() {
        refresh();
        return cache;
    }

    public boolean getAsBoolean() {
        if (refresh()) cacheBoolean = "true".equals(cache);
        return cacheBoolean;
    }

    public long getAsLong() {
        if (refresh()) {
            try {
                cacheLong = Long.parseLong(cache);
            } catch (NumberFormatException nfe) {
                BCLog.logger.warn("Invalid option for " + id + ":" + cache + ", wanted an integer!");
                cacheLong = 0;
            }
        }
        return cacheLong;
    }

    public double getAsDouble() {
        if (refresh()) {
            try {
                cacheDouble = Double.parseDouble(cache);
            } catch (NumberFormatException nfe) {
                BCLog.logger.warn("Invalid option for " + id + ":" + cache + ", wanted a floating-point!");
                cacheDouble = 0;
            }
        }
        return cacheDouble;
    }

    public Expression getAsExpression() {
        if (refresh()) {
            try {
                String string = getAsString();
                cacheExpression = ExpressionCompiler.compileExpression(string);
            } catch (InvalidExpressionException iee) {
                BCLog.logger.warn("Invalid expression for " + id + ":" + cache + ", wanted a valid expression!");
                BCLog.logger.warn("Error: " + iee.getMessage());
                cacheLong = 0;
            }
        }
        return cacheExpression;
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

    public float getAsFloat() {
        return (float) getAsDouble();
    }

    // Helper methods

    public float getAsFloatCapped(float min, float max) {
        return Math.min(max, Math.max(min, getAsFloat()));
    }

    @SideOnly(Side.CLIENT)
    public enum ReloadListener implements IResourceManagerReloadListener {
        INSTANCE;

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {
            reloadAll();
        }
    }
}

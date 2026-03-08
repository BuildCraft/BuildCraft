/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.config;

import buildcraft.api.core.BCLog;
import buildcraft.core.BCCoreConfig;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;

public class DetailedConfigOption {
    private static final Set<DetailedConfigOption> allRegistered = new HashSet<>();

    private final String id, defaultVal;
    String cache;
    boolean hasWarned;
    private boolean cacheBoolean;
    private long cacheLong;
    private double cacheDouble;
    private IExpressionNode cacheExpression;

    public DetailedConfigOption(String name, String defaultVal) {
        this.id = name;
        this.defaultVal = defaultVal;
        allRegistered.add(this);
    }

    public static void reloadAll() {
        for (DetailedConfigOption dco : allRegistered) {
            dco.reload();
        }
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
        return BCCoreConfig.detailedConfigManager.refresh(this, id);
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
                BCLog.logger.warn("Invalid option for " + id + ":" + cache + ", wanted an integer! " + nfe.getMessage());
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
                BCLog.logger.warn("Invalid option for " + id + ":" + cache + ", wanted a floating-point! " + nfe.getMessage());
                cacheDouble = 0;
            }
        }
        return cacheDouble;
    }

    public IExpressionNode getAsExpression() {
        if (refresh()) {
            try {
                String string = getAsString();
                cacheExpression = InternalCompiler.compileExpression(string, DefaultContexts.createWithAll());
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

    @OnlyIn(Dist.CLIENT)
    public enum ReloadListener implements IResourceManagerReloadListener {
        INSTANCE;

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {
            reloadAll();
        }
    }
}

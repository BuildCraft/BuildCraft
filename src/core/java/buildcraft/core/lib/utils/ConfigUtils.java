/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.utils;

import org.apache.logging.log4j.Level;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import buildcraft.api.core.BCLog;

public class ConfigUtils {

    private static final String COMMENT_PREFIX = "";
    private static final String COMMENT_SUFFIX = "";
    private final Configuration config;
    private final String cat;

    public ConfigUtils(Configuration config, String cat) {
        this.config = config;
        this.cat = cat;
    }

    public boolean get(String tag, boolean defaultValue, String comment) {
        return get(tag, defaultValue, false, comment);
    }

    public boolean get(String tag, boolean defaultValue, boolean reset, String comment) {
        Property prop = config.get(cat, tag, defaultValue);
        prop.comment = COMMENT_PREFIX + comment.replace("{t}", tag) + COMMENT_SUFFIX;

        boolean ret = parseBoolean(prop, defaultValue);

        if (reset) {
            prop.set(defaultValue);
        }

        return ret;
    }

    public int get(String tag, int defaultValue, String comment) {
        Property prop = config.get(cat, tag, defaultValue);
        prop.comment = COMMENT_PREFIX + comment.replace("{t}", tag) + COMMENT_SUFFIX;
        return parseInteger(prop, defaultValue);
    }

    public int get(String tag, int min, int defaultValue, int max, String comment) {
        Property prop = config.get(cat, tag, defaultValue);
        prop.comment = COMMENT_PREFIX + comment.replace("{t}", tag) + COMMENT_SUFFIX;
        int parsed = parseInteger(prop, defaultValue);
        int clamped = Math.max(parsed, min);
        clamped = Math.min(clamped, max);
        if (clamped != parsed) {
            prop.set(clamped);
        }
        return clamped;
    }

    public float get(String tag, float min, float defaultValue, float max, String comment) {
        Property prop = config.get(cat, tag, defaultValue);
        prop.comment = COMMENT_PREFIX + comment.replace("{t}", tag) + COMMENT_SUFFIX;
        double parsed = parseDouble(prop, defaultValue);
        double clamped = Math.max(parsed, min);
        clamped = Math.min(clamped, max);
        if (clamped != parsed) {
            prop.set(clamped);
        }
        return (float) clamped;
    }

    private boolean parseBoolean(Property prop, boolean defaultValue) {
        String value = prop.getString();
        boolean parsed;
        try {
            parsed = Boolean.parseBoolean(value);
        } catch (NumberFormatException ex) {
            BCLog.logger.log(Level.WARN, "Failed to parse config tag, reseting to default: " + prop.getName(), ex);
            prop.set(defaultValue);
            return defaultValue;
        }
        return parsed;
    }

    private int parseInteger(Property prop, int defaultValue) {
        String value = prop.getString();
        int parsed;
        try {
            parsed = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            BCLog.logger.log(Level.WARN, "Failed to parse config tag, reseting to default: " + prop.getName(), ex);
            prop.set(defaultValue);
            return defaultValue;
        }
        return parsed;
    }

    private double parseDouble(Property prop, double defaultValue) {
        String value = prop.getString();
        double parsed;
        try {
            parsed = Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            BCLog.logger.log(Level.WARN, "Failed to parse config tag, reseting to default: " + prop.getName(), ex);
            prop.set(defaultValue);
            return defaultValue;
        }
        return parsed;
    }
}

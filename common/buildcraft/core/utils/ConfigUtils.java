/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import buildcraft.BuildCraftCore;
import java.util.logging.Level;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
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
		comment = COMMENT_PREFIX + comment.replace("{t}", tag) + COMMENT_SUFFIX;
		Property prop = config.get(cat, tag, defaultValue);
		prop.comment = comment;
		boolean ret = parseBoolean(prop, defaultValue);
		if (reset)
			prop.set(defaultValue);
		return ret;
	}

	public int get(String tag, int defaultValue, String comment) {
		comment = COMMENT_PREFIX + comment.replace("{t}", tag) + COMMENT_SUFFIX;
		Property prop = config.get(cat, tag, defaultValue);
		prop.comment = comment;
		return parseInteger(prop, defaultValue);
	}

	public int get(String tag, int min, int defaultValue, int max, String comment) {
		comment = COMMENT_PREFIX + comment.replace("{t}", tag) + COMMENT_SUFFIX;
		Property prop = config.get(cat, tag, defaultValue);
		prop.comment = comment;
		int parsed = parseInteger(prop, defaultValue);
		int clamped = Math.max(parsed, min);
		clamped = Math.min(clamped, max);
		if (clamped != parsed)
			prop.set(clamped);
		return clamped;
	}

	public float get(String tag, float min, float defaultValue, float max, String comment) {
		comment = COMMENT_PREFIX + comment.replace("{t}", tag) + COMMENT_SUFFIX;
		Property prop = config.get(cat, tag, defaultValue);
		prop.comment = comment;
		double parsed = parseDouble(prop, defaultValue);
		double clamped = Math.max(parsed, min);
		clamped = Math.min(clamped, max);
		if (clamped != parsed)
			prop.set(clamped);
		return (float) clamped;
	}

	private boolean parseBoolean(Property prop, boolean defaultValue) {
		String value = prop.getString();
		boolean parsed;
		try {
			parsed = Boolean.parseBoolean(value);
		} catch (NumberFormatException ex) {
			BCLog.logger.log(Level.WARNING, "Failed to parse config tag, reseting to default: " + prop.getName(), ex);
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
			BCLog.logger.log(Level.WARNING, "Failed to parse config tag, reseting to default: " + prop.getName(), ex);
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
			BCLog.logger.log(Level.WARNING, "Failed to parse config tag, reseting to default: " + prop.getName(), ex);
			prop.set(defaultValue);
			return defaultValue;
		}
		return parsed;
	}
}

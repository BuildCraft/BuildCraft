/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.builders.patterns;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.TreeMap;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.filler.IFillerRegistry;

public class FillerRegistry implements IFillerRegistry {

	private TreeMap<String, IFillerPattern> patterns = new TreeMap<String, IFillerPattern>();

	@Override
	public void addPattern(IFillerPattern pattern) {
		patterns.put(pattern.getUniqueTag(), pattern);
	}

	@Override
	public IFillerPattern getPattern(String patternName) {
		return patterns.get(patternName);
	}

	@Override
	public IFillerPattern getNextPattern(IFillerPattern currentPattern) {
		Entry<String, IFillerPattern> pattern = patterns.higherEntry(currentPattern.getUniqueTag());
		if (pattern == null) {
			pattern = patterns.firstEntry();
		}
		return pattern.getValue();
	}

	@Override
	public IFillerPattern getPreviousPattern(IFillerPattern currentPattern) {
		Entry<String, IFillerPattern> pattern = patterns.lowerEntry(currentPattern.getUniqueTag());
		if (pattern == null) {
			pattern = patterns.lastEntry();
		}
		return pattern.getValue();
	}

	@Override
	public Collection<IFillerPattern> getPatterns() {
		return Collections.unmodifiableCollection(patterns.values());
	}
}

/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.filler;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.filler.IFillerRegistry;
import buildcraft.builders.triggers.ActionFiller;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class FillerRegistry implements IFillerRegistry {

	private TreeMap<String, IFillerPattern> patterns = new TreeMap<String, IFillerPattern>();
	private Set<ActionFiller> patternActions = new HashSet<ActionFiller>();

	@Override
	public void addPattern(IFillerPattern pattern) {
		patterns.put(pattern.getUniqueTag(), pattern);
		patternActions.add(new ActionFiller(pattern));
	}

	@Override
	public IFillerPattern getPattern(String patternName) {
		return patterns.get(patternName);
	}

	@Override
	public IFillerPattern getNextPattern(IFillerPattern currentPattern) {
		Entry<String, IFillerPattern> pattern = patterns.higherEntry(currentPattern.getUniqueTag());
		if (pattern == null)
			pattern = patterns.firstEntry();
		return pattern.getValue();
	}

	@Override
	public IFillerPattern getPreviousPattern(IFillerPattern currentPattern) {
		Entry<String, IFillerPattern> pattern = patterns.lowerEntry(currentPattern.getUniqueTag());
		if (pattern == null)
			pattern = patterns.lastEntry();
		return pattern.getValue();
	}

	@Override
	public Set<ActionFiller> getActions() {
		return patternActions;
	}
}

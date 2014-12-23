/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.filler;

import java.util.Collection;

public interface IFillerRegistry {
	void addPattern(IFillerPattern pattern);

	IFillerPattern getPattern(String patternName);

	IFillerPattern getNextPattern(IFillerPattern currentPattern);

	IFillerPattern getPreviousPattern(IFillerPattern currentPattern);
	
	Collection<IFillerPattern> getPatterns();
}

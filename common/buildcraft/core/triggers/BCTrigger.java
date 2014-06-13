/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import buildcraft.api.gates.ITrigger;

/**
 * This class has to be implemented to create new triggers kinds to BuildCraft
 * gates. There is an instance per kind, which will get called wherever the
 * trigger can be active.
 */
public abstract class BCTrigger extends BCStatement implements ITrigger {

	public BCTrigger(String... uniqueTag) {
		super(uniqueTag);
	}

}

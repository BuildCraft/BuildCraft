/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gates;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public final class GateExpansions {

	public static final Map<String, IGateExpansion> expansions = new HashMap<String, IGateExpansion>();

	private GateExpansions() {
	}

	public static void registerExpansion(IGateExpansion expansion) {
		expansions.put(expansion.getUniqueIdentifier(), expansion);
	}
}

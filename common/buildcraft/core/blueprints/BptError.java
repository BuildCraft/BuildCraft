/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import buildcraft.api.core.BCLog;

public class BptError extends Exception {
	private static final long serialVersionUID = 3579188081467555542L;

	public BptError(String str) {
		super(str);

		BCLog.logger.debug("BLUEPRINT ERROR:" + str);
	}
}

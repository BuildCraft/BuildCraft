/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.gates;

import buildcraft.api.statements.containers.ISidedStatementContainer;
import buildcraft.api.transport.IPipe;

public interface IGate extends ISidedStatementContainer {
	@Deprecated
	void setPulsing(boolean pulse);

	IPipe getPipe();
}

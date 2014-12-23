/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.transport;

import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IPipeTile.PipeType;

public interface IPipeConnection {

	enum ConnectOverride {

		CONNECT, DISCONNECT, DEFAULT
	}

	/**
	 * Allows you to override pipe connection logic.
	 *
	 * @param type
	 * @param with
	 * @return CONNECT to force a connection, DISCONNECT to force no connection,
	 * and DEFAULT to let the pipe decide.
	 */
	ConnectOverride overridePipeConnection(PipeType type, EnumFacing with);
}

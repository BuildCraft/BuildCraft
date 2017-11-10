/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network;

import buildcraft.api.core.ISerializable;

public interface ISyncedTile {

	/**
	 * called by the PacketHandler for each state contained in a StatePacket
	 *
	 * @param stateId
	 * @return an object that should be refreshed from the state
	 */
	ISerializable getStateInstance(byte stateId);

	/**
	 * Called after a state has been updated
	 *
	 * @param stateId
	 */
	void afterStateUpdated(byte stateId);
}

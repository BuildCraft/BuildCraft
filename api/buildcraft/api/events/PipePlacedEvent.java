/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.events;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.eventhandler.Event;

public class PipePlacedEvent extends Event {
	public EntityPlayer player;
	public String pipeType;
	public int x, y, z;

	public PipePlacedEvent(EntityPlayer player, String pipeType, int x, int y, int z) {
		this.player = player;
		this.pipeType = pipeType;
		this.x = x;
		this.y = y;
		this.z = z;
	}

}

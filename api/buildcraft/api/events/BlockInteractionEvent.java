/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.events;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

@Cancelable
public class BlockInteractionEvent extends Event {
	public EntityPlayer player;
	public Block block;
	public int meta;

	public BlockInteractionEvent(EntityPlayer player, Block block) {
		this.player = player;
		this.block = block;
	}

	public BlockInteractionEvent(EntityPlayer player, Block block, int meta) {
		this.player = player;
		this.block = block;
		this.meta = meta;
	}
}

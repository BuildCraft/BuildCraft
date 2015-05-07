/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
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
public class BlockPlacedDownEvent extends Event {
	public EntityPlayer player;
	public Block block;
	public int meta, x, y, z;

	public BlockPlacedDownEvent(EntityPlayer player, Block block, int meta, int x, int y, int z) {
		this.player = player;
		this.block = block;
		this.meta = meta;
		this.x = x;
		this.y = y;
		this.z = z;
	}
}

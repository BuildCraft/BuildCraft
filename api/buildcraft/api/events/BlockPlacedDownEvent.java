package buildcraft.api.events;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;


@Cancelable
public class BlockPlacedDownEvent extends Event {
	public EntityPlayer player;
	public Block block;
	public int meta;

	public BlockPlacedDownEvent(EntityPlayer player, Block block, int meta) {
		this.player = player;
		this.block = block;
		this.meta = meta;
	}
}

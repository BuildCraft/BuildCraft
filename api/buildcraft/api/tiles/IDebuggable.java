package buildcraft.api.tiles;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * I would like to ask that this interface is not called by any
 * non-Creative Mode-only item, and especially not computer mods.
 * This is because often, the debug information can and will let you
 * "cheat", similar to how F3 gives you debug information about
 * Minecraft's inner engine workings.
 */
public interface IDebuggable {
	/**
	 * Get the debug information from a tile entity as a list of strings,
	 * usable with the BuildCraft Debugger.
	 * @param info The List debug strings should be output to.
	 * @param side The side of the tile.
	 * @param debugger The debugger ItemStack used.
	 * @param player The player querying the debug information.
	 */
	void getDebugInfo(List<String> info, ForgeDirection side, ItemStack debugger, EntityPlayer player);
}

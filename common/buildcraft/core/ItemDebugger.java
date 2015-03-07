package buildcraft.core;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.lib.items.ItemBuildCraft;

/**
 * Created by asie on 3/7/15.
 */
public class ItemDebugger extends ItemBuildCraft {
	public ItemDebugger() {
		super();

		setFull3D();
		setMaxStackSize(1);
	}
	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}

		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof IDebuggable) {
			ArrayList<String> info = new ArrayList<String>();
			((IDebuggable) tile).getDebugInfo(info, ForgeDirection.getOrientation(side), stack, player);
			for (String s : info) {
				player.addChatComponentMessage(new ChatComponentText(s));
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		return true;
	}
}

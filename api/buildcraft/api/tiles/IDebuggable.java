package buildcraft.api.tiles;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public interface IDebuggable {
	void getDebugInfo(List<String> info, ForgeDirection side, ItemStack debugger, EntityPlayer player);
}

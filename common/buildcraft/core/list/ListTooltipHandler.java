package buildcraft.core.list;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import buildcraft.api.items.IList;
import buildcraft.core.lib.utils.StringUtils;

public class ListTooltipHandler {
	@SubscribeEvent
	public void itemTooltipEvent(ItemTooltipEvent event) {
		if (event.itemStack != null && event.entityPlayer != null && event.entityPlayer.openContainer != null
				&& event.entityPlayer.openContainer instanceof ContainerListNew) {
			ItemStack list = event.entityPlayer.getCurrentEquippedItem();
			if (list != null && list.getItem() instanceof IList) {
				if (((IList) list.getItem()).matches(list, event.itemStack)) {
					event.toolTip.add(EnumChatFormatting.GREEN + StringUtils.localize("tip.list.matches"));
				}
			}
		}
	}
}

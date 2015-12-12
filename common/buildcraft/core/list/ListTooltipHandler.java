package buildcraft.core.list;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.items.IList;
import buildcraft.core.lib.utils.BCStringUtils;

public class ListTooltipHandler {
    @SubscribeEvent
    public void itemTooltipEvent(ItemTooltipEvent event) {
        if (event.itemStack != null && event.entityPlayer != null && event.entityPlayer.openContainer != null
            && event.entityPlayer.openContainer instanceof ContainerListNew) {
            ItemStack list = event.entityPlayer.getCurrentEquippedItem();
            if (list != null && list.getItem() instanceof IList) {
                if (((IList) list.getItem()).matches(list, event.itemStack)) {
                    event.toolTip.add(EnumChatFormatting.GREEN + BCStringUtils.localize("tip.list.matches"));
                }
            }
        }
    }
}

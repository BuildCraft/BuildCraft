package buildcraft.core.list;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.items.IList;

import buildcraft.lib.misc.LocaleUtil;

public enum ListTooltipHandler {
    INSTANCE;

    @SubscribeEvent
    public void itemTooltipEvent(ItemTooltipEvent event) {
        final EntityPlayer player = event.getEntityPlayer();
        final ItemStack stack = event.getItemStack();
        if (stack != null && player != null && player.openContainer instanceof ContainerList) {
            ItemStack list = player.getHeldItemMainhand();
            if (list != null && list.getItem() instanceof IList) {
                if (((IList) list.getItem()).matches(list, stack)) {
                    event.getToolTip().add(TextFormatting.GREEN + LocaleUtil.localize("tip.list.matches"));
                }
            }
        }
    }
}

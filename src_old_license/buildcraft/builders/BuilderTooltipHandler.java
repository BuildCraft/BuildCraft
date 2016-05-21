package buildcraft.builders;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.builders.gui.ContainerBuilder;
import buildcraft.core.blueprints.RequirementItemStack;

/** Created by asie on 10/6/15. */
public class BuilderTooltipHandler {
    @SubscribeEvent
    public void itemTooltipEvent(ItemTooltipEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (event.getItemStack() != null && player != null && player.openContainer != null && player.openContainer instanceof ContainerBuilder) {
            ContainerBuilder containerBuilder = (ContainerBuilder) player.openContainer;
            TileBuilder builder = containerBuilder.getBuilder();
            if (builder != null) {
                List<RequirementItemStack> needs = builder.getNeededItems();
                if (needs != null) {
                    for (RequirementItemStack ris : needs) {
                        if (ris.stack == event.getItemStack()) {
                            event.getToolTip().add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + "Needed: " + ris.size);
                        }
                    }
                }
            }
        }
    }
}

package buildcraft.builders;

import java.util.List;

import net.minecraft.util.EnumChatFormatting;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import buildcraft.builders.gui.ContainerBuilder;
import buildcraft.core.blueprints.RequirementItemStack;

/**
 * Created by asie on 10/6/15.
 */
public class BuilderTooltipHandler {
	@SubscribeEvent
	public void itemTooltipEvent(ItemTooltipEvent event) {
		if (event.itemStack != null && event.entityPlayer != null && event.entityPlayer.openContainer != null
				&& event.entityPlayer.openContainer instanceof ContainerBuilder) {
			ContainerBuilder containerBuilder = (ContainerBuilder) event.entityPlayer.openContainer;
			TileBuilder builder = containerBuilder.getBuilder();
			if (builder != null) {
				List<RequirementItemStack> needs = builder.getNeededItems();
				if (needs != null) {
					for (RequirementItemStack ris : needs) {
						if (ris.stack == event.itemStack) {
							event.toolTip.add(EnumChatFormatting.GRAY + "" + EnumChatFormatting.ITALIC + "Needed: " + ris.size);
						}
					}
				}
			}
		}
	}
}

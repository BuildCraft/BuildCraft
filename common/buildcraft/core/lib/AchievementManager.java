package buildcraft.core.lib;

import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.common.AchievementPage;

public class AchievementManager {
	public AchievementPage page;

	public AchievementManager(String name) {
		page = new AchievementPage(name);
		AchievementPage.registerAchievementPage(page);

		FMLCommonHandler.instance().bus().register(this);
	}

	public Achievement registerAchievement(Achievement a) {
		page.getAchievements().add(a.registerStat());
		return a;
	}

	@SubscribeEvent
	public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
		Item item = event.crafting.getItem();
		int damage = event.crafting.getItemDamage();

		for (Achievement a : page.getAchievements()) {
			if (item.equals(a.theItemStack.getItem()) && damage == a.theItemStack.getItemDamage()) {
				event.player.addStat(a, 1);
			}
		}
	}
}

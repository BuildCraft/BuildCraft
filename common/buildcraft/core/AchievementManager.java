package buildcraft.core;

import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.common.AchievementPage;

public class AchievementManager {
	public AchievementPage page;

	public AchievementManager() {
		page = new AchievementPage("BuildCraft");
		AchievementPage.registerAchievementPage(page);
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

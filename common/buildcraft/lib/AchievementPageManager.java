/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package buildcraft.lib;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class AchievementPageManager {
    public final AchievementPage page;

    public AchievementPageManager(String name) {
        page = new AchievementPage(name);
        AchievementPage.registerAchievementPage(page);
    }

    private Achievement getRegisteredParent(Achievement achievement) {
        if (achievement == null) return null;
        if (AchievementPage.isAchievementInPages(achievement)) return achievement;
        return getRegisteredParent(achievement.parentAchievement);
    }

    /** Will register an {@link Achievement} in this pages, but only if the corresponding block or item is registered
     * with the game. */
    public Achievement registerAchievement(Achievement achievement) {
        boolean registered;
		if (achievement.theItemStack != null && achievement.theItemStack.getItem() != null) {
			Item item = achievement.theItemStack.getItem();
			if (item instanceof ItemBlock) {
				Block block = ((ItemBlock) item).getBlock();
				registered = Block.REGISTRY.getNameForObject(block) != null;
			} else {
				registered = Item.REGISTRY.getNameForObject(item) != null;
			}
			if (registered) {
				page.getAchievements().add(achievement);
				achievement.registerStat();
				return achievement;
			} else {
				return getRegisteredParent(achievement);
			}
		} else {
			return getRegisteredParent(achievement);
		}
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

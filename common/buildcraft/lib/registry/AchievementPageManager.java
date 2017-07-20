/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;

import buildcraft.lib.misc.RegistryUtil;
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
                registered = RegistryUtil.isRegistered(block);
            } else {
                registered = RegistryUtil.isRegistered(item);
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

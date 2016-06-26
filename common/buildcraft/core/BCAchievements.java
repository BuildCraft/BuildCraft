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
package buildcraft.core;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

import buildcraft.lib.AchievementPageManager;

public class BCAchievements {
    // public static final AchievementPageManager mainPage;
    // public static final Achievement mainPageGearWood;
    // public static final Achievement mainPageGearStone;
    // public static final Achievement mainPageGearIron;
    // public static final Achievement mainPageGearGold;
    // public static final Achievement mainPageGearDiamond;
    // public static final Achievement mainPageWrench;
    // public static final Achievement mainPageEngineRedstone;
    // public static final Achievement mainPageEngineStirling;
    // public static final Achievement mainPageEngineCombustion;

    static {
        if (!Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) {
            throw new RuntimeException("Accessed BC Achievements too early! You can only use them from init onwards!");
        }
        // mainPage = new AchievementPageManager("BuildCraft");
        // mainPageGearWood = register(mainPage, "woodenGear", "woodenGearAchievement", 0, 0, BCItems.coreGearWood,
        // null);
        // mainPageGearStone = register(mainPage, "stoneGear", "stoneGearAchievement", 2, 0, BCItems.coreGearStone,
        // mainPageGearWood);
        // mainPageGearIron = register(mainPage, "ironGear", "ironGearAchievement", 4, 0, BCItems.coreGearIron,
        // mainPageGearStone);
        // mainPageGearGold = register(mainPage, "goldGear", "goldGearAchievement", 6, 0, BCItems.coreGearGold,
        // mainPageGearIron);
        // mainPageGearDiamond = register(mainPage, "diamondGear", "diamondGearAchievement", 8, 0,
        // BCItems.coreGearDiamond, mainPageGearGold);
        // mainPageWrench = register(mainPage, "wrench", "wrenchAchievement", 3, 2, BCItems.coreWrench,
        // mainPageGearStone);
        // mainPageEngineRedstone = register(mainPage, "redstoneEngine", "engineAchievement1", 1, -2,
        // BCBlocks.coreEngineRedstone, mainPageGearWood);
    }

    public static void init() {
        // MinecraftForge.EVENT_BUS.register(mainPage);
    }

    private static Achievement register(AchievementPageManager page, String id, String unloc, int row, int column, Item item, Achievement parent) {
        return register(page, id, unloc, row, column, item == null ? null : new ItemStack(item), parent);
    }

    private static Achievement register(AchievementPageManager page, String id, String unloc, int row, int column, Block block, Achievement parent) {
        return register(page, id, unloc, row, column, block == null ? null : new ItemStack(block), parent);
    }

    private static Achievement register(AchievementPageManager page, String id, String unloc, int column, int row, ItemStack stack, Achievement parent) {
        Achievement ach = new Achievement("buildcraft:achievement." + id, unloc, column, row, stack, parent);
        return page.registerAchievement(ach);
    }
}

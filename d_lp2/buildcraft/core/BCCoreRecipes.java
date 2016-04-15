package buildcraft.core;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import buildcraft.lib.TagManager;
import buildcraft.lib.TagManager.EnumTagType;

public class BCCoreRecipes {
    public static void init() {
//        if (BCCoreItems.wrench != null) {
//            ItemStack out = new ItemStack(BCCoreItems.wrench);
//            Object[] in = { "I I", " G ", " I ", 'I', "ingotIron", 'G', "gearStone" };
//            BCRegistry.INSTANCE.addCraftingRecipe(out, in);
//        }
//
//        Item list = BCCoreItems.list;
//        if (list != null) {
//            // Convert old lists to new lists
//            BCRegistry.INSTANCE.addShapelessRecipe(new ItemStack(list, 1, 1), new ItemStack(list, 1, 0));
//            if (BCModules.SILICON.isLoaded()) {
//                Object[] input = { "dyeGreen", "dustRedstone", new ItemStack(Items.PAPER, 8) };
//                ItemStack output = new ItemStack(BuildCraftCore.listItem, 1, 1);
//                BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:list", 20000, output, input);
//            } else {
//                BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(list, 1, 1), "ppp", "pYp", "ppp", 'p', Items.PAPER, 'Y', "dyeGreen");
//            }
//        }
//
//        if (BCCoreItems.mapLocation != null) {
//            BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(BCCoreItems.mapLocation), "ppp", "pYp", "ppp", 'p', Items.PAPER, 'Y', "dyeYellow");
//        }
//
//        if (BCCoreItems.paintbrush != null) {
//            Object[] input = { " iw", " gi", "s  ", 's', "stickWood", 'g', "gearWood", 'w', new ItemStack(Blocks.WOOL, 1, 0), 'i', Items.STRING };
//            BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(BCCoreItems.paintbrush), input);
//        }

        String[] gears = { "wood", "stone", "iron", "gold", "diamond" };
        Object[] outers = { "stickWood", "cobblestone", "ingotIron", "ingotGold", "gemDiamond" };
        for (int i = 0; i < gears.length; i++) {
            String key = gears[i];
            Item gear = TagManager.getItem("item.gear." + key);
            if (gear == null) continue;
            Object inner = i == 0 ? null : TagManager.getTag("item.gear." + gears[i - 1], EnumTagType.OREDICT_NAME);
            Object outer = outers[i];
            if (inner == null) {
                Object[] arr = { " o ", "o o", " o ", 'o', outer };
                BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(gear), arr);
            } else {
                Object[] arr = { " o ", "oio", " o ", 'o', outer, 'i', inner };
                BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(gear), arr);
            }
        }
    }
}

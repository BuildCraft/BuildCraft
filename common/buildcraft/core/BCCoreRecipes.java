package buildcraft.core;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.lib.TagManager;
import buildcraft.lib.TagManager.EnumTagType;
import buildcraft.lib.misc.ColourUtil;

public class BCCoreRecipes {
    public static void init() {
        if (BCCoreItems.wrench != null) {
            ItemStack out = new ItemStack(BCCoreItems.wrench);
            Object[] in = { "I I", " G ", " I ", 'I', "ingotIron", 'G', "gearStone" };
            GameRegistry.addRecipe(new ShapedOreRecipe(out, in));
        }

        if (BCCoreBlocks.markerVolume != null) {
            ItemStack out = new ItemStack(BCCoreBlocks.markerVolume);
            ItemStack lapisLazuli = new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage());
            Object[] in = { "l", "t", 'l', lapisLazuli, 't', Blocks.REDSTONE_TORCH };
            GameRegistry.addRecipe(new ShapedOreRecipe(out, in));
        }

        if (BCCoreBlocks.markerPath != null) {
            ItemStack out = new ItemStack(BCCoreBlocks.markerPath);
            ItemStack cactusGreen = new ItemStack(Items.DYE, 1, EnumDyeColor.GREEN.getDyeDamage());
            Object[] in = { "g", "t", 'g', cactusGreen, 't', Blocks.REDSTONE_TORCH };
            GameRegistry.addRecipe(new ShapedOreRecipe(out, in));
        }

        if (BCCoreItems.paintbrush != null) {
            Object[] input = { " iw", " gi", "s  ", 's', "stickWood", 'g', "gearWood", 'w', new ItemStack(Blocks.WOOL, 1, 0), 'i', Items.STRING };
            ItemStack cleanPaintbrush = new ItemStack(BCCoreItems.paintbrush);
            GameRegistry.addRecipe(new ShapedOreRecipe(cleanPaintbrush, input));

            for (EnumDyeColor colour : EnumDyeColor.values()) {
                ItemPaintbrush_BC8.Brush brush = BCCoreItems.paintbrush.new Brush(colour);
                ItemStack out = brush.save(null);
                GameRegistry.addRecipe(new ShapelessOreRecipe(out, cleanPaintbrush, ColourUtil.getDyeName(colour)));
            }
        }

        String[] gears = { "wood", "stone", "iron", "gold", "diamond" };
        Object[] outers = { "stickWood", "cobblestone", "ingotIron", "ingotGold", "gemDiamond" };
        for (int i = 0; i < gears.length; i++) {
            String key = gears[i];
            Item gear = TagManager.getItem("item.gear." + key);
            if (gear == null) continue;
            Object inner = i == 0 ? null : TagManager.getTag("item.gear." + gears[i - 1], EnumTagType.OREDICT_NAME);
            Object outer = outers[i];
            Object[] arr;
            if (inner == null) {
                arr = new Object[] { " o ", "o o", " o ", 'o', outer };
            } else {
                arr = new Object[] { " o ", "oio", " o ", 'o', outer, 'i', inner };
            }
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(gear), arr));
        }
    }
}

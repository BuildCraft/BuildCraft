/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.utils;

import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

public final class ColorUtils {

    private static final int[] WOOL_TO_RGB = new int[] { 0xFFFFFF, 0xD87F33, 0xB24CD8, 0x6699D8, 0xE5E533, 0x7FCC19, 0xF27FA5, 0x4C4C4C, 0x999999,
        0x4C7F99, 0x7F3FB2, 0x334CB2, 0x664C33, 0x667F33, 0x993333, 0x191919 };

    private static final String[] WOOL_TO_NAME = new String[] { "white", "orange", "magenta", "light.blue", "yellow", "lime", "pink", "gray",
        "light.gray", "cyan", "purple", "blue", "brown", "green", "red", "black" };

    private static final String[] OREDICT_DYE_NAMES = new String[] { "dyeWhite", "dyeOrange", "dyeMagenta", "dyeLightBlue", "dyeYellow", "dyeLime",
        "dyePink", "dyeGray", "dyeLightGray", "dyeCyan", "dyePurple", "dyeBlue", "dyeBrown", "dyeGreen", "dyeRed", "dyeBlack" };

    private static final int[] LIGHT_HEX = { 0x181414, 0xBE2B27, 0x007F0E, 0x89502D, 0x253193, 0x7e34bf, 0x299799, 0xa0a7a7, 0x7A7A7A, 0xD97199,
        0x39D52E, 0xFFD91C, 0x66AAFF, 0xD943C6, 0xEA7835, 0xe4e4e4 };

    private static final int[] OREDICT_DYE_IDS = new int[16];

    private static final char[] WOOL_TO_CHAT = new char[] { 'f', '6', 'd', '9', 'e', 'a', 'd', '8', '7', '3', '5', '1', '6', '2', '4', '0' };

    private ColorUtils() {

    }

    public static void initialize() {
        for (int i = 0; i < 16; i++) {
            OREDICT_DYE_IDS[i] = OreDictionary.getOreID(OREDICT_DYE_NAMES[i]);
        }
    }

    public static EnumDyeColor getColorFromDye(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return null;
        if (stack.getItem() == Items.dye) {
            return EnumDyeColor.byDyeDamage(stack.getItemDamage());
        }

        int[] itemOreIDs = OreDictionary.getOreIDs(stack);
        for (int damage = 0; damage < 16; damage++) {
            for (int id : itemOreIDs) {
                if (OREDICT_DYE_IDS[damage] == id) {
                    return EnumDyeColor.byDyeDamage(damage);
                }
            }
        }

        return null;
    }

    public static boolean isDye(ItemStack stack) {
        return getColorFromDye(stack) != null;
    }

    public static int getRGBColor(int wool) {
        return WOOL_TO_RGB[wool & 15];
    }

    public static String getName(int wool) {
        return WOOL_TO_NAME[wool & 15];
    }

    public static String getOreDictionaryName(int wool) {
        return OREDICT_DYE_NAMES[wool & 15];
    }

    public static String getFormatting(int wool) {
        return "\u00a7" + WOOL_TO_CHAT[wool & 15];
    }

    public static String getFormattingTooltip(int wool) {
        return "\u00a7" + (WOOL_TO_CHAT[wool & 15] == '0' ? '8' : WOOL_TO_CHAT[wool & 15]);
    }

    public static int getLightHex(EnumDyeColor color) {
        return LIGHT_HEX[color.getMetadata()];
    }

    public static int convertBGRAtoRGBA(int argb) {
        int a = argb >>> 24;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) + (b << 16) + (g << 8) + r;
    }

    public static EnumDyeColor next(EnumDyeColor color) {
        int meta = color.getMetadata() + 1;
        return EnumDyeColor.byMetadata(meta & 15);
    }

    public static EnumDyeColor previous(EnumDyeColor color) {
        int meta = color.getMetadata() + 15;
        return EnumDyeColor.byMetadata(meta & 15);
    }

    public static String localize(EnumDyeColor color) {
        return StatCollector.translateToLocal(color.getUnlocalizedName());
    }
}

/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.silicon;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.BuildCraftSilicon;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.ModelHelper;

public class ItemRedstoneChipset extends ItemBuildCraft {

    public enum Chipset {

        RED,
        IRON,
        GOLD,
        DIAMOND,
        PULSATING,
        QUARTZ,
        COMP,
        EMERALD;
        public static final Chipset[] VALUES = values();
        private TextureAtlasSprite icon;

        public String getChipsetName() {
            return "redstone_" + name().toLowerCase(Locale.ENGLISH) + "_chipset";
        }

        public ItemStack getStack() {
            return getStack(1);
        }

        public ItemStack getStack(int qty) {
            return new ItemStack(BuildCraftSilicon.redstoneChipset, qty, ordinal());
        }

        public static Chipset fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= VALUES.length) {
                return RED;
            }
            return VALUES[ordinal];
        }
    }

    public ItemRedstoneChipset() {
        super();
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item." + Chipset.fromOrdinal(stack.getItemDamage()).getChipsetName();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List itemList) {
        for (Chipset chipset : Chipset.VALUES) {
            itemList.add(chipset.getStack());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (Chipset chipset : Chipset.values()) {
            ModelHelper.registerItemModel(this, chipset.ordinal(), "/" + chipset.name().toLowerCase(Locale.ROOT));
        }
    }

    public void registerItemStacks() {
        for (Chipset chipset : Chipset.VALUES) {
            OreDictionary.registerOre("chipset" + chipset.name().toUpperCase().substring(0, 1) + chipset.name().toLowerCase().substring(1), chipset
                    .getStack());
        }
    }
}

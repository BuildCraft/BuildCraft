package buildcraft.silicon.item;

import java.util.Locale;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.item.ItemBC_Neptune;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ItemRedstoneChipset extends ItemBC_Neptune {
    public ItemRedstoneChipset(String id) {
        super(id);
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (Type type : Type.values()) {
            addVariant(variants, type.ordinal(), type.getName());
        }
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        for (Type type : Type.values()) {
            subItems.add(new ItemStack(item, 1, type.ordinal()));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item.redstone_" + Type.values()[stack.getMetadata()].getName() + "_chipset";
    }

    public enum Type implements IStringSerializable {
        RED,
        IRON,
        GOLD,
        QUARTZ,
        DIAMOND;

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}

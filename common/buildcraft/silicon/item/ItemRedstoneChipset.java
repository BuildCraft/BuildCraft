package buildcraft.silicon.item;

import buildcraft.lib.item.ItemBC_Neptune;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Locale;

public class ItemRedstoneChipset extends ItemBC_Neptune {
    public ItemRedstoneChipset(String id) {
        super(id);
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for(Type type : Type.values()) {
            addVariant(variants, type.ordinal(), type.getName());
        }
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems) {
        for(Type type : Type.values()) {
            subItems.add(new ItemStack(item, 1, type.ordinal()));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item." + this.getUnlocalizedName() + Type.values()[stack.getMetadata()].getName();
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

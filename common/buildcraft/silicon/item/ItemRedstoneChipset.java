package buildcraft.silicon.item;

import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.items.IChipset;
import buildcraft.lib.item.ItemBC_Neptune;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemRedstoneChipset extends ItemBC_Neptune implements IChipset {
    public final EnumRedstoneChipset type;

    public ItemRedstoneChipset(String idBC, Item.Properties props, EnumRedstoneChipset type) {
        super(idBC, props);
        this.type = type;
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        for (EnumRedstoneChipset type : EnumRedstoneChipset.values()) {
//            addVariant(variants, type.ordinal(), type.getName());
//        }
//    }

    // 1.18.2: different item obj
//    @Override
//    public void addSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
//        for (EnumRedstoneChipset type : EnumRedstoneChipset.values()) {
//            subItems.add(new ItemStack(this, 1, type.ordinal()));
//        }
//    }


    @Override
//    public String getUnlocalizedName(ItemStack stack)
    public String getDescriptionId(ItemStack stack) {
//        return "item.redstone_" + EnumRedstoneChipset.values()[stack.getMetadata()].getName() + "_chipset";
        return "item.redstone_" + type.name().toLowerCase() + "_chipset.name";
    }

    // IChipset

    @Override
    public EnumRedstoneChipset getType() {
        return type;
    }
}

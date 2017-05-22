package buildcraft.transport.item;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.font.SpecialColourFontRenderer;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.ColourUtil;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ItemWire extends ItemBC_Neptune {
    public ItemWire(String id) {
        super(id);
        setHasSubtypes(true);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        for (int i = 0; i < 16; i++) {
            subItems.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (EnumDyeColor color : EnumDyeColor.values()) {
            addVariant(variants, color.getMetadata(), color.getName());
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return ColourUtil.getTextFullTooltipSpecial(EnumDyeColor.byMetadata(stack.getMetadata())) + " " + super.getItemStackDisplayName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public FontRenderer getFontRenderer(ItemStack stack) {
        return SpecialColourFontRenderer.INSTANCE;
    }
}

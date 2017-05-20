package buildcraft.transport.item;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.plug.PluggableLens;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ItemPluggableLens extends ItemBC_Neptune implements IItemPluggable {
    public ItemPluggableLens(String id) {
        super(id);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    public static LensData getData(ItemStack stack) {
        return new LensData(stack);
    }

    @Nonnull
    public ItemStack getStack(EnumDyeColor colour, boolean isFilter) {
        return getStack(new LensData(colour, isFilter));
    }

    @Nonnull
    public ItemStack getStack(LensData variant) {
        ItemStack stack = new ItemStack(this);
        variant.writeToStack(stack);
        return stack;
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, EnumFacing side, EntityPlayer player, EnumHand hand) {
        IPipe pipe = holder.getPipe();
        if (pipe == null || !(pipe.getFlow() instanceof IFlowItems)) {
            return null;
        }
        LensData data = getData(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), Blocks.STONE.getDefaultState());
        PluggableDefinition def = BCTransportPlugs.lens;
        return new PluggableLens(def, holder, side, data.colour, data.isFilter);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        LensData data = getData(stack);
        String colour = data.colour == null ? LocaleUtil.localize("color.clear") : ColourUtil.getTextFullTooltip(data.colour);
        String first = LocaleUtil.localize(data.isFilter ? "item.Filter.name" : "item.Lens.name");
        return colour + " " + first;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        for (int i = 0; i < 34; i++) {
            subItems.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (int i = 0; i < 34; i++) {
            variants.put(i, new ModelResourceLocation("buildcrafttransport:lens_item#inventory"));
        }
    }

    public static class LensData {
        public final EnumDyeColor colour;
        public final boolean isFilter;

        public LensData(EnumDyeColor colour, boolean isFilter) {
            this.colour = colour;
            this.isFilter = isFilter;
        }

        public LensData(ItemStack stack) {
            this(stack.getItemDamage());
        }

        public LensData(int damage) {
            if (damage >= 32) {
                colour = null;
                isFilter = damage == 33;
            } else {
                colour = EnumDyeColor.byDyeDamage(damage & 15);
                isFilter = damage >= 16;
            }
        }

        public int getItemDamage() {
            if (colour == null) {
                return isFilter ? 33 : 32;
            } else {
                return colour.getDyeDamage() + (isFilter ? 16 : 0);
            }
        }

        public ItemStack writeToStack(ItemStack stack) {
            stack.setItemDamage(getItemDamage());
            return stack;
        }
    }
}

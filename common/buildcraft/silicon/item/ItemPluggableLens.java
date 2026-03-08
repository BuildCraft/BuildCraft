/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.item;

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
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.plug.PluggableLens;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;

public class ItemPluggableLens extends ItemBC_Neptune implements IItemPluggable {
    public ItemPluggableLens(String idBC, Item.Properties properties) {
        super(idBC, properties);
//        setMaxDamage(0);
//        setHasSubtypes(true);
    }

    public static LensData getData(ItemStack stack) {
        return new LensData(stack);
    }

    @Nonnull
    public ItemStack getStack(DyeColor colour, boolean isFilter) {
        return getStack(new LensData(colour, isFilter));
    }

    @Nonnull
    public ItemStack getStack(LensData variant) {
        ItemStack stack = new ItemStack(this);
        variant.writeToStack(stack);
        return stack;
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, PlayerEntity player,
                                 Hand hand) {
        IPipe pipe = holder.getPipe();
        if (pipe == null || !(pipe.getFlow() instanceof IFlowItems)) {
            return null;
        }
        LensData data = getData(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), Blocks.STONE.defaultBlockState());
        PluggableDefinition def = BCSiliconPlugs.lens;
        return new PluggableLens(def, holder, side, data.colour, data.isFilter);
    }

    @Override
//    public String getItemStackDisplayName(ItemStack stack)
    public ITextComponent getName(ItemStack stack) {
        // Calen: if using TranslationTextComponent to localize colours, the colour will not be seen, unknown why...
        LensData data = getData(stack);
        String colour = data.colour == null ? LocaleUtil.localize("color.clear")
                : ColourUtil.getTextFullTooltipSpecial(data.colour);
        String first = LocaleUtil.localize(data.isFilter ? "item.Filter.name" : "item.Lens.name");
//        return colour + " " + first;
        return new StringTextComponent(colour + " " + first);
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public FontRenderer getFontRenderer(ItemStack stack) {
//        return SpecialColourFontRenderer.INSTANCE;
//    }

    @Override
    protected void addSubItems(ItemGroup tab, NonNullList<ItemStack> subItems) {
        for (int i = 0; i < 34; i++) {
//            ItemStack stack = new ItemStack(this, 1, i);
            ItemStack stack = new ItemStack(this, 1);
            stack.setDamageValue(i);
            subItems.add(stack);
        }
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        for (int i = 0; i < 34; i++) {
//            variants.put(i, new ModelResourceLocation("buildcraftsilicon:lens_item#inventory"));
//        }
//    }

    public static class LensData {
        public final DyeColor colour;
        public final boolean isFilter;

        public LensData(DyeColor colour, boolean isFilter) {
            this.colour = colour;
            this.isFilter = isFilter;
        }

        public LensData(ItemStack stack) {
            this(stack.getDamageValue());
        }

        public LensData(int damage) {
            if (damage >= 32) {
                colour = null;
                isFilter = damage == 33;
            } else {
//                colour = EnumDyeColor.byDyeDamage(damage & 15);
                colour = DyeColor.byId(15 - damage & 15);
                isFilter = damage >= 16;
            }
        }

        public int getItemDamage() {
            if (colour == null) {
                return isFilter ? 33 : 32;
            } else {
//                return colour.getDyeDamage() + (isFilter ? 16 : 0);
                return (15 - colour.getId()) + (isFilter ? 16 : 0);
            }
        }

        public ItemStack writeToStack(ItemStack stack) {
            stack.setDamageValue(getItemDamage());
            return stack;
        }
    }
}

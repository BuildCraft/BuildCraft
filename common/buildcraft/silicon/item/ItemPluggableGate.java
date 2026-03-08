/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.item;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.plug.PluggableGate;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemPluggableGate extends ItemBC_Neptune implements IItemPluggable {
    private final GateVariant VARIANT;

    // public ItemPluggableGate(String idBC, Item.Properties properties)
    public ItemPluggableGate(String idBC, Item.Properties properties, GateVariant variant) {
        super(idBC, properties);
        this.VARIANT = variant;
    }

    public static GateVariant getVariant(@Nonnull ItemStack stack) {
//        return new GateVariant(NBTUtilBC.getItemData(stack).getCompound("gate"));
        return ((ItemPluggableGate) stack.getItem()).VARIANT;
    }

    @Nonnull
//    public ItemStack getStack(GateVariant variant)
    public static ItemStack getStack(GateVariant variant) {
        RegistryObject<ItemPluggableGate> item = BCSiliconItems.variantGateMap.get(variant);
        if (item == null) {
            return StackUtil.EMPTY;
        }
        ItemStack stack = new ItemStack(item.get());
//        NBTUtilBC.getItemData(stack).put("gate", variant.writeToNBT());
        return stack;
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, PlayerEntity player, Hand hand) {
//        GateVariant variant = getVariant(stack);
        GateVariant variant = this.VARIANT;
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), variant.material.block.defaultBlockState());
        PluggableDefinition def = BCSiliconPlugs.gate;
        return new PluggableGate(def, holder, side, variant);
    }

    @Override
//    public String getItemStackDisplayName(ItemStack stack)
    public ITextComponent getName(ItemStack stack) {
//        return new StringTextComponent(getVariant(StackUtil.asNonNull(stack)).getLocalizedName());
//        return getVariant(StackUtil.asNonNull(stack)).getLocalizedName();
        return this.VARIANT.getLocalizedName();
    }


    @Override
    @OnlyIn(Dist.CLIENT)
//    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag)
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
//        GateVariant variant = getVariant(StackUtil.asNonNull(stack));
        GateVariant variant = this.VARIANT;

//        tooltip.add(LocaleUtil.localize("gate.slots", variant.numSlots));
        tooltip.add(new TranslationTextComponent("gate.slots", variant.numSlots));

        if (variant.numTriggerArgs == variant.numActionArgs) {
            if (variant.numTriggerArgs > 0) {
//                tooltip.add(LocaleUtil.localize("gate.params", variant.numTriggerArgs));
                tooltip.add(new TranslationTextComponent("gate.params", variant.numTriggerArgs));
            }
        } else {
            if (variant.numTriggerArgs > 0) {
//                tooltip.add(LocaleUtil.localize("gate.params.trigger", variant.numTriggerArgs));
                tooltip.add(new TranslationTextComponent("gate.params.trigger", variant.numTriggerArgs));
            }
            if (variant.numActionArgs > 0) {
//                tooltip.add(LocaleUtil.localize("gate.params.action", variant.numTriggerArgs));
                tooltip.add(new TranslationTextComponent("gate.params.action", variant.numTriggerArgs));
            }
        }
    }

    // 1.18.2: different item obj
//    @Override
//    protected void addSubItems(ItemGroup tab, NonNullList<ItemStack> subItems) {
//        subItems.add(new ItemStack(this));
//        for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
//            if (!material.canBeModified) {
//                continue;
//            }
//            for (EnumGateLogic logic : EnumGateLogic.VALUES) {
//                for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
//                    subItems.add(getStack(new GateVariant(logic, material, modifier)));
//                }
//            }
//        }
//    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        variants.put(0, new ModelResourceLocation("buildcraftsilicon:gate_item#inventory"));
//    }
}

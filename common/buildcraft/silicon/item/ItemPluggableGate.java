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
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;

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
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
//        GateVariant variant = getVariant(stack);
        GateVariant variant = this.VARIANT;
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), variant.material.block.defaultBlockState());
        PluggableDefinition def = BCSiliconPlugs.gate;
        return new PluggableGate(def, holder, side, variant);
    }

    @Override
//    public String getItemStackDisplayName(ItemStack stack)
    public Component getName(ItemStack stack) {
//        return Component.literal(getVariant(StackUtil.asNonNull(stack)).getLocalizedName());
//        return getVariant(StackUtil.asNonNull(stack)).getLocalizedName();
        return this.VARIANT.getLocalizedName();
    }


    @Override
    @OnlyIn(Dist.CLIENT)
//    public void addInformation(ItemStack stack, Level world, List<String> tooltip, ITooltipFlag flag)
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level world, List<Component> tooltip, TooltipFlag flag) {
//        GateVariant variant = getVariant(StackUtil.asNonNull(stack));
        GateVariant variant = this.VARIANT;

//        tooltip.add(LocaleUtil.localize("gate.slots", variant.numSlots));
        tooltip.add(Component.translatable("gate.slots", variant.numSlots));

        if (variant.numTriggerArgs == variant.numActionArgs) {
            if (variant.numTriggerArgs > 0) {
//                tooltip.add(LocaleUtil.localize("gate.params", variant.numTriggerArgs));
                tooltip.add(Component.translatable("gate.params", variant.numTriggerArgs));
            }
        } else {
            if (variant.numTriggerArgs > 0) {
//                tooltip.add(LocaleUtil.localize("gate.params.trigger", variant.numTriggerArgs));
                tooltip.add(Component.translatable("gate.params.trigger", variant.numTriggerArgs));
            }
            if (variant.numActionArgs > 0) {
//                tooltip.add(LocaleUtil.localize("gate.params.action", variant.numTriggerArgs));
                tooltip.add(Component.translatable("gate.params.action", variant.numTriggerArgs));
            }
        }
    }

    // 1.18.2: different item obj
//    @Override
//    protected void addSubItems(CreativeModeTab tab, NonNullList<ItemStack> subItems) {
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

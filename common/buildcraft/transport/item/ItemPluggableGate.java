/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import java.util.List;

import javax.annotation.Nonnull;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.gate.EnumGateLogic;
import buildcraft.transport.gate.EnumGateMaterial;
import buildcraft.transport.gate.EnumGateModifier;
import buildcraft.transport.gate.GateVariant;
import buildcraft.transport.plug.PluggableGate;

public class ItemPluggableGate extends ItemBC_Neptune implements IItemPluggable {
    public ItemPluggableGate(String id) {
        super(id);
    }

    public static GateVariant getVariant(@Nonnull ItemStack stack) {
        return new GateVariant(NBTUtilBC.getItemData(stack).getCompoundTag("gate"));
    }

    @Nonnull
    public ItemStack getStack(GateVariant variant) {
        ItemStack stack = new ItemStack(this);
        NBTUtilBC.getItemData(stack).setTag("gate", variant.writeToNBT());
        return stack;
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, EnumFacing side, EntityPlayer player, EnumHand hand) {
        GateVariant variant = getVariant(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), variant.material.block.getDefaultState());
        PluggableDefinition def = BCTransportPlugs.gate;
        return new PluggableGate(def, holder, side, variant);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return getVariant(StackUtil.asNonNull(stack)).getLocalizedName();
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        GateVariant variant = getVariant(StackUtil.asNonNull(stack));

        tooltip.add(LocaleUtil.localize("gate.slots", variant.numSlots));

        if (variant.numTriggerArgs == variant.numActionArgs) {
            if (variant.numTriggerArgs > 0) {
                tooltip.add(LocaleUtil.localize("gate.params", variant.numTriggerArgs));
            }
        } else {
            if (variant.numTriggerArgs > 0) {
                tooltip.add(LocaleUtil.localize("gate.params.trigger", variant.numTriggerArgs));
            }
            if (variant.numActionArgs > 0) {
                tooltip.add(LocaleUtil.localize("gate.params.action", variant.numTriggerArgs));
            }
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        subItems.add(new ItemStack(this));
        for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
            if (!material.canBeModified) {
                continue;
            }
            for (EnumGateLogic logic : EnumGateLogic.VALUES) {
                for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
                    subItems.add(getStack(new GateVariant(logic, material, modifier)));
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        variants.put(0, new ModelResourceLocation("buildcrafttransport:gate_item#inventory"));
    }
}

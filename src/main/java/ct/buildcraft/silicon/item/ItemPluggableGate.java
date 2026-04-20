/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.item;

import java.util.List;

import javax.annotation.Nonnull;

import ct.buildcraft.api.transport.IItemPluggable;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.SoundUtil;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.silicon.BCSilicon;
import ct.buildcraft.silicon.BCSiliconPlugs;
import ct.buildcraft.silicon.gate.EnumGateLogic;
import ct.buildcraft.silicon.gate.EnumGateMaterial;
import ct.buildcraft.silicon.gate.EnumGateModifier;
import ct.buildcraft.silicon.gate.GateVariant;
import ct.buildcraft.silicon.plug.PluggableGate;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemPluggableGate extends Item implements IItemPluggable {
    public ItemPluggableGate() {
        super(new Item.Properties().tab(BCSilicon.tabPlugs));
    }

    public static GateVariant getVariant(@Nonnull ItemStack stack) {
        return new GateVariant(NBTUtilBC.getItemData(stack).getCompound("gate"));
    }

    @Nonnull
    public ItemStack getStack(GateVariant variant) {
        ItemStack stack = new ItemStack(this);
        NBTUtilBC.getItemData(stack).put("gate", variant.writeToNBT());
        return stack;
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
        GateVariant variant = getVariant(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), variant.material.block.defaultBlockState());
        PluggableDefinition def = BCSiliconPlugs.gate;
        return new PluggableGate(def, holder, side, variant);
    }

    
    @Override
	public String getDescriptionId(ItemStack stack) {
    	return getName(stack).getString();//TODO
	}
    
    @Override
	public Component getName(ItemStack stack) {
		return getVariant(StackUtil.asNonNull(stack)).getLocalizedName();
	}

	@Override
    @OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
    	 GateVariant variant = getVariant(StackUtil.asNonNull(stack));

         tooltip.add(Component.translatable("gate.slots", variant.numSlots));

         if (variant.numTriggerArgs == variant.numActionArgs) {
             if (variant.numTriggerArgs > 0) {
                 tooltip.add(Component.translatable("gate.params", variant.numTriggerArgs));
             }
         } else {
             if (variant.numTriggerArgs > 0) {
                 tooltip.add(Component.translatable("gate.params.trigger", variant.numTriggerArgs));
             }
             if (variant.numActionArgs > 0) {
                 tooltip.add(Component.translatable("gate.params.action", variant.numTriggerArgs));
             }
         }
	}
    
    @Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems) {
    	if(!allowedIn(tab))
    		return;
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


/*    @Override
    @OnlyIn(Dist.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        variants.put(0, new ModelResourceLocation("buildcraftsilicon:gate_item#inventory"));
    }*/
}

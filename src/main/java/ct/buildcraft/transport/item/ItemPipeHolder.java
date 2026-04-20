/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.item;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


import ct.buildcraft.api.transport.pipe.IItemPipe;
import ct.buildcraft.api.transport.pipe.PipeApi;
import ct.buildcraft.api.transport.pipe.PipeDefinition;
import ct.buildcraft.transport.BCTransport;
import ct.buildcraft.transport.BCTransportBlocks;
import ct.buildcraft.lib.misc.LocaleUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class ItemPipeHolder extends BlockItem implements IItemPipe {
    public final PipeDefinition definition;
//    private final String id;
    private String unlocalizedName;

    public ItemPipeHolder(PipeDefinition definition) {
        super(BCTransportBlocks.pipeHolder.get(), new Item.Properties().tab(BCTransport.tabPipes));
        this.definition = definition;
        this.unlocalizedName = definition.identifier.toLanguageKey("pipe");
    }



    @Override
    public PipeDefinition getDefinition() {
        return definition;
    }
    
    // BlockItem overrides these to point to the block

	@Override
	public UseAnim getUseAnimation(ItemStack p_41452_) {
		// TODO Auto-generated method stub
		return super.getUseAnimation(p_41452_);
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
	      if (this.allowedIn(tab)) {
	          list.add(new ItemStack(this));
	       }
	}

	@Override
    public Collection<CreativeModeTab> getCreativeTabs() {
        return Collections.singletonList(BCTransport.tabPipes);
    }
	
	

    // Misc usefulness

		@Override
	public Component getName(ItemStack p_41458_) {
		return Component.translatable(unlocalizedName);
	}
		
	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        String tipName = "tip." + unlocalizedName;
        if (I18n.exists(tipName)) {
            tooltip.add(Component.translatable(tipName).setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY)));
        }
        if (definition.flowType == PipeApi.flowFluids) {
            PipeApi.FluidTransferInfo fti = PipeApi.getFluidTransferInfo(definition);
            tooltip.add(LocaleUtil.localizeFluidFlow(fti.transferPerTick));
        } else if (definition.flowType == PipeApi.flowPower) {
            PipeApi.PowerTransferInfo pti = PipeApi.getPowerTransferInfo(definition);
            tooltip.add(LocaleUtil.localizeMjFlow(pti.transferPerTick));
        }
		super.appendHoverText(stack, world, tooltip, flag);
	}
	
	




	
}

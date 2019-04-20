/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.LocaleUtil;

public class ItemBlockBC_Neptune extends ItemBlock implements IItemBuildCraft {
    public final String id;

    public ItemBlockBC_Neptune(BlockBCBase_Neptune block) {
        super(block);
        this.id = "item." + block.id;
        init();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flags) {
        super.addInformation(stack, world, tooltip, flags);
        String tipId = getUnlocalizedName(stack) + ".tip";
        if (LocaleUtil.canLocalize(tipId)) {
            tooltip.add(TextFormatting.GRAY + LocaleUtil.localize(tipId));
        } else if (flags.isAdvanced()) {
            tooltip.add(TextFormatting.GRAY + tipId);
        }
    }
}

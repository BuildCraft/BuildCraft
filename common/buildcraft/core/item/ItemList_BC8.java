/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import java.util.List;

import javax.annotation.Nonnull;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.items.IList;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;

import buildcraft.core.BCCoreGuis;

public class ItemList_BC8 extends ItemBC_Neptune implements IList {
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftcore:list");
    public ItemList_BC8(String id) {
        super(id);
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
        BCCoreGuis.LIST.openGUI(player);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, 0, "clean");
        addVariant(variants, 1, "used");
    }

    @Override
    public int getMetadata(ItemStack stack) {
        return ListHandler.hasItems(StackUtil.asNonNull(stack)) ? 1 : 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        String name = getName(StackUtil.asNonNull(stack));
        if (StringUtils.isNullOrEmpty(name)) return;
        tooltip.add(TextFormatting.ITALIC + name);
    }

    // IList

    @Override
    public String getName(@Nonnull ItemStack stack) {
        return NBTUtilBC.getItemData(stack).getString("label");
    }

    @Override
    public boolean setName(@Nonnull ItemStack stack, String name) {
        NBTUtilBC.getItemData(stack).setString("label", name);
        return true;
    }

    @Override
    public boolean matches(@Nonnull ItemStack stackList, @Nonnull ItemStack item) {
        return ListHandler.matches(stackList, item);
    }
}

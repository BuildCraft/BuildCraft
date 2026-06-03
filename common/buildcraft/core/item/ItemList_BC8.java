/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// TODO(R.Chen): blocked by BCCoreGuis (not yet migrated to Fabric 1.20.1)
package buildcraft.core.item;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import java.util.HashMap;

import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.items.IList;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;

import buildcraft.core.BCCoreGuis;

public class ItemList_BC8 extends ItemBC_Neptune implements IList {
    private static final Identifier ADVANCEMENT = new Identifier("buildcraftcore:list");
    public ItemList_BC8(String id) {
        super(id);
        setMaxStackSize(1);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public TypedActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
        BCCoreGuis.LIST.openGUI(player);
        return new ActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    @Environment(EnvType.CLIENT)
    public void addModelVariants(HashMap<Integer, ModelIdentifier> variants) {
        addVariant(variants, 0, "clean");
        addVariant(variants, 1, "used");
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getMetadata(ItemStack stack) {
        return ListHandler.hasItems(StackUtil.asNonNull(stack)) ? 1 : 0;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    @Environment(EnvType.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, TooltipContext flag) {
        String name = getName(StackUtil.asNonNull(stack));
        if (StringUtils.isNullOrEmpty(name)) return;
        tooltip.add(Formatting.ITALIC + name);
    }

    // IList

    @Override
    public String getName(@Nonnull ItemStack stack) {
        return NBTUtilBC.getItemData(stack).getString("label");
    }

    @Override
    public boolean setName(@Nonnull ItemStack stack, String name) {
        NBTUtilBC.getItemData(stack).putString("label", name);
        return true;
    }

    @Override
    public boolean matches(@Nonnull ItemStack stackList, @Nonnull ItemStack item) {
        return ListHandler.matches(stackList, item);
    }
}

/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import buildcraft.api.items.IList;
import buildcraft.core.BCCoreItems;
import buildcraft.core.BCCoreMenuTypes;
import buildcraft.core.list.ContainerList;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

//public class ItemList_BC8 extends ItemBC_Neptune implements IList
public class ItemList_BC8 extends ItemBC_Neptune implements IList, INamedContainerProvider {
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftcore:list");

    // Calen
    public static final String NBT_KEY = "label";

    public ItemList_BC8(String idBC, Item.Properties properties) {
        super(idBC, properties);
//        setMaxStackSize(1);
    }

    @Override
//    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, EnumHand hand)
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
//        BCCoreGuis.LIST.openGUI(player);
        MessageUtil.serverOpenItemGui(player, BCCoreItems.list.get());
//        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        return new ActionResult<>(ActionResultType.SUCCESS, player.getItemInHand(hand));
    }

//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        addVariant(variants, 0, "clean");
//        addVariant(variants, 1, "used");
//    }

//    @Override
//    public int getMetadata(ItemStack stack) {
//        return ListHandler.hasItems(StackUtil.asNonNull(stack)) ? 1 : 0;
//    }

    @Override
    @OnlyIn(Dist.CLIENT)
//    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag)
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        String name = getName_INamedItem(StackUtil.asNonNull(stack));
//        if (StringUtils.isNullOrEmpty(name)) return;
        if (StringUtils.isNullOrEmpty(name)) return;
//        tooltip.add(TextFormatting.ITALIC + name);
        tooltip.add(new StringTextComponent(TextFormatting.ITALIC + name));
    }

    // IList

    @Override
//    public ITextComponent getName(@Nonnull ItemStack stack)
    public String getName_INamedItem(@Nonnull ItemStack stack) {
//        return new StringTextComponent(NBTUtilBC.getItemData(stack).getString("label"));
//        return new StringTextComponent(NBTUtilBC.getItemData(stack).getString(NBT_KEY));
        return NBTUtilBC.getItemData(stack).getString(NBT_KEY);
    }

    @Override
    public boolean setName(@Nonnull ItemStack stack, String name) {
//        NBTUtilBC.getItemData(stack).putString("label", name);
        NBTUtilBC.getItemData(stack).putString(NBT_KEY, name);
        return true;
    }

    @Override
    public boolean matches(@Nonnull ItemStack stackList, @Nonnull ItemStack item) {
        return ListHandler.matches(stackList, item);
    }

    // Calen
    public static boolean isUsed(ItemStack stack) {
        return ListHandler.hasItems(StackUtil.asNonNull(stack));
    }

    // INamedContainerProvider

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("list");
    }

    @Nullable
    @Override
    public ContainerList createMenu(int id, PlayerInventory inv, PlayerEntity player) {
        return new ContainerList(BCCoreMenuTypes.LIST, id, player);
    }
}

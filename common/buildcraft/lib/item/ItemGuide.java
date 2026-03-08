/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import buildcraft.lib.BCLibMenuTypes;
import buildcraft.lib.container.ContainerGuide;
import buildcraft.lib.guide.GuideBook;
import buildcraft.lib.guide.GuideBookRegistry;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class ItemGuide extends ItemBC_Neptune implements INamedContainerProvider {
    private static final String DEFAULT_BOOK = "buildcraftcore:main";
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftcore:guide");
    private static final String TAG_BOOK_NAME = "BookName";

    public ItemGuide(String idBC, Item.Properties properties) {
        super(idBC, properties);
//        setContainerItem(this);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
//        player.openGui(BCLib.INSTANCE, 0, world, hand == EnumHand.MAIN_HAND ? 0 : 1, 0, 0);
        MessageUtil.serverOpenItemGui(player, this);
        return new ActionResult<>(ActionResultType.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    protected void addSubItems(ItemGroup tab, NonNullList<ItemStack> items) {
        for (GuideBook book : GuideBookRegistry.INSTANCE.getAllEntries()) {
            ItemStack stack = new ItemStack(this);
            if (!book.name.toString().equals(ItemGuide.DEFAULT_BOOK)) {
                setBookName(stack, book.name.toString());
            }
            items.add(stack);
        }
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        String bookName = getBookName(stack);
        GuideBook book = GuideBookRegistry.INSTANCE.getBook(bookName);
        if (book != null) {
            return book.title;
        }
        return super.getName(stack);
    }

    public static String getBookName(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt == null || !nbt.contains(TAG_BOOK_NAME, Constants.NBT.TAG_STRING)) {
            // So that existing guide books continue to work
            return ItemGuide.DEFAULT_BOOK;
        }
        return nbt.getString(TAG_BOOK_NAME);
    }

    public static void setBookName(ItemStack stack, String book) {
        CompoundNBT nbt = NBTUtilBC.getItemData(stack);
        nbt.putString(TAG_BOOK_NAME, book);
    }

    // INamedContainerProvider

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(ItemGuide.DEFAULT_BOOK);
    }

    @Nullable
    @Override
    public ContainerGuide createMenu(int id, PlayerInventory p_39955_, PlayerEntity player) {
        return new ContainerGuide(BCLibMenuTypes.GUIDE, id);
    }
}

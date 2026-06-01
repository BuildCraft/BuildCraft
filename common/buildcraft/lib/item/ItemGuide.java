/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import java.util.List;

import net.minecraftforge.common.util.Constants;

import buildcraft.lib.BCLib;
import buildcraft.lib.guide.GuideBook;
import buildcraft.lib.guide.GuideBookRegistry;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.nbt.NbtElement;

public class ItemGuide extends ItemBC_Neptune {
    private static final String DEFAULT_BOOK = "buildcraftcore:main";
    private static final Identifier ADVANCEMENT = new Identifier("buildcraftcore:guide");
    private static final String TAG_BOOK_NAME = "BookName";

    public ItemGuide(String id) {
        super(id);
        setContainerItem(this);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public TypedActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
        player.openGui(BCLib.INSTANCE, 0, world, hand == Hand.MAIN_HAND ? 0 : 1, 0, 0);
        return new ActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    protected void addSubItems(ItemGroup tab, DefaultedList<ItemStack> items) {
        for (GuideBook book : GuideBookRegistry.INSTANCE.getAllEntries()) {
            ItemStack stack = new ItemStack(this);
            if (!book.name.toString().equals(ItemGuide.DEFAULT_BOOK)) {
                setBookName(stack, book.name.toString());
            }
            items.add(stack);
        }
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String getItemStackDisplayName(ItemStack stack) {
        String bookName = getBookName(stack);
        GuideBook book = GuideBookRegistry.INSTANCE.getBook(bookName);
        if (book != null) {
            return book.title.getFormattedText();
        }
        return super.getItemStackDisplayName(stack);
    }

    public static String getBookName(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(TAG_BOOK_NAME, NbtElement.STRING_TYPE)) {
            // So that existing guide books continue to work
            return ItemGuide.DEFAULT_BOOK;
        }
        return nbt.getString(TAG_BOOK_NAME);
    }

    public static void setBookName(ItemStack stack, String book) {
        NbtCompound nbt = NBTUtilBC.getItemData(stack);
        nbt.putString(TAG_BOOK_NAME, book);
    }
}

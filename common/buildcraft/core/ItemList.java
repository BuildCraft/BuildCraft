/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftCore;
import buildcraft.api.items.IList;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.ModelHelper;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.list.ListHandler;

public class ItemList extends ItemBuildCraft implements IList {
    public ItemList() {
        super();
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            player.openGui(BuildCraftCore.instance, GuiIds.LIST_NEW, world, 0, 0, 0);
        }

        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
        NBTTagCompound nbt = NBTUtils.getItemData(stack);
        if (nbt.hasKey("label")) {
            list.add(nbt.getString("label"));
        }
    }

    public static void saveLabel(ItemStack stack, String text) {
        NBTTagCompound nbt = NBTUtils.getItemData(stack);

        nbt.setString("label", text);
    }

    @Override
    public boolean setName(ItemStack stack, String name) {
        saveLabel(stack, name);
        return true;
    }

    @Override
    public String getName(ItemStack stack) {
        return getLabel(stack);
    }

    @Override
    public String getLabel(ItemStack stack) {
        return NBTUtils.getItemData(stack).getString("label");
    }

    @Override
    public boolean matches(ItemStack stackList, ItemStack item) {
        return ListHandler.matches(stackList, item);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> itemList) {
        itemList.add(new ItemStack(this, 1, 0));
    }

    @Override
    public void registerModels() {
        ModelHelper.registerItemModel(this, 0, "_clean");
        ModelHelper.registerItemModel(this, 1, "_used");
    }
}

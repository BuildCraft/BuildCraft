/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.BuildCraftRobotics;

public class ItemRedstoneBoard extends ItemBuildCraft {
    public ItemRedstoneBoard() {
        super(BCCreativeTab.get("boards"));
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return getBoardNBT(stack) != RedstoneBoardRegistry.instance.getEmptyRobotBoard() ? 1 : 16;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String start = super.getItemStackDisplayName(stack);
        RedstoneBoardNBT<?> board = getBoardNBT(stack);
        return start + " (" + board.getDisplayName() + ")";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        RedstoneBoardNBT<?> board = getBoardNBT(stack);
        board.addInformation(stack, player, list, advanced);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List itemList) {
        itemList.add(createStack(RedstoneBoardRegistry.instance.getEmptyRobotBoard()));
        for (RedstoneBoardNBT<?> boardNBT : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
            itemList.add(createStack(boardNBT));
        }
    }

    public static ItemStack createStack(RedstoneBoardNBT<?> boardNBT) {
        ItemStack stack = new ItemStack(BuildCraftRobotics.redstoneBoard);
        NBTTagCompound nbtData = NBTUtils.getItemData(stack);
        boardNBT.createBoard(nbtData);
        return stack;
    }

    public static RedstoneBoardNBT<?> getBoardNBT(ItemStack stack) {
        return getBoardNBT(getNBT(stack));
    }

    private static NBTTagCompound getNBT(ItemStack stack) {
        NBTTagCompound cpt = NBTUtils.getItemData(stack);
        if (!cpt.hasKey("id")) {
            RedstoneBoardRegistry.instance.getEmptyRobotBoard().createBoard(cpt);
        }
        return cpt;
    }

    private static RedstoneBoardNBT<?> getBoardNBT(NBTTagCompound cpt) {
        return RedstoneBoardRegistry.instance.getRedstoneBoard(cpt);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels() {
        for (RedstoneBoardNBT<?> boardNBT : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
            String type = boardNBT.getItemModelLocation();
            /* Neat little trick: we have to register the models, but NEVER for meta 0 (because of the way minecraft
             * gets its item models). So, provided this number is never 0 it will work */
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(this, 1, new ModelResourceLocation(type, "inventory"));
            ModelBakery.addVariantName(this, type);
        }
    }
}

/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.lib.misc.NBTUtilBC;

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

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
        RedstoneBoardNBT<?> board = getBoardNBT(stack);
        board.addInformation(stack, player, list, advanced);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List<ItemStack> itemList) {
        itemList.add(createStack(RedstoneBoardRegistry.instance.getEmptyRobotBoard()));
        for (RedstoneBoardNBT<?> boardNBT : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
            itemList.add(createStack(boardNBT));
        }
    }

    public static ItemStack createStack(RedstoneBoardNBT<?> boardNBT) {
        ItemStack stack = new ItemStack(BuildCraftRobotics.redstoneBoard);
        NBTTagCompound nbtData = NBTUtilBC.getItemData(stack);
        boardNBT.createBoard(nbtData);
        return stack;
    }

    public static RedstoneBoardNBT<?> getBoardNBT(ItemStack stack) {
        return getBoardNBT(getNBT(stack));
    }

    private static NBTTagCompound getNBT(ItemStack stack) {
        NBTTagCompound cpt = NBTUtilBC.getItemData(stack);
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
        List<RedstoneBoardNBT<?>> boardNBTs = Lists.newArrayList(RedstoneBoardRegistry.instance.getAllBoardNBTs());
        boardNBTs.add(RedstoneBoardRegistry.instance.getEmptyRobotBoard());
        for (RedstoneBoardNBT<?> boardNBT : boardNBTs) {
            String type = boardNBT.getItemModelLocation();
            /* Neat little trick: we have to register the models, but NEVER for meta 0 (because of the way minecraft
             * gets its item models). So, provided this number is never 0 it will work */
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(this, 1, new ModelResourceLocation(type, "inventory"));
            ModelBakery.addVariantName(this, type);
        }
    }
}

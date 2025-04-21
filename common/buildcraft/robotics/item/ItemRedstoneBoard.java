/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.item;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.lib.item.ItemBC_Neptune;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRedstoneBoard extends ItemBC_Neptune {
    private final RedstoneBoardNBT<?> boardNBT;

    public ItemRedstoneBoard(String idBC, Properties properties, RedstoneBoardNBT<?> boardNBT) {
        // super(BCCreativeTab.get("boards"));
        super(idBC, properties);
        this.boardNBT = boardNBT;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return getBoardNBT(stack) != RedstoneBoardRegistry.instance.getEmptyRobotBoard() ? 1 : 16;
    }

    @Override
    public Component getName(ItemStack stack) {
        MutableComponent start = (MutableComponent) super.getName(stack);
        RedstoneBoardNBT<?> board = getBoardNBT(stack);
//        return start + " (" + board.getDisplayName() + ")";
        return start.append(" (" + board.getDisplayName() + ")");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        RedstoneBoardNBT<?> board = getBoardNBT(stack);
        board.addInformation(stack, world, list, flag);
    }

    // Calen 1.18.2: created independent items
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    @Override
////    @OnlyIn(Dist.CLIENT)
//    // public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List itemList)
//    public void addSubItems(CreativeModeTab tab, NonNullList<ItemStack> itemList) {
//        itemList.add(createStack(RedstoneBoardRegistry.instance.getEmptyRobotBoard()));
//        for (RedstoneBoardNBT<?> boardNBT : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
//            itemList.add(createStack(boardNBT));
//        }
//    }

//    public static ItemStack createStack(RedstoneBoardNBT<?> boardNBT) {
////        ItemStack stack = new ItemStack(BCRoboticsItems.redstoneBoard.get());
////        CompoundTag nbtData = NBTUtilBC.getItemData(stack);
////        boardNBT.createBoard(nbtData);
//        ItemStack stack = new ItemStack(RedstoneBoardRegistry.instance.getBoardNBTItemMap().get(boardNBT).get());
//        return stack;
//    }

    public static RedstoneBoardNBT<?> getBoardNBT(ItemStack stack) {
        // return getBoardNBT(getNBT(stack));
        if (stack.getItem() instanceof ItemRedstoneBoard) {
            return ((ItemRedstoneBoard) stack.getItem()).boardNBT;
        } else {
            return RedstoneBoardRegistry.instance.getEmptyRobotBoard();
        }
    }

//    private static CompoundTag getNBT(ItemStack stack) {
//        CompoundTag cpt = NBTUtilBC.getItemData(stack);
//        if (!cpt.contains("id")) {
//            RedstoneBoardRegistry.instance.getEmptyRobotBoard().createBoard(cpt);
//        }
//        return cpt;
//    }

//    private static RedstoneBoardNBT<?> getBoardNBT(CompoundTag cpt) {
//        return RedstoneBoardRegistry.instance.getRedstoneBoard(cpt);
//    }

    // Calen 1.18.2
    public RedstoneBoardNBT<?> getBoardNBT() {
        return boardNBT;
    }

    //    @OnlyIn(Dist.CLIENT)
//    @Override
//    public void registerModels() {
//        List<RedstoneBoardNBT<?>> boardNBTs = Lists.newArrayList(RedstoneBoardRegistry.instance.getAllBoardNBTs());
//        boardNBTs.add(RedstoneBoardRegistry.instance.getEmptyRobotBoard());
//        for (RedstoneBoardNBT<?> boardNBT : boardNBTs) {
//            String type = boardNBT.getItemModelLocation();
//            /* Neat little trick: we have to register the models, but NEVER for meta 0 (because of the way minecraft
//             * gets its item models). So, provided this number is never 0 it will work */
//            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(this, 1, new ModelResourceLocation(type, "inventory"));
//            ModelBakery.addVariantName(this, type);
//        }
//    }
}

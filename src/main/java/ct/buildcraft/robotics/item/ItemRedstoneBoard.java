package ct.buildcraft.robotics.item;

import java.util.List;

import javax.annotation.Nullable;

import ct.buildcraft.robotics.BCRoboticsBoards;
import ct.buildcraft.robotics.BCRoboticsBoards.BoardEntry;
import ct.buildcraft.robotics.BCRoboticsItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemRedstoneBoard extends Item {
    public ItemRedstoneBoard(Properties properties) {
        super(properties);
    }

    public static ItemStack createStack(BoardEntry board) {
        ItemStack stack = new ItemStack(BCRoboticsItems.REDSTONE_BOARD.get());
        CompoundTag tag = stack.getOrCreateTag();
        board.nbt().createBoard(tag);
        tag.putString("board_key", board.key());
        tag.putString("board_color", board.boardColor());
        return stack;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return BCRoboticsBoards.getBoard(stack) == BCRoboticsBoards.EMPTY ? 16 : 1;
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (!allowedIn(tab)) {
            return;
        }
        BCRoboticsBoards.init();
        for (BoardEntry board : BCRoboticsBoards.entriesWithEmpty()) {
            list.add(createStack(board));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        BoardEntry board = BCRoboticsBoards.getBoard(stack);
        return Component.translatable("item.buildcraftrobotics.redstone_board." + board.key());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        BoardEntry board = BCRoboticsBoards.getBoard(stack);
        if (board != BCRoboticsBoards.EMPTY) {
            String legacyKey = board.legacyLangKey();
            tooltip.add(Component.translatable("buildcraft." + legacyKey).withStyle(ChatFormatting.BOLD));
            tooltip.add(Component.translatable("buildcraft." + legacyKey + ".desc").withStyle(ChatFormatting.GRAY));
            if (board.isInDev()) {
                tooltip.add(Component.literal("in dev").withStyle(ChatFormatting.RED));
            }
            tooltip.add(Component.translatable("tooltip.buildcraftrobotics.board.energy", board.energyCost()).withStyle(ChatFormatting.GRAY));
        }
    }
}

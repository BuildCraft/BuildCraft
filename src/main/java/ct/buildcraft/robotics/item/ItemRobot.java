package ct.buildcraft.robotics.item;

import java.util.List;

import javax.annotation.Nullable;

import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.BCRobotics;
import ct.buildcraft.robotics.BCRoboticsBoards;
import ct.buildcraft.robotics.BCRoboticsBoards.BoardEntry;
import ct.buildcraft.robotics.BCRoboticsItems;

import ct.buildcraft.api.events.RobotEvent;
import ct.buildcraft.robotics.entity.EntityRobot;
import ct.buildcraft.robotics.plug.RobotStationPluggable;
import ct.buildcraft.transport.tile.TilePipeHolder;
import ct.buildcraft.transport.block.BlockPipeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemRobot extends Item {
    public ItemRobot(Properties properties) {
        super(properties);
    }

    public static ItemStack createRobotStack(BoardEntry board, int energy) {
        ItemStack stack = new ItemStack(BCRoboticsItems.ROBOT.get());
        CompoundTag boardTag = new CompoundTag();
        board.nbt().createBoard(boardTag);
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("board", boardTag);
        tag.putInt("energy", Math.max(0, Math.min(EntityRobotBase.MAX_ENERGY, energy)));
        tag.putString("robot_key", board.key());
        return stack;
    }

    public static int getEnergy(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt("energy");
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (!allowedIn(tab)) {
            return;
        }
        BCRoboticsBoards.init();
        list.add(createRobotStack(BCRoboticsBoards.EMPTY, 0));
        for (BoardEntry board : BCRoboticsBoards.robotEntries()) {
            list.add(createRobotStack(board, 0));
            list.add(createRobotStack(board, EntityRobotBase.MAX_ENERGY));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        BoardEntry board = BCRoboticsBoards.getRobotBoard(stack);
        if (board == BCRoboticsBoards.EMPTY) {
            return Component.translatable("item.buildcraftrobotics.robot.empty");
        }
        return Component.translatable("item.buildcraftrobotics.robot." + board.key());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        BoardEntry board = BCRoboticsBoards.getRobotBoard(stack);
        if (board != BCRoboticsBoards.EMPTY) {
            String legacyKey = board.legacyLangKey();
            tooltip.add(Component.translatable("buildcraft." + legacyKey).withStyle(ChatFormatting.BOLD));
            tooltip.add(Component.translatable("buildcraft." + legacyKey + ".desc").withStyle(ChatFormatting.GRAY));
            if (board.isInDev()) {
                tooltip.add(Component.literal("in dev").withStyle(ChatFormatting.RED));
            }
            tooltip.add(Component.translatable("tooltip.buildcraftrobotics.robot.energy", getEnergy(stack), EntityRobotBase.MAX_ENERGY)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public EntityRobot createRobot(ItemStack stack, Level level) {
        BoardEntry board = BCRoboticsBoards.getRobotBoard(stack);
        if (board == BCRoboticsBoards.EMPTY) {
            return null;
        }
        EntityRobot robot = new EntityRobot(level, board);
        robot.setEnergy(getEnergy(stack));
        return robot;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof TilePipeHolder tile)) {
            return InteractionResult.PASS;
        }

        Direction side = BlockPipeHolder.rayTracePluggableSide(level, context.getClickedPos(), player);
        if (side == null) {
            return InteractionResult.PASS;
        }
        if (!(tile.getPluggable(side) instanceof RobotStationPluggable station)) {
            return InteractionResult.PASS;
        }
        return placeOnStation(context.getItemInHand(), player, level, tile, side, station);
    }

    public static InteractionResult placeOnStation(ItemStack currentItem, Player player, Level level, TilePipeHolder tile,
                                                   Direction side, RobotStationPluggable pluggable) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        DockingStation station = pluggable.getStation();
        if (station == null || station.isTaken()) {
            return InteractionResult.SUCCESS;
        }
        BoardEntry board = BCRoboticsBoards.getRobotBoard(currentItem);
        if (board == BCRoboticsBoards.EMPTY) {
            return InteractionResult.SUCCESS;
        }
        EntityRobot robot = ((ItemRobot) currentItem.getItem()).createRobot(currentItem, level);
        if (robot == null || robot.getRegistry() == null) {
            return InteractionResult.SUCCESS;
        }
        RobotEvent.Place robotEvent = new RobotEvent.Place(robot, player);
        MinecraftForge.EVENT_BUS.post(robotEvent);
        if (robotEvent.isCanceled()) {
            return InteractionResult.SUCCESS;
        }

        robot.setUniqueRobotId(robot.getRegistry().getNextRobotId());
        BlockPos pos = tile.getPipePos();
        robot.setPos(pos.getX() + 0.5D + side.getStepX() * 0.5D,
                pos.getY() + 0.5D + side.getStepY() * 0.5D,
                pos.getZ() + 0.5D + side.getStepZ() * 0.5D);
        if (station.takeAsMain(robot)) {
            robot.dock(robot.getLinkedStation());
            level.addFreshEntity(robot);
            tile.scheduleRenderUpdate();
            if (!player.isCreative()) {
                currentItem.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

}

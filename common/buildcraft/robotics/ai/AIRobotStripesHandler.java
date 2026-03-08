/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.lib.misc.FakePlayerProvider;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Collection;

public class AIRobotStripesHandler extends AIRobot implements IStripesActivator {
    private BlockPos useToBlock;
    private int useCycles = 0;

    public AIRobotStripesHandler(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotStripesHandler(EntityRobotBase iRobot, BlockPos index) {
        this(iRobot);

        useToBlock = index;
    }

    @Override
    public void start() {
        robot.aimItemAt(useToBlock);
        robot.setItemActive(true);
    }

    @Override
    public void update() {
        if (useToBlock == null) {
            setSuccess(false);
            terminate();
            return;
        }

        useCycles++;

        if (useCycles > 60) {
            // ItemStack stack = robot.getHeldItem();
            ItemStack stack = robot.getMainHandItem();

            // Direction direction = Direction.NORTH;

            Player player = FakePlayerProvider.INSTANCE.getFakePlayer((ServerLevel) robot.level, FakePlayerProvider.NULL_PROFILE, useToBlock);
            // player.rotationPitch = 0;
            player.setYRot(180);
            // player.rotationYaw = 180;
            player.setXRot(180);

            // for (IStripesHandler handler : PipeManager.stripesHandlers)
            for (IStripesHandlerItem handler : PipeApi.stripeRegistry.getItemHandlers().values().stream().flatMap(Collection::stream).toList()) {
                // if (handler.getType() == StripesHandlerType.ITEM_USE && handler.shouldHandle(stack))
                if (handler instanceof IStripesHandlerItem) {
                    // if (handler.handle(robot.worldObj, useToBlock, direction, stack, player, this))
                    if (Arrays.stream(Direction.values()).anyMatch(direction -> handler.handle(robot.level, useToBlock.relative(direction.getOpposite()), direction, stack, player, this))) {
                        // robot.setItemInUse(StackUtil.EMPTY);
                        robot.setItemInUse(player.getMainHandItem());
                        terminate();
                        return;
                    }
                }
            }
            terminate();
        }
    }

    @Override
    public void end() {
        robot.setItemActive(false);
    }

    @Override
    // public int getEnergyCost()
    public long getPowerCost() {
        return 15 * MjAPI.MJ / 10;
    }

    @Override
    public boolean sendItem(ItemStack stack, Direction direction) {
        // InvUtils.dropItems(robot.level, stack, VecUtil.getPos(robot));
        InventoryUtil.drop(robot.level, VecUtil.getPos(robot), stack);
        return true;
    }

    @Override
    public void dropItem(ItemStack stack, Direction direction) {
        sendItem(stack, direction);
    }
}

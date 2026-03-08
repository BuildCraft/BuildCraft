/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class AIRobotUseToolOnBlock extends AIRobot {

    private BlockPos useToBlock;
    private int useCycles = 0;

    public AIRobotUseToolOnBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotUseToolOnBlock(EntityRobotBase iRobot, BlockPos index) {
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
        useCycles++;

        if (useCycles > 40) {
            ItemStack stack = robot.getMainHandItem();

            PlayerEntity player = FakePlayerProvider.INSTANCE.getFakePlayer((ServerWorld) robot.level, FakePlayerProvider.NULL_PROFILE);
            if (BlockUtil.useItemOnBlock(robot.level, player, stack, useToBlock, Direction.UP)) {
//                if (robot.getHeldItem().isItemStackDamageable())
                if (robot.getMainHandItem().isDamageableItem()) {
//                    robot.getHeldItem().damageItem(1, robot);
                    robot.getMainHandItem().setDamageValue(robot.getMainHandItem().getDamageValue() - 1);

                    if (robot.getMainHandItem().getDamageValue() >= robot.getMainHandItem().getMaxDamage()) {
                        robot.setItemInUse(StackUtil.EMPTY);
                    }
                } else {
                    robot.setItemInUse(StackUtil.EMPTY);
                }
            } else {
                setSuccess(false);
//                if (!robot.getHeldItem().isItemStackDamageable())
                if (!robot.getMainHandItem().isDamageableItem()) {
                    BlockUtil.dropItem((ServerWorld) robot.level, VecUtil.getPos(robot), 6000, stack);
                    robot.setItemInUse(StackUtil.EMPTY);
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
        return 8 * MjAPI.MJ / 10;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundNBT nbt) {
        super.writeSelfToNBT(nbt);

        if (useToBlock != null) {
            nbt.put("blockFound", NBTUtilBC.writeBlockPos(useToBlock));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundNBT nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("blockFound")) {
            useToBlock = NBTUtilBC.readBlockPos(nbt.get("blockFound"));
        }
    }
}

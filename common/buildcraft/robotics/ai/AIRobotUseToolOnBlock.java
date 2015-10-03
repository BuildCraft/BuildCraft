/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;

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
            ItemStack stack = robot.getHeldItem();

            EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) robot.worldObj).get();
            if (BlockUtils.useItemOnBlock(robot.worldObj, player, stack, useToBlock, EnumFacing.UP)) {
                if (robot.getHeldItem().isItemStackDamageable()) {
                    robot.getHeldItem().damageItem(1, robot);

                    if (robot.getHeldItem().getItemDamage() >= robot.getHeldItem().getMaxDamage()) {
                        robot.setItemInUse(null);
                    }
                } else {
                    robot.setItemInUse(null);
                }
            } else {
                setSuccess(false);
                if (!robot.getHeldItem().isItemStackDamageable()) {
                    BlockUtils.dropItem((WorldServer) robot.worldObj, Utils.getPos(robot), 6000, stack);
                    robot.setItemInUse(null);
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
    public int getEnergyCost() {
        return 8;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(NBTTagCompound nbt) {
        super.writeSelfToNBT(nbt);

        if (useToBlock != null) {
            nbt.setTag("blockFound", NBTUtils.writeBlockPos(useToBlock));
        }
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.hasKey("blockFound")) {
            useToBlock = NBTUtils.readBlockPos(nbt.getTag("blockFound"));
        }
    }
}

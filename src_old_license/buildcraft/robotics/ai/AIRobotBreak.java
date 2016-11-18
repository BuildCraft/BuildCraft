/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

import buildcraft.core.proxy.CoreProxy;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.NBTUtils;

public class AIRobotBreak extends AIRobot {
    private BlockPos blockToBreak;
    private float blockDamage = 0;

    private IBlockState state;
    private float hardness;
    private float speed;

    public AIRobotBreak(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotBreak(EntityRobotBase iRobot, BlockPos iBlockToBreak) {
        this(iRobot);

        blockToBreak = iBlockToBreak;
    }

    @Override
    public void start() {
        robot.aimItemAt(blockToBreak);

        robot.setItemActive(true);
        state = robot.worldObj.getBlockState(blockToBreak);
        hardness = state.getBlock().getBlockHardness(robot.worldObj, blockToBreak);
        speed = getBreakSpeed(robot, robot.getHeldItem(), state, blockToBreak);
    }

    @Override
    public void update() {
        if (state == null) {
            state = robot.worldObj.getBlockState(blockToBreak);
            if (state.getBlock().isAir(robot.worldObj, blockToBreak)) {
                setSuccess(false);
                terminate();
                return;
            }
            state = robot.worldObj.getBlockState(blockToBreak);
            hardness = state.getBlock().getBlockHardness(robot.worldObj, blockToBreak);
            speed = getBreakSpeed(robot, robot.getHeldItem(), state, blockToBreak);
        }

        if (state.getBlock().isAir(robot.worldObj, blockToBreak) || hardness < 0) {
            setSuccess(false);
            terminate();
            return;
        }

        if (hardness != 0) {
            blockDamage += speed / hardness / 30F;
        } else {
            // Instantly break the block
            blockDamage = 1.1F;
        }

        if (blockDamage > 1.0F) {
            robot.worldObj.sendBlockBreakProgress(robot.getEntityId(), blockToBreak, -1);
            blockDamage = 0;

            boolean continueBreaking = true;

            if (robot.getHeldItem() != null) {
                EntityPlayer fakePlayer = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) robot.worldObj, robot.getPosition()).get();
                if (robot.getHeldItem().getItem().onBlockStartBreak(robot.getHeldItem(), blockToBreak, fakePlayer)) {
                    continueBreaking = false;
                }
            }

            if (continueBreaking && BlockUtil.harvestBlock((WorldServer) robot.worldObj, blockToBreak, robot.getHeldItem(), robot.getPosition())) {
                robot.worldObj.playAuxSFXAtEntity(null, 2001, blockToBreak, Block.getStateId(state));

                if (robot.getHeldItem() != null) {
                    robot.getHeldItem().getItem().onBlockDestroyed(robot.getHeldItem(), robot.worldObj, state.getBlock(), blockToBreak, robot);

                    if (robot.getHeldItem().stackSize == 0) {
                        robot.setItemInUse(null);
                    }
                }
            } else {
                setSuccess(false);
            }

            terminate();
        } else {
            robot.worldObj.sendBlockBreakProgress(robot.getEntityId(), blockToBreak, (int) (blockDamage * 10.0F) - 1);
        }
    }

    @Override
    public void end() {
        robot.setItemActive(false);
        robot.worldObj.sendBlockBreakProgress(robot.getEntityId(), blockToBreak, -1);
    }

    private float getBreakSpeed(EntityRobotBase robot, ItemStack usingItem, IBlockState state, BlockPos pos) {
        ItemStack stack = usingItem;
        float f = stack == null ? 1.0F : stack.getItem().getDigSpeed(stack, state);

        if (f > 1.0F) {
            int i = EnchantmentHelper.getEfficiencyModifier(robot);

            if (i > 0) {
                float f1 = i * i + 1;

                boolean canHarvest = ForgeHooks.canToolHarvestBlock(robot.worldObj, pos, usingItem);

                if (!canHarvest && f <= 1.0F) {
                    f += f1 * 0.08F;
                } else {
                    f += f1;
                }
            }
        }

        f = ForgeEventFactory.getBreakSpeed(BlockUtil.getFakePlayerWithTool((WorldServer) robot.worldObj, blockToBreak, robot.getHeldItem()), state,
                f, blockToBreak);
        return f < 0 ? 0 : f;
    }

    @Override
    public int getEnergyCost() {
        return (int) Math.ceil((float) BuilderAPI.BREAK_ENERGY * 2 / 30.0F);
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(NBTTagCompound nbt) {
        super.writeSelfToNBT(nbt);

        if (blockToBreak != null) {
            nbt.setTag("blockToBreak", NBTUtils.writeBlockPos(blockToBreak));
        }
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.hasKey("blockToBreak")) {
            blockToBreak = NBTUtils.readBlockPos(nbt.getTag("blockToBreak"));
        }
    }
}

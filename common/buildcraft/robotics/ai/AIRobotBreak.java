/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;

public class AIRobotBreak extends AIRobot {
    private BlockPos blockToBreak;
    private float blockDamage = 0;

    private BlockState state;
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
        state = robot.level.getBlockState(blockToBreak);
        hardness = state.getDestroySpeed(robot.level, blockToBreak);
        //        speed = getBreakSpeed(robot, robot.getHeldItem(), state, blockToBreak);
        speed = getBreakSpeed(robot, robot.getMainHandItem(), state, blockToBreak);
    }

    @Override
    public void update() {
        if (state == null) {
            state = robot.level.getBlockState(blockToBreak);
//            if (state.getBlock().isAir(robot.level, blockToBreak))
            if (state.isAir()) {
                setSuccess(false);
                terminate();
                return;
            }
            state = robot.level.getBlockState(blockToBreak);
            hardness = state.getDestroySpeed(robot.level, blockToBreak);
//            speed = getBreakSpeed(robot, robot.getHeldItem(), state, blockToBreak);
            speed = getBreakSpeed(robot, robot.getMainHandItem(), state, blockToBreak);
        }

        if (state.isAir() || hardness < 0) {
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
            robot.level.destroyBlockProgress(robot.getId(), blockToBreak, -1);
            blockDamage = 0;

            boolean continueBreaking = true;

            // if (robot.getMainHandItem() != null)
            if (!robot.getMainHandItem().isEmpty()) {
                FakePlayer fakePlayer = FakePlayerProvider.INSTANCE.getFakePlayer((ServerLevel) robot.level, FakePlayerProvider.NULL_PROFILE);
                if (robot.getMainHandItem().getItem().onBlockStartBreak(robot.getMainHandItem(), blockToBreak, fakePlayer)) {
                    continueBreaking = false;
                }
            }

            if (continueBreaking && BlockUtil.harvestBlock((ServerLevel) robot.level, blockToBreak, robot.getMainHandItem(), FakePlayerProvider.NULL_PROFILE)) {
                // robot.worldObj.playAuxSFXAtEntity(null, 2001, blockToBreak, Block.getStateId(state));
                SoundUtil.playBlockBreak(robot.level, blockToBreak, state);

                // if (robot.getMainHandItem() != null)
                if (!robot.getMainHandItem().isEmpty()) {
                    // robot.getMainHandItem().getItem().onBlockDestroyed(robot.getMainHandItem(), robot.level, state.getBlock(), blockToBreak, robot);
                    robot.getMainHandItem().getItem().mineBlock(robot.getMainHandItem(), robot.level, state, blockToBreak, robot);

                    if (robot.getMainHandItem().getCount() == 0) {
                        robot.setItemInUse(StackUtil.EMPTY);
                    }
                }
            } else {
                setSuccess(false);
            }

            terminate();
        } else {
            robot.level.destroyBlockProgress(robot.getId(), blockToBreak, (int) (blockDamage * 10.0F) - 1);
        }
    }

    @Override
    public void end() {
        robot.setItemActive(false);
        robot.level.destroyBlockProgress(robot.getId(), blockToBreak, -1);
    }

    private float getBreakSpeed(EntityRobotBase robot, ItemStack usingItem, BlockState state, BlockPos pos) {
        ItemStack stack = usingItem;
        float f = (stack == null || stack.isEmpty()) ? 1.0F : stack.getItem().getDestroySpeed(stack, state);

        if (f > 1.0F) {
            // int i = EnchantmentHelper.getEfficiencyModifier(robot);
            int i = EnchantmentHelper.getBlockEfficiency(robot);

            if (i > 0) {
                float f1 = i * i + 1;

                // boolean canHarvest = ForgeHooks.canToolHarvestBlock(robot.level, pos, usingItem);
                boolean canHarvest = ForgeHooks.isCorrectToolForDrops(robot.level.getBlockState(pos), BlockUtil.getFakePlayerWithTool((ServerLevel) robot.level, usingItem, FakePlayerProvider.NULL_PROFILE));

                if (!canHarvest && f <= 1.0F) {
                    f += f1 * 0.08F;
                } else {
                    f += f1;
                }
            }
        }

        f = ForgeEventFactory.getBreakSpeed(BlockUtil.getFakePlayerWithTool((ServerLevel) robot.level, robot.getMainHandItem(), FakePlayerProvider.NULL_PROFILE), state,
                f, blockToBreak);
        return f < 0 ? 0 : f;
    }

    @Override
    // public int getEnergyCost()
    public long getPowerCost() {
        // return (int) Math.ceil((float) BuilderAPI.BREAK_ENERGY * 2 / 30.0F);
        return (int) Math.ceil((float) 16 * MjAPI.MJ * 2 / 30.0F);
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (blockToBreak != null) {
            nbt.put("blockToBreak", NBTUtilBC.writeBlockPos(blockToBreak));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("blockToBreak")) {
            blockToBreak = NBTUtilBC.readBlockPos(nbt.get("blockToBreak"));
        }
    }
}

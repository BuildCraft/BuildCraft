/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;

public class BoardRobotMiner extends BoardRobotGenericBreakBlock {
    // private static final int MAX_HARVEST_LEVEL = 3;
    // private int harvestLevel = 0;
    private IItemTier harvestLevel = ItemTier.WOOD;

    public BoardRobotMiner(EntityRobotBase iRobot) {
        super(iRobot);
        detectHarvestLevel();
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        super.delegateAIEnded(ai);

        if (ai instanceof AIRobotFetchAndEquipItemStack) {
            if (ai.success()) {
                detectHarvestLevel();
            }
        }
    }

    private void detectHarvestLevel() {
        // ItemStack stack = robot.getHeldItem();
        ItemStack stack = robot.getMainHandItem();

        // if (stack != null && stack.getItem() != null && stack.getItem().getToolClasses(stack).contains("pickaxe"))
        if (!stack.isEmpty() && stack.getItem() instanceof PickaxeItem) {
            // harvestLevel = stack.getItem().getHarvestLevel(stack, "pickaxe");
            harvestLevel = ((PickaxeItem) stack.getItem()).getTier();
        }
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("miner");
    }

    @Override
    public boolean isExpectedTool(@Nonnull ItemStack stack) {
        // return stack != null && stack.getItem().getToolClasses(stack).contains("pickaxe");
        return !stack.isEmpty() && stack.getItem() instanceof PickaxeItem;
    }

    @Override
    public boolean isExpectedBlock(World world, BlockPos pos) {
        // return BuildCraftAPI.getWorldProperty("ore@hardness=" + Math.min(MAX_HARVEST_LEVEL, harvestLevel)).get(world, pos);
        if (harvestLevel != null) {
            // return world.getBlockState(pos).is(harvestLevel.getTag());
            BlockState state = world.getBlockState(pos);
            return state.is(Tags.Blocks.ORES) && state.getHarvestLevel() <= harvestLevel.getLevel();
        } else {
            return false;
        }
    }

}

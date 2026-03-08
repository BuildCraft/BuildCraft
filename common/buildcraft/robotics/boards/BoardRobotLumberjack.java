/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class BoardRobotLumberjack extends BoardRobotGenericBreakBlock {

    public BoardRobotLumberjack(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public BoardRobotLumberjack(EntityRobotBase iRobot, CompoundTag nbt) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("lumberjack");
    }

    @Override
    public boolean isExpectedTool(@Nonnull ItemStack stack) {
        // return stack != null && stack.getItem().getToolClasses(stack).contains("axe");
        return !stack.isEmpty() && stack.getItem() instanceof AxeItem;
    }

    @Override
    public boolean isExpectedBlock(Level world, BlockPos pos) {
        // return BuildCraftAPI.getWorldProperty("wood").get(world, pos);
        return world.getBlockState(pos).is(BlockTags.LOGS);
    }
}

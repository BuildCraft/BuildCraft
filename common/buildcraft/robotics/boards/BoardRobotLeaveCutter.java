/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BoardRobotLeaveCutter extends BoardRobotGenericBreakBlock {

    public BoardRobotLeaveCutter(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("leave_cutter");
    }

    @Override
    // public boolean isExpectedTool(ItemStack stack)
    public boolean isExpectedTool(@Nonnull ItemStack stack) {
        // return stack != null && stack.getItem() instanceof ShearsItem;
        return !stack.isEmpty() && stack.getItem() instanceof ShearsItem;
    }

    @Override
    public boolean isExpectedBlock(World world, BlockPos pos) {
        // return BuildCraftAPI.getWorldProperty("leaves").get(world, pos);
        return world.getBlockState(pos).is(BlockTags.LEAVES);
    }

}

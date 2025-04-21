/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BoardRobotShovelman extends BoardRobotGenericBreakBlock {

    public BoardRobotShovelman(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("shovelman");
    }

    @Override
    public boolean isExpectedTool(@Nonnull ItemStack stack) {
        // return stack != null && stack.getItem().getToolClasses(stack).contains("shovel");
        return !stack.isEmpty() && stack.getItem() instanceof ShovelItem;
    }

    @Override
    public boolean isExpectedBlock(World world, BlockPos pos) {
        // return BuildCraftAPI.getWorldProperty("shoveled").get(world, pos);
        return ShovelItem.DIGGABLES.contains(world.getBlockState(pos).getBlock());
    }

}

/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotLumberjack extends BoardRobotGenericBreakBlock {

    public BoardRobotLumberjack(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public BoardRobotLumberjack(EntityRobotBase iRobot, NBTTagCompound nbt) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("lumberjack");
    }

    @Override
    public boolean isExpectedTool(ItemStack stack) {
        return stack != null && stack.getItem().getToolClasses(stack).contains("axe");
    }

    @Override
    public boolean isExpectedBlock(World world, BlockPos pos) {
        return BuildCraftAPI.getWorldProperty("wood").get(world, pos);
    }
}

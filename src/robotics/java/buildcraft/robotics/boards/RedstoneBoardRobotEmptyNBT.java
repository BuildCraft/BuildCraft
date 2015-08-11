package buildcraft.robotics.boards;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.robotics.EntityRobot;

public class RedstoneBoardRobotEmptyNBT extends RedstoneBoardRobotNBT {

    public static RedstoneBoardRobotEmptyNBT instance = new RedstoneBoardRobotEmptyNBT();

    @Override
    public RedstoneBoardRobot create(EntityRobotBase robot) {
        return new BoardRobotEmpty(robot);
    }

    @Override
    public ResourceLocation getRobotTexture() {
        return EntityRobot.ROBOT_BASE;
    }

    @Override
    public String getID() {
        return "buildcraft:boardRobotEmpty";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {}

    @Override
    public String getItemModelLocation() {
        return "buildcraftrobotics:board/clean";
    }

    @Override
    public String getDisplayName() {
        return StringUtils.localize("buildcraft.boardRobotClean");
    }

}

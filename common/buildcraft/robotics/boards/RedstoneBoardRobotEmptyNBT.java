package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class RedstoneBoardRobotEmptyNBT extends RedstoneBoardRobotNBT {

    public static RedstoneBoardRobotEmptyNBT instance = new RedstoneBoardRobotEmptyNBT();

    private static final ResourceLocation ROBOT_ID = new ResourceLocation("buildcraftrobotics:robot_base");

    @Override
    public RedstoneBoardRobot create(EntityRobotBase robot) {
        return new BoardRobotEmpty(robot);
    }

    @Override
    public ResourceLocation getRobotTexture() {
        return EntityRobot.ROBOT_BASE;
    }

    @Override
    public ResourceLocation getRobotTextureFullLocation() {
        return EntityRobot.ROBOT_BASE_PNG;
    }

    @Override
    public ResourceLocation getRobotId() {
        return ROBOT_ID;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation("buildcraftrobotics:board_robot_empty");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void addInformation(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {}

    @Override
    // public String getItemModelLocation()
    public String getBoardTexture() {
        // return "buildcraftrobotics:board/clean";
        return "buildcraftrobotics:items/board/clean";
    }

//    @Override
//    public String getDisplayName() {
//        return LocaleUtil.localize("buildcraft.boardRobotClean");
//    }

    @Override
    public Component getDisplayNameComponent() {
        return new TranslatableComponent("buildcraft.boardRobotClean");
    }
}

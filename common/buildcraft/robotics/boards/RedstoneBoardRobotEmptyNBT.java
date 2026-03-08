package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

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
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {}

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
    public ITextComponent getDisplayNameComponent() {
        return new TranslationTextComponent("buildcraft.boardRobotClean");
    }
}

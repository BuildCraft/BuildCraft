package buildcraft.datagen.robotics;

import buildcraft.datagen.base.BCBaseBlockStateGenerator;
import buildcraft.robotics.BCRoboticsBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class RoboticsBlockStateGenerator extends BCBaseBlockStateGenerator {
    public RoboticsBlockStateGenerator(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
        super(gen, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // zonePlanner
        simple4FacingBlock(
                BCRoboticsBlocks.zonePlanner.get(),
                90,
                180,
                270,
                0,
                models().withExistingParent(BCRoboticsBlocks.zonePlanner.get().getRegistryName().toString(), CUBE)
                        .texture("particle", "buildcraftrobotics:blocks/zone_planner/default")
                        .texture("down", "buildcraftrobotics:blocks/zone_planner/default")
                        .texture("up", "buildcraftrobotics:blocks/zone_planner/top")
                        .texture("north", "buildcraftrobotics:blocks/zone_planner/front")
                        .texture("east", "buildcraftrobotics:blocks/zone_planner/left")
                        .texture("south", "buildcraftrobotics:blocks/zone_planner/back")
                        .texture("west", "buildcraftrobotics:blocks/zone_planner/right")
        );
        // requester
        simple4FacingBlock(
                BCRoboticsBlocks.requester.get(),
                90,
                180,
                270,
                0,
                models().withExistingParent(BCRoboticsBlocks.requester.get().getRegistryName().toString(), CUBE)
                        .texture("particle", "buildcraftrobotics:blocks/requester/side")
                        .texture("down", "buildcraftrobotics:blocks/requester/bottom")
                        .texture("up", "buildcraftrobotics:blocks/requester/top")
                        .texture("north", "buildcraftrobotics:blocks/requester/front")
                        .texture("east", "buildcraftrobotics:blocks/requester/side")
                        .texture("south", "buildcraftrobotics:blocks/requester/back")
                        .texture("west", "buildcraftrobotics:blocks/requester/side")
        );
    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Robotics BlockState Generator";
    }
}

package buildcraft.datagen.robotics;

import buildcraft.datagen.base.BCBaseBlockStateGenerator;
import buildcraft.robotics.BCRobotics;
import buildcraft.robotics.BCRoboticsBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class RoboticsBlockStateGenerator extends BCBaseBlockStateGenerator {
    public RoboticsBlockStateGenerator(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BCRobotics.MODID, exFileHelper);
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
                        .texture("particle", "buildcraftrobotics:block/zone_planner/default")
                        .texture("down", "buildcraftrobotics:block/zone_planner/default")
                        .texture("up", "buildcraftrobotics:block/zone_planner/top")
                        .texture("north", "buildcraftrobotics:block/zone_planner/front")
                        .texture("east", "buildcraftrobotics:block/zone_planner/right")
                        .texture("south", "buildcraftrobotics:block/zone_planner/back")
                        .texture("west", "buildcraftrobotics:block/zone_planner/left")
        );
    }

    @NotNull
    @Override
    public String getName() {
        return "BuildCraft Robotics BlockState Generator";
    }
}

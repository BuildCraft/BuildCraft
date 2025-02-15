package buildcraft.datagen.robotics;

import buildcraft.datagen.base.BCBaseItemModelGenerator;
import buildcraft.robotics.BCRobotics;
import buildcraft.robotics.BCRoboticsBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class RoboticsItemModelGenerator extends BCBaseItemModelGenerator {
    public RoboticsItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BCRobotics.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent(BCRoboticsBlocks.zonePlanner.get().getRegistryName().toString(), new ResourceLocation("buildcraftrobotics:block/zone_planner"));
    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Robotics Item Model Generator";
    }
}

package buildcraft.datagen.robotics;

import buildcraft.datagen.base.BCBaseItemModelGenerator;
import buildcraft.robotics.BCRoboticsBlocks;
import buildcraft.robotics.BCRoboticsItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelBuilder.Perspective;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class RoboticsItemModelGenerator extends BCBaseItemModelGenerator {
    public RoboticsItemModelGenerator(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // blocks
        withExistingParent(BCRoboticsBlocks.zonePlanner.get().getRegistryName().toString(), new ResourceLocation("buildcraftrobotics:block/zone_planner"));
        withExistingParent(BCRoboticsBlocks.requester.get().getRegistryName().toString(), new ResourceLocation("buildcraftrobotics:block/requester"));

        // robotStation
        getBuilder(BCRoboticsItems.robotStation.get().getRegistryName().toString()).parent(BUILTIN_ENTITY);

        // board
        BCRoboticsItems.redstoneBoard.forEach((nbt, item) -> {
            withExistingParent(item.get().getRegistryName().toString(), GENERATED)
                    .texture("layer0", nbt.getBoardTexture());
        });

        // robot
        String robot = "buildcraftrobotics:item/robot";
        getBuilder(robot)
                .element()
                .from(4, 4, 4)
                .to(12, 12, 12)
                .face(Direction.DOWN).texture("#all").uvs(8 * 16 / 32, 0 * 16 / 32, 16, 8 * 16 / 32).end()
                .face(Direction.UP).texture("#all").uvs(16 * 16 / 32, 0 * 16 / 32, 24 * 16 / 32, 8 * 16 / 32).end()
                .face(Direction.NORTH).texture("#all").uvs(0 * 16 / 32, 8 * 16 / 32, 8 * 16 / 32, 16 * 16 / 32).end()
                .face(Direction.SOUTH).texture("#all").uvs(8 * 16 / 32, 8 * 16 / 32, 16 * 16 / 32, 16 * 16 / 32).end()
                .face(Direction.WEST).texture("#all").uvs(16 * 16 / 32, 8 * 16 / 32, 24 * 16 / 32, 16 * 16 / 32).end()
                .face(Direction.EAST).texture("#all").uvs(24 * 16 / 32, 8 * 16 / 32, 32 * 16 / 32, 16 * 16 / 32).end()
                .end()

                .transforms()
                .transform(Perspective.GUI)
                .rotation(30, 225, 0)
                .translation(0, 0, 0)
                .scale(1, 1, 1)
                .end()
                .transform(Perspective.GROUND)
                .rotation(0, 0, 0)
                .translation(0, 3, 0)
                .scale(0.4F, 0.4F, 0.4F)
                .end()
                .transform(Perspective.FIXED)
                .rotation(0, 0, 0)
                .translation(0, 0, 0)
                .scale(0.8F, 0.8F, 0.8F)
                .end()
                .transform(Perspective.THIRDPERSON_RIGHT)
                .rotation(75, 45, 0)
                .translation(0, 2.5F, 0)
                .scale(0.6F, 0.6F, 0.6F)
                .end()
                .transform(Perspective.FIRSTPERSON_RIGHT)
                .rotation(0, 45, 0)
                .translation(0, 0, 0)
                .scale(0.64F, 0.64F, 0.64F)
                .end()
                .transform(Perspective.FIRSTPERSON_LEFT)
                .rotation(0, 225, 0)
                .translation(0, 0, 0)
                .scale(0.64F, 0.64F, 0.64F)
                .end()
                .end()
        ;
        BCRoboticsItems.robot.forEach((nbt, item) -> {
            withExistingParent(BCRoboticsItems.robot.get(nbt).get().getRegistryName().toString(), robot)
                    .texture("all", nbt.getRobotTexture());
        });
    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Robotics Item Model Generator";
    }
}

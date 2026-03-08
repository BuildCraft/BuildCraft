package buildcraft.datagen.core;

import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.core.BCCoreBlocks;
import buildcraft.core.block.BlockDecoration;
import buildcraft.datagen.base.BCBaseBlockStateGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder.Perspective;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class CoreBlockStateGenerator extends BCBaseBlockStateGenerator {
    public static ModelBuilder torch_center_lit;

    public CoreBlockStateGenerator(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
        super(gen, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        models().withExistingParent("buildcraftcore:block/default_cube", CUBE)
                .transforms()
                .transform(Perspective.THIRDPERSON_RIGHT)
                .rotation(10, -45, 170)
                .translation(0, 1.5F, -2.75F)
                .scale(0.375F, 0.375F, 0.375F)
                .end()
                .end()
        ;

        // engine
        builtinEntity(BCCoreBlocks.engineWood.get(), "buildcraftcore:blocks/engine/wood/back");
        builtinEntity(BCCoreBlocks.engineCreative.get(), "buildcraftcore:blocks/engine/creative/back");

        // spring
        simpleBlock(BCCoreBlocks.springWater.get(), new ConfiguredModel(new ModelFile.ExistingModelFile(new ResourceLocation("minecraft:block/bedrock"), this.models().existingFileHelper)));
        simpleBlock(BCCoreBlocks.springOil.get(), new ConfiguredModel(new ModelFile.ExistingModelFile(new ResourceLocation("minecraft:block/bedrock"), this.models().existingFileHelper)));

        // decorated
        BCCoreBlocks.decoratedMap.values().forEach(decorated ->
                {
                    BlockDecoration block = decorated.get();
                    ResourceLocation rl = block.getRegistryName();
                    EnumDecoratedBlock type = block.DECORATED_TYPE;
                    String texture;
                    switch (type) {
                        case BLUEPRINT:
                            texture = "buildcraftcore:blocks/blueprint/blue";
                            break;
                        case DESTROY:
                            texture = "buildcraftcore:blocks/misc/texture_red_dark";
                            break;
                        case LASER_BACK:
                            texture = "buildcraftsilicon:blocks/laser/bottom";
                            break;
                        case LEATHER:
                            texture = "buildcraftcore:blocks/misc/leather";
                            break;
                        case PAPER:
                            texture = "buildcraftcore:blocks/misc/paper";
                            break;
                        case TEMPLATE:
                            texture = "buildcraftcore:blocks/blueprint/black";
                            break;
                        default:
                            throw new RuntimeException("Unexpected EnumDecoratedBlock: [" + type + "]");
                    }
                    getVariantBuilder(block).forAllStates(
                            s -> ConfiguredModel.builder().modelFile(
                                            models().withExistingParent(rl.getNamespace() + ":block/decorated/" + type.getSerializedName(), CUBE_ALL)
                                                    .texture("all", texture)
                                    )
                                    .build()
                    );

                }
        );

        // markerVolume
        torch_center_lit = models().getBuilder("buildcraftcore:block/torch_center_lit")
                .texture("particle", "#all")
                .element()
                .from(7, 0, 7)
                .to(9, 9, 9)
                .shade(false)
                .face(Direction.EAST).uvs(2, 5, 4, 14).texture("#all").end()
                .face(Direction.NORTH).uvs(4, 5, 6, 14).texture("#all").end()
                .face(Direction.WEST).uvs(6, 5, 8, 14).texture("#all").end()
                .face(Direction.SOUTH).uvs(0, 5, 2, 14).texture("#all").end()
                .face(Direction.DOWN).uvs(0, 14, 2, 16).texture("#all").end()
                .face(Direction.UP).uvs(0, 3, 2, 5).texture("#all").end()
                .end();
        ModelBuilder markerVolume = models().getBuilder("buildcraftcore:block/marker_volume")
                .parent(torch_center_lit)
                .texture("all", "buildcraftcore:blocks/marker_volume");
        getMultipartBuilder(BCCoreBlocks.markerVolume.get())
                .part()
                .modelFile(markerVolume)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.UP)
                .end()
                .part()
                .modelFile(markerVolume)
                .rotationX(180)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.DOWN)
                .end()
                .part()
                .modelFile(markerVolume)
                .rotationX(90)
                .rotationY(90)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.EAST)
                .end()
                .part()
                .modelFile(markerVolume)
                .rotationX(90)
                .rotationY(180)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.SOUTH)
                .end()
                .part()
                .modelFile(markerVolume)
                .rotationX(90)
                .rotationY(270)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.WEST)
                .end()
                .part()
                .modelFile(markerVolume)
                .rotationX(90)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.NORTH)
                .end()
        ;

        // markerPath
        ModelBuilder markerPath = models().getBuilder("buildcraftcore:block/marker_path")
                .parent(torch_center_lit)
                .texture("all", "buildcraftcore:blocks/marker_path");
        getMultipartBuilder(BCCoreBlocks.markerPath.get())
                .part()
                .modelFile(markerPath)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.UP)
                .end()
                .part()
                .modelFile(markerPath)
                .rotationX(180)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.DOWN)
                .end()
                .part()
                .modelFile(markerPath)
                .rotationX(90)
                .rotationY(90)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.EAST)
                .end()
                .part()
                .modelFile(markerPath)
                .rotationX(90)
                .rotationY(180)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.SOUTH)
                .end()
                .part()
                .modelFile(markerPath)
                .rotationX(90)
                .rotationY(270)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.WEST)
                .end()
                .part()
                .modelFile(markerPath)
                .rotationX(90)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.NORTH)
                .end()
        ;

        // power_tester
        getVariantBuilder(BCCoreBlocks.powerTester.get()).forAllStates(s ->
                ConfiguredModel.builder().modelFile(
                                models().withExistingParent("buildcraftcore:block/power_tester", CUBE_ALL)
                                        .texture("all", "buildcraftcore:blocks/power_tester")
                        )
                        .build()
        );
    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Core BlockState Generator";
    }
}

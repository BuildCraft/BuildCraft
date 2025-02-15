package buildcraft.datagen.core;

import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.core.BCCore;
import buildcraft.core.BCCoreBlocks;
import buildcraft.core.block.BlockDecoration;
import buildcraft.datagen.base.BCBaseBlockStateGenerator;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class CoreBlockStateGenerator extends BCBaseBlockStateGenerator {
    public CoreBlockStateGenerator(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BCCore.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        models().withExistingParent("buildcraftcore:block/default_cube", CUBE)
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(10, -45, 170)
                .translation(0, 1.5F, -2.75F)
                .scale(0.375F, 0.375F, 0.375F)
                .end()
                .end()
        ;

        // engine
        builtinEntity(BCCoreBlocks.engineWood.get(), "buildcraftcore:block/engine/wood/back");
        builtinEntity(BCCoreBlocks.engineCreative.get(), "buildcraftcore:block/engine/creative/back");

        // spring
        simpleBlock(BCCoreBlocks.springWater.get(), new ConfiguredModel(new ModelFile.ExistingModelFile(new ResourceLocation("minecraft:block/bedrock"), this.models().existingFileHelper)));
        simpleBlock(BCCoreBlocks.springOil.get(), new ConfiguredModel(new ModelFile.ExistingModelFile(new ResourceLocation("minecraft:block/bedrock"), this.models().existingFileHelper)));

        // decorated
        BCCoreBlocks.decoratedMap.values().forEach(decorated ->
                {
                    BlockDecoration block = decorated.get();
                    ResourceLocation rl = block.getRegistryName();
                    EnumDecoratedBlock type = block.DECORATED_TYPE;
                    String texture = switch (type) {
                        case BLUEPRINT -> "buildcraftcore:block/blueprint/blue";
                        case DESTROY -> "buildcraftcore:block/misc/texture_red_dark";
                        case LASER_BACK -> "buildcraftsilicon:block/laser/bottom";
                        case LEATHER -> "buildcraftcore:block/misc/leather";
                        case PAPER -> "buildcraftcore:block/misc/paper";
                        case TEMPLATE -> "buildcraftcore:block/blueprint/black";
                    };
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
        ModelBuilder torch_center_lit = models().getBuilder("buildcraftcore:block/torch_center_lit")
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
                .texture("all", "buildcraftcore:block/marker_volume");
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
                .texture("all", "buildcraftcore:block/marker_path");
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
                                        .texture("all", "buildcraftcore:block/power_tester")
                        )
                        .build()
        );
    }

    @NotNull
    @Override
    public String getName() {
        return "BuildCraft Core BlockState Generator";
    }
}

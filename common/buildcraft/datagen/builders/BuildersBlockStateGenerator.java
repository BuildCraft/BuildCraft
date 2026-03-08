package buildcraft.datagen.builders;

import buildcraft.api.enums.EnumOptionalSnapshotType;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.builders.BCBuilders;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.block.BlockArchitectTable;
import buildcraft.builders.block.BlockBuilder;
import buildcraft.datagen.base.BCBaseBlockStateGenerator;
import buildcraft.datagen.core.CoreBlockStateGenerator;
import buildcraft.lib.block.BlockBCBase_Neptune;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class BuildersBlockStateGenerator extends BCBaseBlockStateGenerator {
    public BuildersBlockStateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, BCBuilders.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // builder
        ModelBuilder<BlockModelBuilder> builder_main =
                models().withExistingParent("buildcraftbuilders:block/builder/main", CUBE)
                        .texture("particle", "buildcraftbuilders:blocks/builder/side")
                        .texture("down", "buildcraftbuilders:blocks/builder/bottom")
                        .texture("up", "buildcraftbuilders:blocks/builder/top")
                        .texture("north", "buildcraftbuilders:blocks/builder/front")
                        .texture("east", "buildcraftbuilders:blocks/builder/side")
                        .texture("south", "buildcraftbuilders:blocks/builder/back")
                        .texture("west", "buildcraftbuilders:blocks/builder/side");
        ModelBuilder<BlockModelBuilder> slot_empty =
                models().getBuilder("buildcraftbuilders:block/builder/slot_empty")
                        .texture("all", "buildcraftbuilders:blocks/builder/slot_empty")
                        .element()
                        .from(3, 4, -0.01F)
                        .to(13, 6, 9.99F)
                        .face(Direction.DOWN).texture("#all").uvs(3, 3, 13, 13).end()
                        .face(Direction.UP).texture("#all").uvs(3, 3, 13, 13).end()
                        .face(Direction.NORTH).texture("#all").uvs(3, 0, 13, 2).end()
                        .face(Direction.SOUTH).texture("#all").uvs(3, 0, 13, 2).end()
                        .face(Direction.WEST).texture("#all").uvs(3, 0, 13, 2).end()
                        .face(Direction.EAST).texture("#all").uvs(3, 0, 13, 2).end()
                        .end();
        ModelBuilder<BlockModelBuilder> slot_blueprint =
                models().getBuilder("buildcraftbuilders:block/builder/slot_blueprint")
                        .texture("slot", "buildcraftbuilders:blocks/builder/slot_blueprint")
                        .element()
                        .from(3, 4, -2)
                        .to(13, 6, 8)
                        .face(Direction.DOWN).texture("#slot").uvs(3, 3, 13, 13).end()
                        .face(Direction.UP).texture("#slot").uvs(3, 3, 13, 13).end()
                        .face(Direction.NORTH).texture("#slot").uvs(3, 0, 13, 2).end()
                        .face(Direction.SOUTH).texture("#slot").uvs(3, 0, 13, 2).end()
                        .face(Direction.WEST).texture("#slot").uvs(3, 0, 13, 2).end()
                        .face(Direction.EAST).texture("#slot").uvs(3, 0, 13, 2).end()
                        .end();
        ModelBuilder<BlockModelBuilder> slot_template =
                models().getBuilder("buildcraftbuilders:block/builder/slot_template")
                        .texture("template", "buildcraftbuilders:blocks/builder/slot_template")
                        .element()
                        .from(3, 4, -2)
                        .to(13, 6, 8)
                        .face(Direction.DOWN).texture("#template").uvs(3, 3, 13, 13).end()
                        .face(Direction.UP).texture("#template").uvs(3, 3, 13, 13).end()
                        .face(Direction.NORTH).texture("#template").uvs(3, 0, 13, 2).end()
                        .face(Direction.SOUTH).texture("#template").uvs(3, 0, 13, 2).end()
                        .face(Direction.WEST).texture("#template").uvs(3, 0, 13, 2).end()
                        .face(Direction.EAST).texture("#template").uvs(3, 0, 13, 2).end()
                        .end();
        MultiPartBlockStateBuilder multiPartBuilder_block_builder = getMultipartBuilder(BCBuildersBlocks.builder.get());
        add4Facing(multiPartBuilder_block_builder, builder_main, null, null);
        add4Facing(multiPartBuilder_block_builder, slot_empty, BlockBuilder.SNAPSHOT_TYPE, EnumOptionalSnapshotType.NONE);
        add4Facing(multiPartBuilder_block_builder, slot_blueprint, BlockBuilder.SNAPSHOT_TYPE, EnumOptionalSnapshotType.BLUEPRINT);
        add4Facing(multiPartBuilder_block_builder, slot_template, BlockBuilder.SNAPSHOT_TYPE, EnumOptionalSnapshotType.TEMPLATE);

        // frame
        ModelBuilder connection = models().withExistingParent("buildcraftbuilders:block/frame/connection", CUBE_ALL)
                .element()
                .from(4, 4, 0)
                .to(12, 12, 4)
                .face(Direction.DOWN).texture("#all").end()
                .face(Direction.UP).texture("#all").end()
                .face(Direction.WEST).texture("#all").end()
                .face(Direction.EAST).texture("#all").end()
                .end()
                .element()
                .from(12, 12, 4)
                .to(4, 4, 0)
                .face(Direction.DOWN).texture("#all").end()
                .face(Direction.UP).texture("#all").end()
                .face(Direction.WEST).texture("#all").end()
                .face(Direction.EAST).texture("#all").end()
                .end()
                .texture("all", "buildcraftbuilders:blocks/frame/default");
        getMultipartBuilder(BCBuildersBlocks.frame.get())
                .part()
                .modelFile(
                        models().withExistingParent("buildcraftbuilders:block/frame/base", CUBE_ALL)
                                .element()
                                .from(4, 4, 4)
                                .to(12, 12, 12)
                                .face(Direction.DOWN).texture("#all").cullface(Direction.DOWN).end()
                                .face(Direction.UP).texture("#all").cullface(Direction.UP).end()
                                .face(Direction.NORTH).texture("#all").cullface(Direction.NORTH).end()
                                .face(Direction.SOUTH).texture("#all").cullface(Direction.SOUTH).end()
                                .face(Direction.WEST).texture("#all").cullface(Direction.WEST).end()
                                .face(Direction.EAST).texture("#all").cullface(Direction.EAST).end()
                                .end()
                                .element()
                                .from(12, 12, 12)
                                .to(4, 4, 4)
                                .face(Direction.DOWN).texture("#all").cullface(Direction.UP).end()
                                .face(Direction.UP).texture("#all").cullface(Direction.DOWN).end()
                                .face(Direction.NORTH).texture("#all").cullface(Direction.SOUTH).end()
                                .face(Direction.SOUTH).texture("#all").cullface(Direction.NORTH).end()
                                .face(Direction.WEST).texture("#all").cullface(Direction.EAST).end()
                                .face(Direction.EAST).texture("#all").cullface(Direction.WEST).end()
                                .end()
                                .texture("all", "buildcraftbuilders:blocks/frame/default")
                )
                .addModel()
                .end()

                .part()
                .modelFile(connection)
                .rotationX(270)
                .rotationY(0)
                .addModel()
                .condition(BuildCraftProperties.CONNECTED_UP, true)
                .end()

                .part()
                .modelFile(connection)
                .rotationX(90)
                .rotationY(0)
                .addModel()
                .condition(BuildCraftProperties.CONNECTED_DOWN, true)
                .end()

                .part()
                .modelFile(connection)
                .rotationX(0)
                .rotationY(90)
                .addModel()
                .condition(BuildCraftProperties.CONNECTED_EAST, true)
                .end()

                .part()
                .modelFile(connection)
                .rotationX(180)
                .rotationY(90)
                .addModel()
                .condition(BuildCraftProperties.CONNECTED_WEST, true)
                .end()

                .part()
                .modelFile(connection)
                .rotationX(0)
                .rotationY(0)
                .addModel()
                .condition(BuildCraftProperties.CONNECTED_NORTH, true)
                .end()

                .part()
                .modelFile(connection)
                .rotationX(180)
                .rotationY(0)
                .addModel()
                .condition(BuildCraftProperties.CONNECTED_SOUTH, true)
                .end()
        ;

        // quarry
        ResourceLocation quarry = BCBuildersBlocks.quarry.get().getRegistryName();
        ResourceLocation normal_top = new ResourceLocation(BCBuilders.MODID, "blocks/quarry/normal/top");
        ResourceLocation normal_bottom = new ResourceLocation(BCBuilders.MODID, "blocks/quarry/normal/bottom");
        ResourceLocation normal_side = new ResourceLocation(BCBuilders.MODID, "blocks/quarry/normal/side");
        ResourceLocation normal_front = new ResourceLocation(BCBuilders.MODID, "blocks/quarry/normal/front");
        ResourceLocation normal_back = new ResourceLocation(BCBuilders.MODID, "blocks/quarry/normal/back");

        ResourceLocation connected_top = new ResourceLocation(BCBuilders.MODID, "blocks/quarry/connected/top");
        ResourceLocation connected_bottom = new ResourceLocation(BCBuilders.MODID, "blocks/quarry/connected/bottom");
        ResourceLocation connected_side = new ResourceLocation(BCBuilders.MODID, "blocks/quarry/connected/side");
        ResourceLocation connected_front = new ResourceLocation(BCBuilders.MODID, "blocks/quarry/connected/front");
        ResourceLocation connected_back = new ResourceLocation(BCBuilders.MODID, "blocks/quarry/connected/back");
        getVariantBuilder(BCBuildersBlocks.quarry.get()).forAllStates(s ->
        {
            int rotY = switch (s.getValue(BlockBCBase_Neptune.PROP_FACING)) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                case NORTH -> 0;
                default -> throw new RuntimeException("Only Facing 4!");
            };
            boolean connected_up = s.getValue(BuildCraftProperties.CONNECTED_UP);
            boolean connected_down = s.getValue(BuildCraftProperties.CONNECTED_DOWN);
            boolean connected_east = s.getValue(BuildCraftProperties.CONNECTED_EAST);
            boolean connected_west = s.getValue(BuildCraftProperties.CONNECTED_WEST);
            boolean connected_north = s.getValue(BuildCraftProperties.CONNECTED_NORTH);
            boolean connected_south = s.getValue(BuildCraftProperties.CONNECTED_SOUTH);
            return ConfiguredModel.builder().modelFile(
                            models().withExistingParent(
                                            quarry.getNamespace() + ":block/" + quarry.getPath()
                                                    + "/" + connected_up + "_" + connected_down + "_" + connected_east + "_" + connected_west + "_" + connected_north + "_" + connected_south
                                            ,
                                            CUBE)
                                    .texture("particle", normal_side)
                                    .texture("up", connected_up ? connected_top : normal_top)
                                    .texture("down", connected_down ? connected_bottom : normal_bottom)
                                    .texture("east", connected_east ? connected_side : normal_side)
                                    .texture("west", connected_west ? connected_side : normal_side)
                                    .texture("north", connected_north ? connected_front : normal_front)
                                    .texture("south", connected_south ? connected_back : normal_back)
                    )
                    .rotationY(rotY)
                    .build();
        });

        // architect
        ResourceLocation architect_on = new ResourceLocation("buildcraftbuilders:architect_on");
        ResourceLocation architect_off = new ResourceLocation("buildcraftbuilders:architect_off");
        getVariantBuilder(BCBuildersBlocks.architect.get()).forAllStates(s ->
        {
            Direction direction = s.getValue(BlockBCBase_Neptune.PROP_FACING);
            int rotY = switch (direction) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                case NORTH -> 0;
                default -> throw new RuntimeException("Only Facing 4!");
            };
            boolean valid = s.getValue(BlockArchitectTable.PROP_VALID);
            if (valid) {
                return ConfiguredModel.builder().modelFile(
                                models().withExistingParent(architect_on.toString(), new ResourceLocation("minecraft", "block/orientable"))
                                        .texture("particle", "buildcraftbuilders:blocks/architect/back")
                                        .texture("down", "buildcraftbuilders:blocks/architect/bottom")
                                        .texture("up", "buildcraftbuilders:blocks/architect/top")
                                        .texture("north", "buildcraftbuilders:blocks/architect/front_on")
                                        .texture("east", "buildcraftbuilders:blocks/architect/left")
                                        .texture("south", "buildcraftbuilders:blocks/architect/back")
                                        .texture("west", "buildcraftbuilders:blocks/architect/right")
                        )
                        .rotationY(rotY)
                        .build();
            } else {
                return ConfiguredModel.builder().modelFile(
                                models().withExistingParent(architect_off.toString(), new ResourceLocation("minecraft", "block/orientable"))
                                        .texture("particle", "buildcraftbuilders:blocks/architect/back")
                                        .texture("down", "buildcraftbuilders:blocks/architect/bottom")
                                        .texture("up", "buildcraftbuilders:blocks/architect/top")
                                        .texture("north", "buildcraftbuilders:blocks/architect/front_off")
                                        .texture("east", "buildcraftbuilders:blocks/architect/left")
                                        .texture("south", "buildcraftbuilders:blocks/architect/back")
                                        .texture("west", "buildcraftbuilders:blocks/architect/right")
                        )
                        .rotationY(rotY)
                        .build();
            }
        });

        // filler
        simple4FacingBlock(
                BCBuildersBlocks.filler.get(),
                90,
                180,
                270,
                0,
                models().withExistingParent("buildcraftbuilders:block/filler/main", CUBE)
                        .texture("particle", "buildcraftbuilders:blocks/filler/side")
                        .texture("down", "buildcraftbuilders:blocks/filler/bottom")
                        .texture("up", "buildcraftbuilders:blocks/filler/top")
                        .texture("north", "buildcraftbuilders:blocks/filler/front")
                        .texture("east", "buildcraftbuilders:blocks/filler/side")
                        .texture("south", "buildcraftbuilders:blocks/filler/side")
                        .texture("west", "buildcraftbuilders:blocks/filler/side")
        );

        // library
        simple4FacingBlock(
                BCBuildersBlocks.library.get(),
                90,
                180,
                270,
                0,
                models().withExistingParent(BCBuildersBlocks.library.get().getRegistryName().toString(), CUBE)
                        .transforms()
                        .transform(ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
                        .rotation(0, 135, 0)
                        .translation(0, 0, 0)
                        .scale(0.40F, 0.40F, 0.40F)
                        .end()
                        .end()
                        .texture("particle", "buildcraftbuilders:blocks/library/back")
                        .texture("down", "buildcraftbuilders:blocks/library/bottom")
                        .texture("up", "buildcraftbuilders:blocks/library/top")
                        .texture("north", "buildcraftbuilders:blocks/library/front")
                        .texture("east", "buildcraftbuilders:blocks/library/left")
                        .texture("south", "buildcraftbuilders:blocks/library/back")
                        .texture("west", "buildcraftbuilders:blocks/library/right")
        );

        // replacer
        simple4FacingBlock(
                BCBuildersBlocks.replacer.get(),
                90,
                180,
                270,
                0,
                models().withExistingParent(BCBuildersBlocks.replacer.get().getRegistryName().toString(), CUBE)
                        .texture("particle", "buildcraftbuilders:blocks/replacer/side")
                        .texture("down", "buildcraftbuilders:blocks/replacer/bottom")
                        .texture("up", "buildcraftbuilders:blocks/replacer/top")
                        .texture("north", "buildcraftbuilders:blocks/replacer/front")
                        .texture("east", "buildcraftbuilders:blocks/replacer/side")
                        .texture("south", "buildcraftbuilders:blocks/replacer/side")
                        .texture("west", "buildcraftbuilders:blocks/replacer/side")
        );

        // markerConstruction
        ModelBuilder markerConstruction = models().getBuilder("buildcraftbuilders:block/marker_construction")
                .parent(CoreBlockStateGenerator.torch_center_lit)
                .texture("all", "buildcraftbuilders:blocks/marker_construction");
        getMultipartBuilder(BCBuildersBlocks.markerConstruction.get())
                .part()
                .modelFile(markerConstruction)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.UP)
                .end()
                .part()
                .modelFile(markerConstruction)
                .rotationX(180)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.DOWN)
                .end()
                .part()
                .modelFile(markerConstruction)
                .rotationX(90)
                .rotationY(90)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.EAST)
                .end()
                .part()
                .modelFile(markerConstruction)
                .rotationX(90)
                .rotationY(180)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.SOUTH)
                .end()
                .part()
                .modelFile(markerConstruction)
                .rotationX(90)
                .rotationY(270)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.WEST)
                .end()
                .part()
                .modelFile(markerConstruction)
                .rotationX(90)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.NORTH)
                .end()
        ;
    }

    @NotNull
    @Override
    public String getName() {
        return "BuildCraft Builders BlockState Generator";
    }
}

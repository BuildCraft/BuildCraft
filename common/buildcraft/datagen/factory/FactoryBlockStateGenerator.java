package buildcraft.datagen.factory;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.datagen.base.BCBaseBlockStateGenerator;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockTank;
import buildcraft.factory.block.BlockWaterGel;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class FactoryBlockStateGenerator extends BCBaseBlockStateGenerator {
    public FactoryBlockStateGenerator(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
        super(gen, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // heatExchange
        getVariantBuilder(BCFactoryBlocks.heatExchange.get()).forAllStates(state -> new ConfiguredModel[] { new ConfiguredModel(BUILTIN_ENTITY_MODEL) });

        // pump
        getVariantBuilder(BCFactoryBlocks.pump.get())
                .forAllStates(s ->
                        ConfiguredModel.builder().modelFile(
                                        models().withExistingParent("buildcraftfactory:block/pump", CUBE)
                                                .texture("particle", "buildcraftfactory:blocks/pump/side")
                                                .texture("down", "buildcraftfactory:blocks/pump/bottom")
                                                .texture("up", "buildcraftfactory:blocks/pump/top")
                                                .texture("north", "buildcraftfactory:blocks/pump/side")
                                                .texture("east", "buildcraftfactory:blocks/pump/side")
                                                .texture("south", "buildcraftfactory:blocks/pump/side")
                                                .texture("west", "buildcraftfactory:blocks/pump/side")
                                )
                                .build()
                )
        ;

        // tank
        BlockModelBuilder tank = models().getBuilder("buildcraftfactory:block/tank")
                .element()
                .from(2, 0, 2)
                .to(14, 16, 14)
                .face(Direction.DOWN).texture("#down").cullface(Direction.DOWN).end()
                .face(Direction.UP).texture("#up").cullface(Direction.UP).end()
                .face(Direction.NORTH).texture("#side").end()
                .face(Direction.SOUTH).texture("#side").end()
                .face(Direction.WEST).texture("#side").end()
                .face(Direction.EAST).texture("#side").end()
                .end()
                .texture("particle", "buildcraftfactory:blocks/tank/side")
                .texture("up", "buildcraftfactory:blocks/tank/end")
                .texture("down", "buildcraftfactory:blocks/tank/end")
                .texture("side", "buildcraftfactory:blocks/tank/side");
        BlockModelBuilder tank_joined_below = models().getBuilder("buildcraftfactory:block/tank_joined_below")
                .parent(tank)
                .texture("particle", "buildcraftfactory:blocks/tank/side_joined_below")
                .texture("side", "buildcraftfactory:blocks/tank/side_joined_below");
        getVariantBuilder(BCFactoryBlocks.tank.get())
                .forAllStates(s ->
                        ConfiguredModel.builder().modelFile(
                                        s.getValue(BlockTank.JOINED_BELOW) ?
                                                tank_joined_below
                                                :
                                                tank
                                )
                                .build()
                )
        ;

        // auto_workbench_item
        getVariantBuilder(BCFactoryBlocks.autoWorkbenchItems.get()).forAllStates(s ->
                ConfiguredModel.builder().modelFile(
                                models().withExistingParent(BCFactoryBlocks.autoWorkbenchItems.get().getRegistryName().toString(), CUBE_ALL)
                                        .texture("particle", "buildcraftfactory:blocks/auto_workbench_item/side")
                                        .texture("up", "buildcraftfactory:blocks/auto_workbench_item/top")
                                        .texture("down", "buildcraftfactory:blocks/auto_workbench_item/top")
                                        .texture("north", "buildcraftfactory:blocks/auto_workbench_item/side")
                                        .texture("east", "buildcraftfactory:blocks/auto_workbench_item/side")
                                        .texture("south", "buildcraftfactory:blocks/auto_workbench_item/side")
                                        .texture("west", "buildcraftfactory:blocks/auto_workbench_item/side")
                        )
                        .build()
        );

        // chute
        ModelBuilder chute = models().getBuilder("buildcraftfactory:block/chute")
                // Top box
                .element()
                .from(0, 9, 0)
                .to(16, 16, 16)
                .face(Direction.DOWN).uvs(0, 0, 16, 16).texture("#top_bottom").end()
                .face(Direction.UP).uvs(0, 0, 16, 16).texture("#top").end()
                .face(Direction.NORTH).uvs(0, 0, 16, 7).texture("#top_side").end()
                .face(Direction.SOUTH).uvs(0, 0, 16, 7).texture("#top_side").end()
                .face(Direction.WEST).uvs(0, 0, 16, 7).texture("#top_side").end()
                .face(Direction.EAST).uvs(0, 0, 16, 7).texture("#top_side").end()
                .end()
                // Middle box
                .element()
                .from(1, 8, 1)
                .to(15, 9, 15)
                .face(Direction.DOWN).uvs(1, 1, 15, 15).texture("#side2").end()
                .face(Direction.UP).uvs(1, 1, 15, 15).texture("#side2").end()
                .face(Direction.NORTH).uvs(1, 0, 15, 1).texture("#side").end()
                .face(Direction.SOUTH).uvs(1, 0, 15, 1).texture("#side").end()
                .face(Direction.WEST).uvs(1, 0, 15, 1).texture("#side").end()
                .face(Direction.EAST).uvs(1, 0, 15, 1).texture("#side").end()
                .end()
                // Middle box
                .element()
                .from(2, 7, 2)
                .to(14, 8, 14)
                .face(Direction.DOWN).uvs(2, 2, 14, 14).texture("#side2").end()
                .face(Direction.UP).uvs(2, 2, 14, 14).texture("#side2").end()
                .face(Direction.NORTH).uvs(2, 1, 14, 2).texture("#side").end()
                .face(Direction.SOUTH).uvs(2, 1, 14, 2).texture("#side").end()
                .face(Direction.WEST).uvs(2, 1, 14, 2).texture("#side").end()
                .face(Direction.EAST).uvs(2, 1, 14, 2).texture("#side").end()
                .end()
                // Middle box
                .element()
                .from(3, 6, 3)
                .to(13, 7, 13)
                .face(Direction.DOWN).uvs(3, 3, 13, 13).texture("#side2").end()
                .face(Direction.UP).uvs(3, 3, 13, 13).texture("#side2").end()
                .face(Direction.NORTH).uvs(3, 2, 13, 3).texture("#side").end()
                .face(Direction.SOUTH).uvs(3, 2, 13, 3).texture("#side").end()
                .face(Direction.WEST).uvs(3, 2, 13, 3).texture("#side").end()
                .face(Direction.EAST).uvs(3, 2, 13, 3).texture("#side").end()
                .end()
                // Middle box
                .element()
                .from(4, 5, 4)
                .to(12, 6, 12)
                .face(Direction.DOWN).uvs(4, 4, 12, 12).texture("#side2").end()
                .face(Direction.UP).uvs(4, 4, 12, 12).texture("#side2").end()
                .face(Direction.NORTH).uvs(4, 3, 12, 4).texture("#side").end()
                .face(Direction.SOUTH).uvs(4, 3, 12, 4).texture("#side").end()
                .face(Direction.WEST).uvs(4, 3, 12, 4).texture("#side").end()
                .face(Direction.EAST).uvs(4, 3, 12, 4).texture("#side").end()
                .end()
                // Middle box
                .element()
                .from(5, 4, 5)
                .to(11, 5, 11)
                .face(Direction.DOWN).uvs(5, 5, 11, 11).texture("#side2").end()
                .face(Direction.UP).uvs(5, 5, 11, 11).texture("#side2").end()
                .face(Direction.NORTH).uvs(5, 4, 11, 5).texture("#side").end()
                .face(Direction.SOUTH).uvs(5, 4, 11, 5).texture("#side").end()
                .face(Direction.WEST).uvs(5, 4, 11, 5).texture("#side").end()
                .face(Direction.EAST).uvs(5, 4, 11, 5).texture("#side").end()
                .end()
                // Middle box
                .element()
                .from(6, 3, 6)
                .to(10, 4, 10)
                .face(Direction.DOWN).uvs(6, 6, 10, 10).texture("#side2").end()
                .face(Direction.UP).uvs(6, 6, 10, 10).texture("#side2").end()
                .face(Direction.NORTH).uvs(6, 5, 10, 6).texture("#side").end()
                .face(Direction.SOUTH).uvs(6, 5, 10, 6).texture("#side").end()
                .face(Direction.WEST).uvs(6, 5, 10, 6).texture("#side").end()
                .face(Direction.EAST).uvs(6, 5, 10, 6).texture("#side").end()
                .end()
                .texture("particle", "buildcraftfactory:blocks/chute/top")
                .texture("top", "buildcraftfactory:blocks/chute/top")
                .texture("top_bottom", "buildcraftfactory:blocks/chute/top_bottom")
                .texture("top_side", "buildcraftfactory:blocks/chute/top_side")
                .texture("bottom", "buildcraftfactory:blocks/chute/bottom")
                .texture("side", "buildcraftfactory:blocks/chute/side")
                .texture("side2", "buildcraftfactory:blocks/chute/side2");
        ModelBuilder chute_connected = models().getBuilder("buildcraftfactory:block/chute_connected")
                // Bottom box
                .element()
                .from(5, 0, 5)
                .to(11, 3, 11)
                .face(Direction.DOWN).uvs(0, 0, 6, 6).texture("#bottom").end()
                .face(Direction.UP).uvs(0, 0, 6, 6).texture("#bottom").end()
                .face(Direction.NORTH).uvs(10, 13, 16, 16).texture("#bottom").end()
                .face(Direction.SOUTH).uvs(10, 13, 16, 16).texture("#bottom").end()
                .face(Direction.WEST).uvs(10, 13, 16, 16).texture("#bottom").end()
                .face(Direction.EAST).uvs(10, 13, 16, 16).texture("#bottom").end()
                .end()
                .texture("particle", "buildcraftfactory:blocks/chute/top")
                .texture("top", "buildcraftfactory:blocks/chute/top")
                .texture("top_bottom", "buildcraftfactory:blocks/chute/top_bottom")
                .texture("top_side", "buildcraftfactory:blocks/chute/top_side")
                .texture("bottom", "buildcraftfactory:blocks/chute/bottom")
                .texture("side", "buildcraftfactory:blocks/chute/side")
                .texture("side2", "buildcraftfactory:blocks/chute/side2");
        getMultipartBuilder(BCFactoryBlocks.chute.get())
                .part()
                .modelFile(chute)
                .rotationX(0)
                .rotationY(0)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.UP)
                .end()
                .part()
                .modelFile(chute)
                .rotationX(180)
                .rotationY(0)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.DOWN)
                .end()
                .part()
                .modelFile(chute)
                .rotationX(90)
                .rotationY(90)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.EAST)
                .end()
                .part()
                .modelFile(chute)
                .rotationX(90)
                .rotationY(180)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.SOUTH)
                .end()
                .part()
                .modelFile(chute)
                .rotationX(90)
                .rotationY(270)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.WEST)
                .end()
                .part()
                .modelFile(chute)
                .rotationX(90)
                .rotationY(0)
                .addModel()
                .condition(BuildCraftProperties.BLOCK_FACING_6, Direction.NORTH)
                .end()

                .part()
                .modelFile(chute_connected)
                .rotationX(180)
                .rotationY(0)
                .addModel()
                .condition(BuildCraftProperties.CONNECTED_UP, true)
                .end()
                .part()
                .modelFile(chute_connected)
                .rotationX(0)
                .rotationY(0)
                .addModel()
                .condition(BuildCraftProperties.CONNECTED_DOWN, true)
                .end()
                .part()
                .modelFile(chute_connected)
                .rotationX(90)
                .rotationY(270)
                .addModel()
                .condition(BuildCraftProperties.CONNECTED_EAST, true)
                .end()
                .part()
                .modelFile(chute_connected)
                .rotationX(90)
                .rotationY(0)
                .addModel()
                .condition(BuildCraftProperties.CONNECTED_SOUTH, true)
                .end()
                .part()
                .modelFile(chute_connected)
                .rotationX(90)
                .rotationY(90)
                .addModel()
                .condition(BuildCraftProperties.CONNECTED_WEST, true)
                .end()
                .part()
                .modelFile(chute_connected)
                .rotationX(90)
                .rotationY(180)
                .addModel()
                .condition(BuildCraftProperties.CONNECTED_NORTH, true)
                .end()
        ;

        // waterGel
        ResourceLocation waterGel = BCFactoryBlocks.waterGel.get().getRegistryName();
        getVariantBuilder(BCFactoryBlocks.waterGel.get()).forAllStates(state ->
                {
                    ResourceLocation tex;
                    switch (state.getValue(BlockWaterGel.PROP_STAGE)) {
                        case SPREAD_0:
                            tex = new ResourceLocation("buildcraftfactory:blocks/gel/spread_0");
                            break;
                        case SPREAD_1:
                            tex = new ResourceLocation("buildcraftfactory:blocks/gel/spread_1");
                            break;
                        case SPREAD_2:
                            tex = new ResourceLocation("buildcraftfactory:blocks/gel/spread_2");
                            break;
                        case SPREAD_3:
                            tex = new ResourceLocation("buildcraftfactory:blocks/gel/spread_3");
                            break;
                        case GELLING_0:
                            tex = new ResourceLocation("buildcraftfactory:blocks/gel/gelling_0");
                            break;
                        case GELLING_1:
                            tex = new ResourceLocation("buildcraftfactory:blocks/gel/gelling_1");
                            break;
                        case GEL:
                            tex = new ResourceLocation("buildcraftfactory:blocks/gel/gel");
                            break;
                        default:
                            throw new RuntimeException("Unexpected GelStage: [" + state.getValue(BlockWaterGel.PROP_STAGE) + "]");
                    }
                    return ConfiguredModel.builder().modelFile(
                                    models().withExistingParent(waterGel.getNamespace() + ":block/" + waterGel.getPath() + "/" + state.getValue(BlockWaterGel.PROP_STAGE).name().toLowerCase(), CUBE_ALL)
                                            .texture(
                                                    "all",
                                                    tex
                                            )
                            )
                            .build();
                }
        );

        // flood_gate
        ResourceLocation floodGate = BCFactoryBlocks.floodGate.get().getRegistryName();
        ResourceLocation floodGate_top = new ResourceLocation("buildcraftfactory:blocks/flood_gate/top");
        ResourceLocation floodGate_closed = new ResourceLocation("buildcraftfactory:blocks/flood_gate/closed");
        ResourceLocation floodGate_open = new ResourceLocation("buildcraftfactory:blocks/flood_gate/open");
        getVariantBuilder(BCFactoryBlocks.floodGate.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(
                                models().withExistingParent(
                                                floodGate.getNamespace() + ":block/" + floodGate.getPath()
                                                        + "/" + state.getValue(BuildCraftProperties.CONNECTED_DOWN)
                                                        + "_" + state.getValue(BuildCraftProperties.CONNECTED_NORTH)
                                                        + "_" + state.getValue(BuildCraftProperties.CONNECTED_SOUTH)
                                                        + "_" + state.getValue(BuildCraftProperties.CONNECTED_EAST)
                                                        + "_" + state.getValue(BuildCraftProperties.CONNECTED_WEST)
                                                ,
                                                new ResourceLocation("buildcraftcore:block/default_cube")
                                        )
                                        .texture("particle", floodGate_top)
                                        .texture("up", floodGate_top)
                                        .texture("down", state.getValue(BuildCraftProperties.CONNECTED_DOWN) ? floodGate_open : floodGate_closed)
                                        .texture("north", state.getValue(BuildCraftProperties.CONNECTED_NORTH) ? floodGate_open : floodGate_closed)
                                        .texture("south", state.getValue(BuildCraftProperties.CONNECTED_SOUTH) ? floodGate_open : floodGate_closed)
                                        .texture("east", state.getValue(BuildCraftProperties.CONNECTED_EAST) ? floodGate_open : floodGate_closed)
                                        .texture("west", state.getValue(BuildCraftProperties.CONNECTED_WEST) ? floodGate_open : floodGate_closed)
                        )
                        .build()
        );

        // miningWell
        simple4FacingBlock(
                BCFactoryBlocks.miningWell.get(),
                90,
                180,
                270,
                0,
                models().withExistingParent(BCFactoryBlocks.miningWell.get().getRegistryName().toString(), CUBE)
                        .texture("particle", "buildcraftfactory:blocks/mining_well/side")
                        .texture("down", "buildcraftfactory:blocks/mining_well/bottom")
                        .texture("up", "buildcraftfactory:blocks/mining_well/top")
                        .texture("north", "buildcraftfactory:blocks/mining_well/front")
                        .texture("east", "buildcraftfactory:blocks/mining_well/side")
                        .texture("south", "buildcraftfactory:blocks/mining_well/back")
                        .texture("west", "buildcraftfactory:blocks/mining_well/side")
        );

        // Tube
        builtinEntity(BCFactoryBlocks.tube.get(), "buildcraftfactory:blocks/pump/tube");

        // distiller
        simple4FacingBlock(
                BCFactoryBlocks.distiller.get(),
                180,
                270,
                0,
                90,
                models().getBuilder("buildcraftfactory:block/distiller")
                        .texture("particle", "buildcraftfactory:blocks/distiller/tank_sprite_a")
                        // Side Tank
                        .element()
                        .from(0, 0, 4)
                        .to(8, 16, 12)
                        .face(Direction.UP).uvs(8, 0, 16, 8).texture("#sprite_a").end()
                        .face(Direction.DOWN).uvs(8, 0, 16, 8).texture("#sprite_a").end()
                        .face(Direction.NORTH).uvs(0, 0, 8, 16).texture("#sprite_a").end()
                        .face(Direction.SOUTH).uvs(0, 0, 8, 16).texture("#sprite_a").end()
                        .face(Direction.EAST).uvs(0, 0, 8, 16).texture("#sprite_a").end()
                        .face(Direction.WEST).uvs(0, 0, 8, 16).texture("#sprite_a").end()
                        .end()
                        // Bottom Tank
                        .element()
                        .from(8, 0, 0)
                        .to(16, 8, 16)
                        .face(Direction.UP).uvs(0, 0, 16, 8).texture("#sprite_b").rotation(ModelBuilder.FaceRotation.CLOCKWISE_90).end()
                        .face(Direction.DOWN).uvs(0, 0, 16, 8).texture("#sprite_b").rotation(ModelBuilder.FaceRotation.CLOCKWISE_90).end()
                        .face(Direction.NORTH).uvs(8, 8, 16, 16).texture("#sprite_a").end()
                        .face(Direction.SOUTH).uvs(8, 8, 16, 16).texture("#sprite_a").end()
                        .face(Direction.EAST).uvs(0, 8, 16, 16).texture("#sprite_b").end()
                        .face(Direction.WEST).uvs(0, 8, 16, 16).texture("#sprite_b").end()
                        .end()
                        // Top Tank
                        .element()
                        .from(8, 8, 0)
                        .to(16, 16, 16)
                        .face(Direction.UP).uvs(0, 0, 16, 8).texture("#sprite_b").rotation(ModelBuilder.FaceRotation.CLOCKWISE_90).end()
                        .face(Direction.DOWN).uvs(0, 0, 16, 8).texture("#sprite_b").rotation(ModelBuilder.FaceRotation.CLOCKWISE_90).end()
                        .face(Direction.NORTH).uvs(8, 8, 16, 16).texture("#sprite_a").end()
                        .face(Direction.SOUTH).uvs(8, 8, 16, 16).texture("#sprite_a").end()
                        .face(Direction.EAST).uvs(0, 8, 16, 16).texture("#sprite_b").end()
                        .face(Direction.WEST).uvs(0, 8, 16, 16).texture("#sprite_b").end()
                        .end()
                        .texture("sprite_a", "buildcraftfactory:blocks/distiller/tank_sprite_a")
                        .texture("sprite_b", "buildcraftfactory:blocks/distiller/tank_sprite_b")

        );
    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Factory BlockState Generator";
    }
}

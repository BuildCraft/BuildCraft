package buildcraft.datagen.factory;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.datagen.base.BCBaseBlockStateGenerator;
import buildcraft.factory.BCFactory;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockTank;
import buildcraft.factory.block.BlockWaterGel;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class FactoryBlockStateGenerator extends BCBaseBlockStateGenerator {
    public FactoryBlockStateGenerator(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BCFactory.MODID, exFileHelper);
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
                                                .texture("particle", "buildcraftfactory:block/pump/side")
                                                .texture("down", "buildcraftfactory:block/pump/bottom")
                                                .texture("up", "buildcraftfactory:block/pump/top")
                                                .texture("north", "buildcraftfactory:block/pump/side")
                                                .texture("east", "buildcraftfactory:block/pump/side")
                                                .texture("south", "buildcraftfactory:block/pump/side")
                                                .texture("west", "buildcraftfactory:block/pump/side")
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
                .texture("particle", "buildcraftfactory:block/tank/side")
                .texture("up", "buildcraftfactory:block/tank/end")
                .texture("down", "buildcraftfactory:block/tank/end")
                .texture("side", "buildcraftfactory:block/tank/side");
        BlockModelBuilder tank_joined_below = models().getBuilder("buildcraftfactory:block/tank_joined_below")
                .parent(tank)
                .texture("particle", "buildcraftfactory:block/tank/side_joined_below")
                .texture("side", "buildcraftfactory:block/tank/side_joined_below");
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
                                        .texture("particle", "buildcraftfactory:block/auto_workbench_item/side")
                                        .texture("up", "buildcraftfactory:block/auto_workbench_item/top")
                                        .texture("down", "buildcraftfactory:block/auto_workbench_item/top")
                                        .texture("north", "buildcraftfactory:block/auto_workbench_item/side")
                                        .texture("east", "buildcraftfactory:block/auto_workbench_item/side")
                                        .texture("south", "buildcraftfactory:block/auto_workbench_item/side")
                                        .texture("west", "buildcraftfactory:block/auto_workbench_item/side")
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
                .texture("particle", "buildcraftfactory:block/chute/top")
                .texture("top", "buildcraftfactory:block/chute/top")
                .texture("top_bottom", "buildcraftfactory:block/chute/top_bottom")
                .texture("top_side", "buildcraftfactory:block/chute/top_side")
                .texture("bottom", "buildcraftfactory:block/chute/bottom")
                .texture("side", "buildcraftfactory:block/chute/side")
                .texture("side2", "buildcraftfactory:block/chute/side2");
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
                .texture("particle", "buildcraftfactory:block/chute/top")
                .texture("top", "buildcraftfactory:block/chute/top")
                .texture("top_bottom", "buildcraftfactory:block/chute/top_bottom")
                .texture("top_side", "buildcraftfactory:block/chute/top_side")
                .texture("bottom", "buildcraftfactory:block/chute/bottom")
                .texture("side", "buildcraftfactory:block/chute/side")
                .texture("side2", "buildcraftfactory:block/chute/side2");
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
                ConfiguredModel.builder().modelFile(
                                models().withExistingParent(waterGel.getNamespace() + ":block/" + waterGel.getPath() + "/" + state.getValue(BlockWaterGel.PROP_STAGE).name().toLowerCase(), CUBE_ALL)
                                        .texture(
                                                "all",
                                                switch (state.getValue(BlockWaterGel.PROP_STAGE)) {
                                                    case SPREAD_0 ->
                                                            new ResourceLocation("buildcraftfactory:block/gel/spread_0");
                                                    case SPREAD_1 ->
                                                            new ResourceLocation("buildcraftfactory:block/gel/spread_1");
                                                    case SPREAD_2 ->
                                                            new ResourceLocation("buildcraftfactory:block/gel/spread_2");
                                                    case SPREAD_3 ->
                                                            new ResourceLocation("buildcraftfactory:block/gel/spread_3");
                                                    case GELLING_0 ->
                                                            new ResourceLocation("buildcraftfactory:block/gel/gelling_0");
                                                    case GELLING_1 ->
                                                            new ResourceLocation("buildcraftfactory:block/gel/gelling_1");
                                                    case GEL -> new ResourceLocation("buildcraftfactory:block/gel/gel");
                                                }
                                        )
                        )
                        .build()
        );

        // flood_gate
        ResourceLocation floodGate = BCFactoryBlocks.floodGate.get().getRegistryName();
        ResourceLocation floodGate_top = new ResourceLocation("buildcraftfactory:block/flood_gate/top");
        ResourceLocation floodGate_closed = new ResourceLocation("buildcraftfactory:block/flood_gate/closed");
        ResourceLocation floodGate_open = new ResourceLocation("buildcraftfactory:block/flood_gate/open");
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
                        .texture("particle", "buildcraftfactory:block/mining_well/side")
                        .texture("down", "buildcraftfactory:block/mining_well/bottom")
                        .texture("up", "buildcraftfactory:block/mining_well/top")
                        .texture("north", "buildcraftfactory:block/mining_well/front")
                        .texture("east", "buildcraftfactory:block/mining_well/side")
                        .texture("south", "buildcraftfactory:block/mining_well/back")
                        .texture("west", "buildcraftfactory:block/mining_well/side")
        );

        // Tube
        builtinEntity(BCFactoryBlocks.tube.get(), "buildcraftfactory:block/pump/tube");

        // distiller
        simple4FacingBlock(
                BCFactoryBlocks.distiller.get(),
                180,
                270,
                0,
                90,
                models().getBuilder("buildcraftfactory:block/distiller")
                        .texture("particle", "buildcraftfactory:block/distiller/tank_sprite_a")
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
                        .texture("sprite_a", "buildcraftfactory:block/distiller/tank_sprite_a")
                        .texture("sprite_b", "buildcraftfactory:block/distiller/tank_sprite_b")

        );
    }

    @NotNull
    @Override
    public String getName() {
        return "BuildCraft Factory BlockState Generator";
    }
}

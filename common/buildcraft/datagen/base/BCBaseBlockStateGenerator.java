package buildcraft.datagen.base;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.block.BlockBCBase_Neptune;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Arrays;

public abstract class BCBaseBlockStateGenerator extends BlockStateProvider {
    protected static final ResourceLocation CUBE = new ResourceLocation("minecraft", "block/cube");
    protected static final ResourceLocation CUBE_ALL = new ResourceLocation("minecraft", "block/cube_all");
    protected static final ResourceLocation BLOCK = new ResourceLocation("minecraft", "block/block");
    private static final ResourceLocation BUILTIN_ENTITY_LOCATION = new ResourceLocation("minecraft", "builtin/entity");
    protected static final ModelFile BUILTIN_ENTITY_MODEL = new ModelFile.UncheckedModelFile(BUILTIN_ENTITY_LOCATION);

    public BCBaseBlockStateGenerator(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
        super(gen, modid, exFileHelper);
    }

    public void builtinEntity(Block b) {
        // Calen: set this in blockstate json to avoid the model loaded by mc
        simpleBlock(b, new ConfiguredModel(BUILTIN_ENTITY_MODEL));
    }

    public void builtinEntity(Block b, String particle) {
        models().existingFileHelper.trackGenerated(BUILTIN_ENTITY_LOCATION, ResourcePackType.CLIENT_RESOURCES, ".json", "models");
        // Calen: set this in blockstate json to avoid the model loaded by mc
        simpleBlock(b, ConfiguredModel.builder().modelFile(
                        models().withExistingParent(b.getRegistryName().toString(), BUILTIN_ENTITY_LOCATION)
                                .texture("particle", particle)
                )
                .build());
    }

    public void simple4FacingBlock(Block b, int east, int south, int west, int north, ModelFile model) {
        getVariantBuilder(b).forAllStates(s ->
        {
            int rotY;
            switch (s.getValue(BlockBCBase_Neptune.PROP_FACING)) {
                case EAST:
                    rotY = east;
                    break;
                case SOUTH:
                    rotY = south;
                    break;
                case WEST:
                    rotY = west;
                    break;
                case NORTH:
                    rotY = north;
                    break;
                default:
                    throw new RuntimeException("Only Facing 4!");
            }
            return ConfiguredModel.builder().modelFile(
                            model
                    )
                    .rotationY(rotY)
                    .build();
        });
    }

    public <T extends Comparable<T>> void add4Facing(MultiPartBlockStateBuilder builder, ModelFile model, Property<T> property, T value) {
        Arrays.stream(Direction.BY_2D_DATA).forEach(direction ->
                {
                    if (property == null || value == null) {
                        builder
                                .part()
                                .modelFile(model)
                                .rotationY((int) direction.getOpposite().toYRot())
                                .addModel()
                                .condition(BuildCraftProperties.BLOCK_FACING, direction)
                                .end();
                    } else {
                        builder
                                .part()
                                .modelFile(model)
                                .rotationY((int) direction.getOpposite().toYRot())
                                .addModel()
                                .condition(BuildCraftProperties.BLOCK_FACING, direction)
                                .condition(property, value)
                                .end();
                    }
                }
        );
    }
}

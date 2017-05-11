package buildcraft.builders.snapshot;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.schematics.SchematicBlockFactory;
import buildcraft.api.schematics.SchematicBlockFactoryRegistry;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class SchematicBlockManager {
    public static ISchematicBlock<?> getSchematicBlock(SchematicBlockContext context) {
        for (SchematicBlockFactory<?> schematicBlockFactory : Lists.reverse(SchematicBlockFactoryRegistry.getFactories())) {
            if (schematicBlockFactory.predicate.test(context)) {
                ISchematicBlock<?> schematicBlock = schematicBlockFactory.supplier.get();
                schematicBlock.init(context);
                return schematicBlock;
            }
        }
        throw new UnsupportedOperationException();
    }

    public static ISchematicBlock<?> getSchematicBlock(World world,
                                                       BlockPos basePos,
                                                       BlockPos pos,
                                                       IBlockState blockState,
                                                       Block block) {
        SchematicBlockContext context = new SchematicBlockContext(
                world,
                basePos,
                pos,
                blockState,
                block
        );
        return getSchematicBlock(context);
    }

    public static void computeRequired(Blueprint blueprint) {
        FakeWorld world = FakeWorld.INSTANCE;
        world.uploadBlueprint(blueprint, true);
        world.editable = false;
        for (int z = 0; z < blueprint.size.getZ(); z++) {
            for (int y = 0; y < blueprint.size.getY(); y++) {
                for (int x = 0; x < blueprint.size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z).add(FakeWorld.BLUEPRINT_OFFSET);
                    ISchematicBlock<?> schematicBlock = blueprint.data
                            [pos.getX() - FakeWorld.BLUEPRINT_OFFSET.getX()]
                            [pos.getY() - FakeWorld.BLUEPRINT_OFFSET.getY()]
                            [pos.getZ() - FakeWorld.BLUEPRINT_OFFSET.getZ()];
                    IBlockState blockState = world.getBlockState(pos);
                    Block block = blockState.getBlock();
                    schematicBlock.computeRequiredItemsAndFluids(
                            new SchematicBlockContext(
                                    world,
                                    FakeWorld.BLUEPRINT_OFFSET,
                                    pos,
                                    blockState,
                                    block
                            )
                    );
                }
            }
        }
        world.editable = true;
        world.clear();
    }

    @Nonnull
    public static NBTTagCompound writeToNBT(ISchematicBlock<?> schematicBlock) {
        NBTTagCompound schematicBlockTag = new NBTTagCompound();
        schematicBlockTag.setString(
                "name",
                SchematicBlockFactoryRegistry
                        .getFactoryByInstance(schematicBlock)
                        .name
                        .toString()
        );
        schematicBlockTag.setTag("data", schematicBlock.serializeNBT());
        return schematicBlockTag;
    }

    @Nonnull
    public static ISchematicBlock<?> readFromNBT(NBTTagCompound schematicBlockTag) {
        ISchematicBlock<?> schematicBlock = SchematicBlockFactoryRegistry
                .getFactoryByName(new ResourceLocation(schematicBlockTag.getString("name")))
                .supplier
                .get();
        schematicBlock.deserializeNBT(schematicBlockTag.getCompoundTag("data"));
        return schematicBlock;
    }
}

package ct.buildcraft.builders;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.api.schematics.SchematicBlockContext;
import ct.buildcraft.api.schematics.SchematicBlockFactoryRegistry;
import ct.buildcraft.api.schematics.SchematicEntityFactoryRegistry;
import ct.buildcraft.builders.snapshot.SchematicBlockAir;
import ct.buildcraft.builders.snapshot.SchematicBlockDefault;
import ct.buildcraft.builders.snapshot.SchematicBlockFluid;
import ct.buildcraft.builders.snapshot.SchematicEntityDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BCBuildersSchematics {
    public static void preInit() {
        registerSchematicFactory("air", 0, SchematicBlockAir::predicate, SchematicBlockAir::new);
        registerSchematicFactory("default", 100, SchematicBlockDefault::predicate, SchematicBlockDefault::new);
        registerSchematicFactory("fluid", 200, SchematicBlockFluid::predicate, SchematicBlockFluid::new);

        registerSchematicFactory("banner", 300, c -> c.block instanceof BannerBlock, BCBuildersSchematics::getBanner);
        registerSchematicFactory("vine", 300, c -> c.block instanceof VineBlock, BCBuildersSchematics::getVine);

        SchematicEntityFactoryRegistry.registerFactory("default", 100, SchematicEntityDefault::predicate,
            SchematicEntityDefault::new);
    }

    private static <S extends ISchematicBlock> void registerSchematicFactory(String name, int priority,
        Predicate<SchematicBlockContext> predicate, Supplier<S> supplier) {
        SchematicBlockFactoryRegistry.registerFactory(name, priority, predicate, supplier);
    }

    private static SchematicBlockDefault getBanner() {
        return new SchematicBlockDefault() {
            @Nonnull
            @Override
            public List<ItemStack> computeRequiredItems(Level level) {
            	ItemStack itemstack = new ItemStack(BannerBlock.byColor(DyeColor.byId(tileNbt.getInt("Base"))));
            	ListTag pattern = tileNbt.getList("Patterns", 10);
            	if (pattern != null && !pattern.isEmpty()) {
                    CompoundTag compoundtag = new CompoundTag();
                    compoundtag.put("Patterns", pattern);
                    BlockItem.setBlockEntityData(itemstack, BlockEntityType.BANNER, compoundtag);
            	}
                return Collections.singletonList(itemstack);
            }
        };
    }

    private static SchematicBlockDefault getVine() {
        return new SchematicBlockDefault() {
            @Override
            public boolean isReadyToBuild(Level world, BlockPos blockPos) {
                return super.isReadyToBuild(world, blockPos)
                    && (world.getBlockState(blockPos.above()).getBlock() instanceof VineBlock
                        || StreamSupport.stream(Direction.Plane.HORIZONTAL.spliterator(), false).map(Direction::getNormal).map(blockPos::offset)
                            .map(world::getBlockState)
                            .anyMatch(state -> state.canOcclude() && state.getMaterial().blocksMotion()));
            }
        };
    }
}

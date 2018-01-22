package buildcraft.builders;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockVine;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.schematics.SchematicBlockFactoryRegistry;
import buildcraft.api.schematics.SchematicEntityFactoryRegistry;

import buildcraft.builders.snapshot.SchematicBlockAir;
import buildcraft.builders.snapshot.SchematicBlockDefault;
import buildcraft.builders.snapshot.SchematicBlockFluid;
import buildcraft.builders.snapshot.SchematicEntityDefault;

public class BCBuildersSchematics {
    public static void preInit() {
        registerSchematicFactory("air", 0, SchematicBlockAir::predicate, SchematicBlockAir::new);
        registerSchematicFactory("default", 100, SchematicBlockDefault::predicate, SchematicBlockDefault::new);
        registerSchematicFactory("fluid", 200, SchematicBlockFluid::predicate, SchematicBlockFluid::new);

        registerSchematicFactory("banner", 300, c -> c.block instanceof BlockBanner, BCBuildersSchematics::getBanner);
        registerSchematicFactory("vine", 300, c -> c.block instanceof BlockVine, BCBuildersSchematics::getVine);

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
            public List<ItemStack> computeRequiredItems() {
                return Collections.singletonList(makeBanner(
                    EnumDyeColor.byDyeDamage(tileNbt.getInteger("Base")), tileNbt.getTagList("Patterns", 10)));
            }
        };
    }

    /**
     * Direct copy from 1.12
     */
    public static ItemStack makeBanner(EnumDyeColor dyeColor, @Nullable NBTTagList nbtTagList)
    {
        ItemStack itemstack = new ItemStack(Items.BANNER, 1, dyeColor.getDyeDamage());

        if (nbtTagList != null && !nbtTagList.hasNoTags())
        {
            itemstack.getSubCompound("BlockEntityTag", true).setTag("Patterns", nbtTagList.copy());
        }

        return itemstack;
    }

    private static SchematicBlockDefault getVine() {
        return new SchematicBlockDefault() {
            @Override
            public boolean isReadyToBuild(World world, BlockPos blockPos) {
                return super.isReadyToBuild(world, blockPos)
                    && (world.getBlockState(blockPos.up()).getBlock() instanceof BlockVine
                        || StreamSupport.stream(EnumFacing.Plane.HORIZONTAL.spliterator(), false).map(blockPos::offset)
                            .map(world::getBlockState)
                            .anyMatch(state -> state.isFullCube() && state.getMaterial().blocksMovement()));
            }
        };
    }
}

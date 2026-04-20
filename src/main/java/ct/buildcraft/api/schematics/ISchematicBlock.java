package ct.buildcraft.api.schematics;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.InvalidInputDataException;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.fluids.FluidStack;

public interface ISchematicBlock {
    void init(SchematicBlockContext context);

    default boolean isAir() {
        return false;
    }

    @Nonnull
    default Set<BlockPos> getRequiredBlockOffsets() {
        return Collections.emptySet();
    }

    @Nonnull
    default List<ItemStack> computeRequiredItems() {
        return Collections.emptyList();
    }

    @Nonnull
    default List<FluidStack> computeRequiredFluids() {
        return Collections.emptyList();
    }

    ISchematicBlock getRotated(Rotation rotation);

    boolean canBuild(Level world, BlockPos blockPos);

    default boolean isReadyToBuild(Level world, BlockPos blockPos) {
        return true;
    }

    boolean build(Level world, BlockPos blockPos);

    boolean buildWithoutChecks(Level world, BlockPos blockPos);

    boolean isBuilt(Level world, BlockPos blockPos);

    CompoundTag serializeNBT();

    /** @throws InvalidInputDataException If the input data wasn't correct or didn't make sense. */
    void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException;
}

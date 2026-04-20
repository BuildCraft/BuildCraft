package ct.buildcraft.api.schematics;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.InvalidInputDataException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

public interface ISchematicEntity {
    void init(SchematicEntityContext context);

    Vec3 getPos();

    @Nonnull
    default List<ItemStack> computeRequiredItems() {
        return Collections.emptyList();
    }

    @Nonnull
    default List<FluidStack> computeRequiredFluids() {
        return Collections.emptyList();
    }

    ISchematicEntity getRotated(Rotation rotation);

    Entity build(BlockAndTintGetter world, BlockPos basePos);

    Entity buildWithoutChecks(BlockAndTintGetter world, BlockPos basePos);

    CompoundTag serializeNBT();

    /** @throws InvalidInputDataException If the input data wasn't correct or didn't make sense. */
    void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException;
}

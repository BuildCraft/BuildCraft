package buildcraft.api.bpt;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IStackFilter;

public interface IBuilder {
    World getWorld();

    BlockPos getPos();

    ImmutableSet<BptPermissions> getPermissions();

    /** @return The number of ticks the animation will take */
    int startBlockBuilding(Vec3d target, IBlockState state, int delay);

    /** @return The number of ticks the animation will take */
    int startItemStackBuilding(Vec3d target, ItemStack display, int delay);

    /** @return The number of ticks the animation will take. It is an array {start, end} of the fluid flowing
     *         timings. */
    int[] startFluidBuilding(Vec3d target, FluidStack fluid, int delay);

    /** @return The number of ticks the animation will take. It is an array {start, end} of the power flowing
     *         timings. */
    int[] startPowerBuilding(Vec3d target, int milliJoules, int delay);

    /** Requests a single item stack */
    IRequestedStack requestStack(IStackFilter filter, int amunt);

    IRequestedFluid requestFluid(IFluidFilter filter, int amount);

    void addAction(IBptAction action, int delay);

    public interface IRequestedStack {
        ItemStack getRequested();

        void release();
    }

    public interface IRequestedFluid {
        FluidStack getRequested();

        void release();
    }
}

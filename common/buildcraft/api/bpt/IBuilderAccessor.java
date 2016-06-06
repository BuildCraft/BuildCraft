package buildcraft.api.bpt;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IStackFilter;
import buildcraft.lib.permission.PlayerOwner;

// TODO: What does this encompass? Is this just a context, or is it everything?
// Should implementations delegate to something else for item/fluid getting?
// How do "robot builders" work? Don't they have lots of different positions depending
// on which one is executing it?
public interface IBuilderAccessor {
    World getWorld();

    /** @return The position from where building animations should start. Most of the time this will be inside the
     *         builder block, however this may not be the case if a player or robot is building. */
    Vec3d getBuilderPosition();

    ImmutableSet<BptPermissions> getPermissions();

    PlayerOwner getOwner();

    /** @return The number of ticks the animation will take */
    int startBlockAnimation(Vec3d target, IBlockState state, int delay);

    /** @return The number of ticks the animation will take */
    int startItemStackAnimation(Vec3d target, ItemStack display, int delay);

    /** @return The number of ticks the animation will take. It is an array {start, end} of the fluid flowing
     *         timings. */
    // FIXME Ambiguous timings doc!
    int[] startFluidAnimation(Vec3d target, FluidStack fluid, int delay);

    /** @return The number of ticks the animation will take. It is an array {start, end} of the power flowing
     *         timings. */
    // FIXME Ambiguous timings doc!
    int[] startPowerAnimation(Vec3d target, int milliJoules, int delay);

    /** Requests a single item stack */
    IRequestedStack requestStack(IStackFilter filter, int amunt);

    IRequestedFluid requestFluid(IFluidFilter filter, int amount);

    void addAction(IBptAction action, int delay);

    // TODO: What does this do? It doesn't make sense atm
    public interface IRequestedStack {
        ItemStack getRequested();

        void release();
    }

    // TODO: What does this do? It doesn't make sense atm
    public interface IRequestedFluid {
        FluidStack getRequested();

        void release();
    }
}

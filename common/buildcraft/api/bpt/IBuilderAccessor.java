/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
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

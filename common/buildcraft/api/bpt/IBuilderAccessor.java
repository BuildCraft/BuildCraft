/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.api.bpt;

import com.google.common.collect.ImmutableSet;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.lib.bpt.task.TaskUsable;
import buildcraft.lib.permission.PlayerOwner;

// TODO: What does this encompass? Is this just a context, or is it everything?
// Should implementations delegate to something else for item/fluid getting?
// How do "robot builders" work? Don't they have lots of different positions depending
// on which one is executing it?
public interface IBuilderAccessor extends IMaterialProvider {
    World getWorld();

    ImmutableSet<BptPermissions> getPermissions();

    PlayerOwner getOwner();

    /** @return The number of ticks the animation will take */
    int startItemStackAnimation(Vec3d target, ItemStack display, int delay);

    /** @return The number of ticks the animation will take. It is an array {start, end} of the fluid flowing
     *         timings. */
    // FIXME Ambiguous timings doc!
    int[] startFluidAnimation(Vec3d target, FluidStack fluid, int delay);

    /** @return The number of ticks the animation will take. It is an array {start, end} of the power flowing
     *         timings. */
    // FIXME Ambiguous timings doc!
    int[] startPowerAnimation(Vec3d target, long microJoules, int delay);

    /** Checks to see if this builder has permission to edit the given block */
    boolean hasPermissionToEdit(BlockPos pos);

    long drainPower(long requested, TaskUsable requestor);

    boolean target(Vec3d vec, TaskUsable requestor);

    void returnItems(Vec3d from, ItemStack stack);

    void returnItems(BlockPos from, ItemStack stack);
}

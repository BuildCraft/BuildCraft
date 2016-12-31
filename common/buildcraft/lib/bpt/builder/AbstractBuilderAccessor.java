/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.bpt.builder;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.bpt.BptPermissions;
import buildcraft.api.bpt.IBuilderAccessor;

import buildcraft.lib.misc.StackUtil;

public abstract class AbstractBuilderAccessor implements IBuilderAccessor {
    private final GameProfile owner;
    private final BuilderAnimationManager animationManager;

    public AbstractBuilderAccessor(GameProfile owner, BuilderAnimationManager animationManager) {
        this.owner = owner;
        this.animationManager = animationManager;
    }

    public AbstractBuilderAccessor(GameProfile owner, BuilderAnimationManager animation, NBTTagCompound nbt) {
        this(owner, animation);
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        return nbt;
    }

    public void tick() {

    }

    @Override
    public GameProfile getOwner() {
        return owner;
    }

    @Override
    public int startItemStackAnimation(Vec3d target, ItemStack display, int delay) {
        return 0;
    }

    @Override
    public int[] startFluidAnimation(Vec3d target, FluidStack fluid, int delay) {
        // TODO use the animation manager!
        return new int[] { 0, 0 };
    }

    @Override
    public int[] startPowerAnimation(Vec3d target, long microJoules, int delay) {
        // TODO use the animation manager!
        return new int[] { 0, 30 };
    }

    @Override
    public IRequestedItem requestStack(ItemStack stack) {
        if (getPermissions().contains(BptPermissions.FREE_MATERIALS)) {
            return new RequestedFree.FreeItem(stack);
        }
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public IRequestedItem requestStackForBlock(IBlockState state) {
        ItemStack wanted = StackUtil.getItemStackForState(state);
        if (wanted == null) {
            throw new IllegalStateException("Unknown item block " + state);
        }
        return requestStack(wanted);
    }

    @Override
    public IRequestedFluid requestFluid(FluidStack fluid) {
        if (getPermissions().contains(BptPermissions.FREE_MATERIALS)) {
            return new RequestedFree.FreeFluid(fluid);
        }
        throw new AbstractMethodError("Implement this!");
    }

    public void releaseAll() {

    }
}

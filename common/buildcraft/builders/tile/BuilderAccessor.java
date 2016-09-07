/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.tile;

import com.google.common.collect.ImmutableSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.api.bpt.BptPermissions;
import buildcraft.lib.bpt.builder.AbstractBuilderAccessor;
import buildcraft.lib.bpt.task.TaskUsable;
import buildcraft.lib.misc.PermissionUtil;
import buildcraft.lib.misc.VecUtil;

public class BuilderAccessor extends AbstractBuilderAccessor {
    private final Vec3d vec;
    private final TileBuilder_Neptune tile;

    public BuilderAccessor(TileBuilder_Neptune tile) {
        super(tile.getOwner(), tile.tickingBuilder.animationManager);
        this.vec = VecUtil.add(new Vec3d(0.5, 0.5, 0.5), tile.getPos());
        this.tile = tile;
    }

    public BuilderAccessor(TileBuilder_Neptune tile, NBTTagCompound nbt) {
        super(tile.getOwner(), tile.tickingBuilder.animationManager, nbt);
        this.vec = VecUtil.add(new Vec3d(0.5, 0.5, 0.5), tile.getPos());
        this.tile = tile;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();

        return nbt;
    }

    @Override
    public World getWorld() {
        return tile.getWorld();
    }

    @Override
    public ImmutableSet<BptPermissions> getPermissions() {
        return ImmutableSet.of(BptPermissions.FREE_MATERIALS);
    }

    @Override
    public boolean hasPermissionToEdit(BlockPos pos) {
        return PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, tile.getOwner(), PermissionUtil.createFrom(getWorld(), pos));
    }

    @Override
    public long drainPower(long requested, TaskUsable requestor) {
        return requested;// TODO: fix this!
    }

    @Override
    public boolean target(Vec3d vec, TaskUsable requestor) {
        return true; // The builder doesn't have a head position
    }

    @Override
    public void returnItems(BlockPos from, ItemStack stack) {
        // TODO Auto-generated method stub
    }

    @Override
    public void returnItems(Vec3d from, ItemStack stack) {
        // TODO Auto-generated method stub
    }
}

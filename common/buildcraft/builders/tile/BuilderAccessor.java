/* Copyright (c) 2016 SpaceToad and the BuildCraft team
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

import buildcraft.builders.bpt.TickingBlueprintBuilder;
import buildcraft.lib.bpt.builder.AbstractBuilderAccessor;
import buildcraft.lib.bpt.task.TaskUsable;
import buildcraft.lib.misc.PermissionUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.WorldUtil;
import buildcraft.lib.tile.TileBC_Neptune;

public class BuilderAccessor extends AbstractBuilderAccessor {
    private final Vec3d vec;
    private final TileBC_Neptune tile;
    private final boolean isCreative;

    public BuilderAccessor(TileBC_Neptune tile, TickingBlueprintBuilder ticker) {
        super(tile.getOwner(), ticker.animationManager);
        this.vec = VecUtil.add(new Vec3d(0.5, 0.5, 0.5), tile.getPos());
        this.tile = tile;
        isCreative = WorldUtil.isWorldCreative(tile.getWorld());
    }

    public BuilderAccessor(TileBC_Neptune tile, TickingBlueprintBuilder ticker, NBTTagCompound nbt) {
        super(tile.getOwner(), ticker.animationManager, nbt);
        this.vec = VecUtil.add(new Vec3d(0.5, 0.5, 0.5), tile.getPos());
        this.tile = tile;
        isCreative = WorldUtil.isWorldCreative(tile.getWorld());
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
        if (isCreative) {
            return BptPermissions.SET_NORMAL_CREATIVE;
        } else {
            return BptPermissions.SET_NORMAL_SURVIVAL;
        }
    }

    @Override
    public boolean hasPermissionToEdit(BlockPos pos) {
        return PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, getOwner(), PermissionUtil.createFrom(getWorld(), pos));
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

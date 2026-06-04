/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.IPlayerOwned;

/** Future class for checking to see if a given player can actually do something. */
public class PermissionUtil {
    // Just object types so that we can change these later without needing to change callers
    public static final Object PERM_VIEW = "buildcraft.view";
    public static final Object PERM_EDIT = "buildcraft.edit";
    public static final Object PERM_DESTROY = "buildcraft.destroy";

    private static final int MAX_INTERACT_DISTANCE = 8;
    private static final int MAX_INTERACT_DISTANCE_SQ = MAX_INTERACT_DISTANCE * MAX_INTERACT_DISTANCE;

    public static boolean hasPermission(Object type, PermissionBlock attempting, PermissionBlock target) {
        // TODO(R.Chen): fire a Fabric block-break callback if it's a break event; check spawn-radius OP rule.
        return true;
    }

    public static boolean hasPermission(Object type, GameProfile attempting, PermissionBlock target) {
        // TODO(R.Chen): fire a Fabric block-break callback if it's a break event; check spawn-radius OP rule.
        return true;
    }

    public static boolean hasPermission(Object type, PlayerEntity attempting, PermissionBlock target) {
        if (attempting.squaredDistanceTo(
            target.pos.getX() + 0.5, target.pos.getY() + 0.5, target.pos.getZ() + 0.5
        ) > MAX_INTERACT_DISTANCE_SQ) {
            return false;
        }
        // TODO(R.Chen): check spawn-radius OP rule for PERM_DESTROY / PERM_EDIT.
        return true;
    }

    public static PermissionBlock createFrom(World world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        IPlayerOwned owned = null;

        if (tile instanceof IPlayerOwned) {
            owned = (IPlayerOwned) tile;
        }

        return new PermissionBlock(owned, pos);
    }

    public static class PermissionBlock {
        public final IPlayerOwned owned;
        public final BlockPos pos;

        public PermissionBlock(IPlayerOwned owned, BlockPos pos) {
            this.owned = owned;
            this.pos = pos;
        }
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.IPlayerOwned;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Future class for checking to see if a given player can actually do something. */
public class PermissionUtil {
    // Just object types so that we can change these later without needing to change callers
    public static final Object PERM_VIEW = "buildcraft.view";
    public static final Object PERM_EDIT = "buildcraft.edit";
    public static final Object PERM_DESTROY = "buildcraft.destroy";

    private static final int MAX_INTERACT_DISTANCE = 8;
    private static final int MAX_INTERACT_DISTANCE_SQ = MAX_INTERACT_DISTANCE * MAX_INTERACT_DISTANCE;

    public static boolean hasPermission(Object type, PermissionBlock attempting, PermissionBlock target) {
        // TODO: fire a forge block-break event if its a break event
        if (type == PERM_DESTROY || type == PERM_EDIT) {
            // TODO check "area" -- the player must be an OP if its within spawn radius
        }
        return true;
    }

    public static boolean hasPermission(Object type, GameProfile attempting, PermissionBlock target) {
        // TODO: fire a forge block-break event if its a break event
        if (type == PERM_DESTROY || type == PERM_EDIT) {
            // TODO check "area" -- the player must be an OP if its within spawn radius
        }
        return true;
    }

    public static boolean hasPermission(Object type, Player attempting, PermissionBlock target) {
        // TODO: fire a forge block-break event if its a break event
        if (attempting.distanceToSqr(target.pos.getX(), target.pos.getY(), target.pos.getZ()) > MAX_INTERACT_DISTANCE_SQ) {
            return false;
        }

        if (type == PERM_DESTROY || type == PERM_EDIT) {
            // TODO check "area" -- the player must be an OP if its within spawn radius
        }
        return true;
    }

    public static PermissionBlock createFrom(Level world, BlockPos pos) {
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

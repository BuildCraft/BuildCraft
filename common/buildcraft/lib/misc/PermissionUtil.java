package buildcraft.lib.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.permission.IPlayerOwned;
import buildcraft.lib.permission.PlayerOwner;
import buildcraft.lib.permission.PlayerOwnership;

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
        return PlayerOwnership.INSTANCE.canUse(attempting.owned.getOwner(), target.owned);
    }

    public static boolean hasPermission(Object type, PlayerOwner attempting, PermissionBlock target) {
        // TODO: fire a forge block-break event if its a break event
        if (type == PERM_DESTROY || type == PERM_EDIT) {
            // TODO check "area" -- the player must be an OP if its within spawn radius
        }
        return PlayerOwnership.INSTANCE.canUse(attempting, target.owned);
    }

    public static boolean hasPermission(Object type, EntityPlayer attempting, PermissionBlock target) {
        // TODO: fire a forge block-break event if its a break event
        if (attempting.getDistanceSq(target.pos) > MAX_INTERACT_DISTANCE_SQ) {
            return false;
        }

        if (type == PERM_DESTROY || type == PERM_EDIT) {
            // TODO check "area" -- the player must be an OP if its within spawn radius
        }
        return PlayerOwnership.INSTANCE.canUse(attempting, target.owned);
    }

    public static PermissionBlock createFrom(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
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

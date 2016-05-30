package buildcraft.lib.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.permission.PlayerOwner;

/** Future class for checking to see if a given player can actually do something. */
public class PermissionUtil {
    // Just object types so that we can change these later without needing to change callers
    public static final Object PERM_VIEW = "buildcraft.view";
    public static final Object PERM_EDIT = "buildcraft.edit";
    public static final Object PERM_DESTROY = "buildcraft.destroy";

    public static boolean hasPermission(Object type, PermissionBlock attempting, PermissionBlock target) {
        if (type == PERM_DESTROY || type == PERM_EDIT) {
            // TODO check "area" -- the player must be an OP if its within spawn radius
        }
        return true;
    }

    public static boolean hasPermission(Object type, EntityPlayer attempting, PermissionBlock target) {
        // TODO check "closeness" -- the player must be close to the target
        if (type == PERM_DESTROY || type == PERM_EDIT) {
            // TODO check "area" -- the player must be an OP if its within spawn radius
        }
        return true;
    }

    public static PermissionBlock createFrom(World world, BlockPos pos) {
        // TODO
        return new PermissionBlock(null, pos);
    }

    public static class PermissionBlock {
        public final PlayerOwner owner;
        public final BlockPos pos;

        public PermissionBlock(PlayerOwner owner, BlockPos pos) {
            this.owner = owner;
            this.pos = pos;
        }
    }
}

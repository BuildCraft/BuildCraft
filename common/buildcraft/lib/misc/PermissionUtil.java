package buildcraft.lib.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.permission.PlayerOwner;

/** Future class for checking to see if a given player can actually do something. */
public class PermissionUtil {
    public static boolean hasPermission(UsedObject attempting, UsedObject target) {
        return true;
    }

    public static boolean hasPermission(EntityPlayer attempting, UsedObject target) {
        return true;
    }

    public static UsedObject createFrom(World world, BlockPos pos) {
        // TODO
        return new UsedObject(null, pos);
    }

    public static class UsedObject {
        public final PlayerOwner owner;
        public final BlockPos pos;

        public UsedObject(PlayerOwner owner, BlockPos pos) {
            this.owner = owner;
            this.pos = pos;
        }
    }
}

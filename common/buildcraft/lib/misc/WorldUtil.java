package buildcraft.lib.misc;

import net.minecraft.world.World;

public class WorldUtil {
    public static boolean isWorldCreative(World world) {
        return world.getWorldInfo().getGameType().isCreative();
    }
}

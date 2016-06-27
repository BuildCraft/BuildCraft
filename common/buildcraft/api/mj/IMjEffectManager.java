package buildcraft.api.mj;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Various effects for showing power loss visibly, and for large amounts of power, causes some damage to nearby
 * entities. */
public interface IMjEffectManager {
    void createPowerLossEffect(World world, Vec3d center, int milliJoulesLost);

    void createPowerLossEffect(World world, Vec3d center, EnumFacing direction, int milliJoulesLost);

    void createPowerLossEffect(World world, Vec3d center, Vec3d direction, int milliJoulesLost);
}

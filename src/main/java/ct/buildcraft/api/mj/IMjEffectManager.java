package ct.buildcraft.api.mj;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Various effects for showing power loss visibly, and for large amounts of power, causes some damage to nearby
 * entities. */
public interface IMjEffectManager {
    void createPowerLossEffect(Level world, Vec3 center, long microJoulesLost);

    void createPowerLossEffect(Level world, Vec3 center, Direction direction, long microJoulesLost);

    void createPowerLossEffect(Level world, Vec3 center, Vec3 direction, long microJoulesLost);
}

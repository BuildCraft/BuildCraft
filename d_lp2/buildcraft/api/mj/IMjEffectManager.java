package buildcraft.api.mj;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface IMjEffectManager {
    void createPowerLossEffect(World world, Vec3d center, int joulesLost);

    void createPowerLossEffect(World world, Vec3d center, EnumFacing direction, int joulesLost);

    void createPowerLossEffect(World world, Vec3d center, Vec3d direction, int joulesLost);
}

package buildcraft.robotics;

import buildcraft.lib.registry.RegistrationHelper;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.RegistryObject;

public class BCRoboticsParticleTypes {
    private static final RegistrationHelper HELPER = new RegistrationHelper(BCRobotics.MODID);

    public static RegistryObject<SimpleParticleType> robot;

    public static void preInit() {
        robot = HELPER.addParticle("robot");
    }
}

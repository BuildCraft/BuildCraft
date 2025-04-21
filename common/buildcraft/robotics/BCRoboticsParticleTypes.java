package buildcraft.robotics;

import buildcraft.lib.registry.RegistrationHelper;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.fml.RegistryObject;

public class BCRoboticsParticleTypes {
    private static final RegistrationHelper HELPER = new RegistrationHelper(BCRobotics.MODID);

    public static RegistryObject<BasicParticleType> robot;

    public static void preInit() {
        robot = HELPER.addParticle("robot");
    }
}

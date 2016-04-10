package buildcraft.test;

import org.junit.Assert;

import net.minecraft.util.math.Vec3d;

public class TestHelper {
    public static void assertVec3dEquals(Vec3d expected, Vec3d centerExact2) {
        if (expected.distanceTo(centerExact2) > 1e-12) {
            Assert.fail(centerExact2 + " was not equal to expected " + expected);
        }
    }
}

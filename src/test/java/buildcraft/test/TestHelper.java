package buildcraft.test;

import org.junit.Assert;

import net.minecraft.util.Vec3;

public class TestHelper {
    public static void assertVec3Equals(Vec3 expected, Vec3 centerExact2) {
        if (expected.distanceTo(centerExact2) > 1e-12) {
            Assert.fail(centerExact2 + " was not equal to expected " + expected);
        }
    }
}

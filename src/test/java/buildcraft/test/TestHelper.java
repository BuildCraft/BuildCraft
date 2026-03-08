package buildcraft.test;

import net.minecraft.util.math.vector.Vector3d;
import org.junit.Assert;


public class TestHelper
{
//    public static void assertVec3dEquals(Vec3d expected, Vec3d centerExact2)
    public static void assertVec3dEquals(Vector3d expected, Vector3d centerExact2)
    {
        if (expected.distanceTo(centerExact2) > 1e-12)
        {
            Assert.fail(centerExact2 + " was not equal to expected " + expected);
        }
    }
}

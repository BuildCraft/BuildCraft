package buildcraft.test.lib.client.model;

import org.junit.Assert;
import org.junit.Test;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

import buildcraft.lib.client.model.MutableQuad;

public class MutableQuadTest {
    @Test
    public void testRotations() {
        for (EnumFacing from : EnumFacing.VALUES) {
            for (EnumFacing to : EnumFacing.VALUES) {
                Vec3i vec = from.getDirectionVec();
                MutableQuad q = new MutableQuad();
                q.vertex_0.positionf(vec.getX(), vec.getY(), vec.getZ());
                q.rotate(from, to, 0, 0, 0);
                float ex = to.getFrontOffsetX();
                float ey = to.getFrontOffsetY();
                float ez = to.getFrontOffsetZ();

                Assert.assertEquals(from + " -> " + to + " [X]", ex, q.vertex_0.position_x, 0.001f);
                Assert.assertEquals(from + " -> " + to + " [Y]", ey, q.vertex_0.position_y, 0.001f);
                Assert.assertEquals(from + " -> " + to + " [Z]", ez, q.vertex_0.position_z, 0.001f);
            }
        }
    }
}

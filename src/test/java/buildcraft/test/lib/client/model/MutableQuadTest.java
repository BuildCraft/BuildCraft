package buildcraft.test.lib.client.model;

import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.Direction;
import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.client.model.MutableQuad;

public class MutableQuadTest
{

    @Test
    public void testRotations()
    {
//        for (EnumFacing from : EnumFacing.VALUES)
        for (Direction from : Direction.values())
        {
//            for (EnumFacing to : EnumFacing.VALUES)
            for (Direction to : Direction.values())
            {
//                Vector3i vec = from.getDirectionVec();
                Vector3i vec = from.getNormal();
                MutableQuad q = new MutableQuad();
                q.vertex_0.positionf(vec.getX(), vec.getY(), vec.getZ());
                q.rotate(from, to, 0, 0, 0);
//                float ex = to.getFrontOffsetX();
//                float ey = to.getFrontOffsetY();
//                float ez = to.getFrontOffsetZ();
                float ex = to.getStepX();
                float ey = to.getStepY();
                float ez = to.getStepZ();

                Assert.assertEquals(from + " -> " + to + " [X]", ex, q.vertex_0.position_x, 0.001f);
                Assert.assertEquals(from + " -> " + to + " [Y]", ey, q.vertex_0.position_y, 0.001f);
                Assert.assertEquals(from + " -> " + to + " [Z]", ez, q.vertex_0.position_z, 0.001f);
            }
        }
    }
}

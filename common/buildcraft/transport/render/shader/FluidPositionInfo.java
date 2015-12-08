package buildcraft.transport.render.shader;

import java.nio.FloatBuffer;

import net.minecraft.util.Vec3;

public class FluidPositionInfo {
    public final Vec3 min, max, point, direction;
    public final boolean visible, moves;
    public final float textureIndex;
    public final long startMoving, endMoving;

    public FluidPositionInfo(FluidPositionInfoBuilder builder) {
        min = builder.min;
        max = builder.max;
        point = builder.point;
        direction = builder.direction;
        visible = builder.visible;
        moves = builder.moves;
        textureIndex = builder.textureIndex;
        startMoving = builder.startMoving;
        endMoving = builder.endMoving;
    }

    public FloatBuffer asBuffer() {
        FloatBuffer buffer = FloatBuffer.allocate(4 * 3 + 1);
        buffer.put(asArray(min));
        buffer.put(asArray(max));
        buffer.put(asArray(point));
        buffer.put(asArray(direction));
        buffer.put(textureIndex);
        return buffer;
    }

    private float[] asArray(Vec3 vec) {
        return new float[] { (float) vec.xCoord, (float) vec.yCoord, (float) vec.zCoord };
    }
}

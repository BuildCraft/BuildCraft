package buildcraft.transport.client.shader;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.utils.Utils;

@SideOnly(Side.CLIENT)
public class FluidShaderData {
    public final FluidPositionInfo positionA, positionB;
    public final TextureAtlasSprite sprite;
    public final long expireTick;

    public FluidShaderData(FluidShaderDataBuilder builder) {
        this.positionA = builder.positionA;
        this.positionB = builder.positionB;
        this.sprite = builder.sprite;
        this.expireTick = builder.expire;
    }

    public FluidPositionInfo interpolateA(long tick, float partialTicks) {
        if (!positionA.moves) {
            return positionA;
        }
        if (tick < positionA.startMoving) {
            return positionA;
        }
        float position = 0;
        return positionA;
    }

    /** Interpolates B to move away from A as time progresses. */
    public FluidPositionInfo interpolateB(long tick, float partialTicks) {
        if (!positionB.moves) {
            return positionB;
        }
        if (tick < positionB.startMoving) {
            return positionB;
        }
        float position = 0;
        if (tick >= positionB.endMoving) {
            position = 0;
        } else {
            long tickDiff = positionB.endMoving - positionB.startMoving;
            if (tickDiff <= 0) {
                position = 1;
            } else {
                position = (positionB.endMoving - tick - partialTicks) / tickDiff;
            }
        }

        // TODO: Make this respect direction, and move on a curve (Changing the direction as appropriate)
        Vec3 diff = positionB.point.subtract(positionA.point);
        Vec3 offset = Utils.multiply(diff, 1 - position);

        FluidPositionInfoBuilder builder = new FluidPositionInfoBuilder(positionB);
        builder.setMin(builder.min.add(offset));
        builder.setMax(builder.max.add(offset));
        builder.setPoint(builder.point.add(offset));
        return builder.build();
    }

    public boolean isValid(long tick) {
        return tick < expireTick;
    }
}

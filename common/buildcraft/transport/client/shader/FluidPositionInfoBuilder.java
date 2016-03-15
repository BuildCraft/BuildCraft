package buildcraft.transport.client.shader;

import net.minecraft.util.Vec3;

public class FluidPositionInfoBuilder {
    Vec3 min, max, point, direction;
    boolean visible, moves;
    float textureIndex;
    long startMoving, endMoving;

    public FluidPositionInfoBuilder(FluidPositionInfo info) {
        setMin(info.min).setMax(info.max).setPoint(info.point).setDirection(info.direction);
        setVisible(info.visible).setMoves(info.moves).setTextureIndex(info.textureIndex);
        setStartMoving(info.startMoving).setEndMoving(info.endMoving);
    }

    public FluidPositionInfoBuilder() {
        // Empty builder.
    }

    public FluidPositionInfoBuilder setMin(Vec3 min) {
        this.min = min;
        return this;
    }

    public FluidPositionInfoBuilder setMax(Vec3 max) {
        this.max = max;
        return this;
    }

    public FluidPositionInfoBuilder setPoint(Vec3 point) {
        this.point = point;
        return this;
    }

    public FluidPositionInfoBuilder setDirection(Vec3 direction) {
        this.direction = direction;
        return this;
    }

    public FluidPositionInfoBuilder setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public FluidPositionInfoBuilder setMoves(boolean moves) {
        this.moves = moves;
        return this;
    }

    public FluidPositionInfoBuilder setTextureIndex(float textureIndex) {
        this.textureIndex = textureIndex;
        return this;
    }

    public FluidPositionInfoBuilder setStartMoving(long startMoving) {
        this.startMoving = startMoving;
        return this;
    }

    public FluidPositionInfoBuilder setEndMoving(long endMoving) {
        this.endMoving = endMoving;
        return this;
    }

    public FluidPositionInfo build() {
        return new FluidPositionInfo(this);
    }
}

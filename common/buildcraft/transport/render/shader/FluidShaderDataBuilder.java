package buildcraft.transport.render.shader;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class FluidShaderDataBuilder {
    FluidPositionInfo positionA, positionB;
    TextureAtlasSprite sprite;
    long expire;

    public FluidShaderDataBuilder setPositionA(FluidPositionInfo info) {
        positionA = info;
        return this;
    }

    public FluidShaderDataBuilder setPositionB(FluidPositionInfo info) {
        positionB = info;
        return this;
    }

    public FluidShaderDataBuilder setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
        return this;
    }

    public FluidShaderDataBuilder setExpires(long tick) {
        this.expire = tick;
        return this;
    }

    public FluidShaderData build() {
        return new FluidShaderData(this);
    }
}

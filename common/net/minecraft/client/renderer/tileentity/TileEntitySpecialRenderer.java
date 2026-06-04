// STUB(R.Chen): Minecraft 1.12 TileEntitySpecialRenderer → 1.20.1 BlockEntityRenderer.
package net.minecraft.client.renderer.tileentity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

public abstract class TileEntitySpecialRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    @Override
    public abstract void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);
}

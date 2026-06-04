// STUB(R.Chen): Forge FastTESR (fast tile entity special renderer) — compile shim.
package net.minecraftforge.client.model.animation;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public abstract class FastTESR<T extends BlockEntity> {
    public abstract void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage, MatrixStack matrices, VertexConsumerProvider.Immediate buffer);
}

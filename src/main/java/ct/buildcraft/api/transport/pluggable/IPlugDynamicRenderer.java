package ct.buildcraft.api.transport.pluggable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;

public interface IPlugDynamicRenderer<P extends PipePluggable> {
    void render(P plug, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,int combinedLight, int combinedOverlay);
}

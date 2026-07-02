package ct.buildcraft.api.transport.pipe;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;

public interface IPipeFlowRenderer<F extends PipeFlow> {
    /** @param flow The flow to render
     * @param partialTicks
     * @param matrix
     * @param buffer The vertex buffer that you can render into. Note that you can still do GL stuff.
     * @param combinedLight
     * @param combinedOverlay  */
    void render(F flow, float partialTicks, PoseStack matrix, MultiBufferSource buffer,int combinedLight, int combinedOverlay);
}

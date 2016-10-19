package buildcraft.transport.client.render;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.transport.neptune.IPipeFlowRenderer;

import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.TravellingItem;

public enum RendererPipeFlowItems implements IPipeFlowRenderer<PipeFlowItems> {
    INSTANCE;

    @Override
    public void render(PipeFlowItems flow, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        long now = flow.pipe.getHolder().getPipeWorld().getTotalWorldTime();

        List<TravellingItem> toRender = flow.getAllItemsForRender();

        for (TravellingItem item : toRender) {
            Vec3d pos = item.getRenderPosition(BlockPos.ORIGIN, now, partialTicks);
            // TODO: render the item FAST
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + pos.xCoord, y + pos.yCoord, z + pos.zCoord);
            Minecraft.getMinecraft().getRenderItem().renderItem(item.stack, TransformType.GROUND);
            GlStateManager.popMatrix();
        }
    }
}

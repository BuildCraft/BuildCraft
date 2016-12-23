package buildcraft.builders.client.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import buildcraft.builders.entity.EntityQuarryFrame;

public class RenderEntityQuarryFrame extends Render<EntityQuarryFrame> {

    public RenderEntityQuarryFrame(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityQuarryFrame entity) {
        return null;
    }

    @Override
    public void doRender(EntityQuarryFrame entity, double x, double y, double z, float entityYaw, float partialTicks) {
        // NO-OP -- the quarry tile renders this
    }
}

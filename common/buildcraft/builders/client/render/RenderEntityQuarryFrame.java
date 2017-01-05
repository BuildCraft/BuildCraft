package buildcraft.builders.client.render;

import net.minecraft.client.renderer.culling.ICamera;
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
    public boolean shouldRender(EntityQuarryFrame livingEntity, ICamera camera, double camX, double camY, double camZ) {
        return false;
    }
}

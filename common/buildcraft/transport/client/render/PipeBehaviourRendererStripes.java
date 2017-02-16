package buildcraft.transport.client.render;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;

public enum PipeBehaviourRendererStripes implements IPipeBehaviourRenderer<PipeBehaviourStripes> {
    INSTANCE;

    @Override
    public void render(PipeBehaviourStripes stripes, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        EnumFacing dir = stripes.getCurrentDir();
        if (dir == null) return;
        MutableQuad[] quads = BCTransportModels.getStripesDynQuads(dir);
        vb.setTranslation(x, y, z);
        int light = stripes.pipe.getHolder().getPipeWorld().getCombinedLight(stripes.pipe.getHolder().getPipePos(), 0);
        for (MutableQuad q : quads) {
            q.multShade();
            q.lighti(light);
            q.render(vb);
        }
        vb.setTranslation(0, 0, 0);
    }
}

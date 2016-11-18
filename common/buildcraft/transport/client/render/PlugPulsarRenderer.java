package buildcraft.transport.client.render;

import java.util.Arrays;

import javax.vecmath.Matrix4f;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.neptune.IPluggableDynamicRenderer;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MatrixUtil;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.plug.PluggablePulsar;

public class PlugPulsarRenderer implements IPluggableDynamicRenderer {
    private static final MutableQuad[][] cache = new MutableQuad[6 * 2][];

    private final PluggablePulsar toRender;

    public PlugPulsarRenderer(PluggablePulsar toRender) {
        this.toRender = toRender;
    }

    private static MutableQuad[] getFromCache(EnumFacing side, boolean isPulsing, double stage) {
        if (isPulsing /* TODO: Use renderStyle == FULL_ANIMATION */) {
            // TODO: Return a different stage
        }

        int index = side.ordinal() + (isPulsing ? 6 : 0);
        if (isPulsing) {
            cache[index] = null;
        }
        if (cache[index] == null) {
            MutableQuad[] quads = BCTransportModels.getPulsarDynQuads(isPulsing, stage);
            Matrix4f transform = MatrixUtil.rotateTowardsFace(side);
            for (MutableQuad q : quads) {
                q.transform(transform);
                if (q.isShade()) {
                    q.setCalculatedDiffuse();
                    q.setShade(false);
                }
            }
            if (isPulsing) {
                return quads;
            }
            cache[index] = quads;
        }
        return cache[index];
    }

    public static void onModelBake() {
        Arrays.fill(cache, null);
    }

    @Override
    public void render(double x, double y, double z, float partialTicks, VertexBuffer vb) {
        vb.setTranslation(x, y, z);
        for (MutableQuad q : getFromCache(toRender.side, toRender.isPulsing(), toRender.getStage(partialTicks))) {
            q.render(vb);
        }
        vb.setTranslation(0, 0, 0);
    }
}

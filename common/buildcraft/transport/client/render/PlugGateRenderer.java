package buildcraft.transport.client.render;

import java.util.Arrays;

import javax.vecmath.Matrix4f;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.neptune.IPluggableDynamicRenderer;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MatrixUtil;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.plug.PluggableGate;

public class PlugGateRenderer implements IPluggableDynamicRenderer {
    private static final MutableQuad[][] cache = new MutableQuad[6 * 2][];

    private final PluggableGate toRender;

    public PlugGateRenderer(PluggableGate toRender) {
        this.toRender = toRender;
    }

    private static MutableQuad[] getFromCache(EnumFacing side, boolean isOn) {
        int index = side.ordinal() + (isOn ? 6 : 0);
        if (cache[index] == null) {
            MutableQuad[] quads = BCTransportModels.getGateDynQuads(isOn);
            Matrix4f transform = MatrixUtil.rotateTowardsFace(side);
            for (MutableQuad q : quads) {
                q.transform(transform);
                if (q.isShade()) {
                    q.setCalculatedDiffuse();
                    q.setShade(false);
                }
            }
            cache[index] = quads;
        }
        return cache[index];
    }

    public static void onModelBake() {
        Arrays.fill(cache, null);
    }

    @Override
    public void render(double x, double y, double z, VertexBuffer vb) {
        vb.setTranslation(x, y, z);
        for (MutableQuad q : getFromCache(toRender.side, toRender.logic.isOn)) {
            q.render(vb);
        }
        vb.setTranslation(0, 0, 0);
    }
}

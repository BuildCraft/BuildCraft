package buildcraft.transport.client.render;

import java.util.Arrays;

import javax.vecmath.Matrix4f;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MatrixUtil;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.plug.PluggableGate;

public enum PlugGateRenderer implements IPlugDynamicRenderer<PluggableGate> {
    INSTANCE;

    private static final MutableQuad[][] cache = new MutableQuad[6 * 2][];

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
    public void render(PluggableGate gate, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        vb.setTranslation(x, y, z);
        for (MutableQuad q : getFromCache(gate.side, gate.logic.isOn)) {
            q.render(vb);
        }
        vb.setTranslation(0, 0, 0);
    }
}

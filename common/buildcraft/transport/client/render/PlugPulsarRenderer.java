package buildcraft.transport.client.render;

import java.util.Arrays;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.plug.PluggablePulsar;

public enum PlugPulsarRenderer implements IPlugDynamicRenderer<PluggablePulsar> {
    INSTANCE;

    private static final MutableQuad[][] cache = new MutableQuad[6][];

    private static MutableQuad[] getFromCache(PluggablePulsar pulsar, double stage) {
        EnumFacing side = pulsar.side;
        boolean isPulsing = pulsar.isPulsingClient();
        boolean isAuto = pulsar.isAutoEnabledClient();
        boolean isManual = pulsar.isManuallyEnabledClient();
        if (isPulsing /* TODO: Use renderStyle == FULL_ANIMATION */) {
            // TODO: Return a different stage
        }

        int index = side.ordinal();
        if (isPulsing || cache[index] == null) {
            MutableQuad[] quads = BCTransportModels.getPulsarDynQuads(isPulsing, stage, isAuto, isManual);
            for (MutableQuad q : quads) {
                q.rotate(EnumFacing.WEST, side, 0.5f, 0.5f, 0.5f);
                q.multShade();
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
    public void render(PluggablePulsar pulsar, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        vb.setTranslation(x, y, z);
        for (MutableQuad q : getFromCache(pulsar, pulsar.getStage(partialTicks))) {
            q.render(vb);
        }
        vb.setTranslation(0, 0, 0);
    }
}

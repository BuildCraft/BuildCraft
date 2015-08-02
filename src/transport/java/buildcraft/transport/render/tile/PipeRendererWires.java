package buildcraft.transport.render.tile;

import java.util.EnumMap;
import java.util.List;

import javax.vecmath.Vector3f;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.Vec3;

import buildcraft.api.transport.PipeWire;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.render.BuildCraftBakedModel;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.BuildCraftTransport;
import buildcraft.transport.PipeRenderState;

public class PipeRendererWires {
    private static EnumMap<PipeWire, Vec3> wirePosMap = Maps.newEnumMap(PipeWire.class);
    private static final double WIRE_WIDTH = 0.05;
    // Offset all wires very slightly out of the pipe
    private static final double WIRE_OFFSET = 0.001;

    static {
        wirePosMap.put(PipeWire.RED, getOffset(PipeWire.RED, false, true, false));
        wirePosMap.put(PipeWire.BLUE, getOffset(PipeWire.BLUE, true, true, true));
        wirePosMap.put(PipeWire.GREEN, getOffset(PipeWire.GREEN, true, false, false));
        wirePosMap.put(PipeWire.YELLOW, getOffset(PipeWire.YELLOW, false, false, true));
    }

    private static Vec3 getOffset(PipeWire wire, boolean x, boolean y, boolean z) {
        double min = CoreConstants.PIPE_MIN_POS - WIRE_WIDTH;
        double max = CoreConstants.PIPE_MAX_POS;

        // Offset each wire slightly differently to avoid z-fighting between wires
        int multiple = wire.ordinal();
        double offset = WIRE_OFFSET * multiple;
        double inset = -offset;
        Vec3 base = new Vec3(x ? min : max, y ? min : max, z ? min : max);
        return base.addVector(x ? inset : offset, y ? inset : offset, z ? inset : offset);
    }

    public static void renderPipeWires(List<BakedQuad> quads, PipeRenderState renderState) {
        for (PipeWire wire : PipeWire.values()) {
            if (renderState.wireMatrix.hasWire(wire)) {
                renderPipeWire(quads, renderState, wire);
            }
        }
    }

    private static void renderPipeWire(List<BakedQuad> quads, PipeRenderState renderState, PipeWire wire) {
        Vec3 pos = wirePosMap.get(wire);

        TextureAtlasSprite sprite = BuildCraftTransport.instance.wireIconProvider.getIcon(wire, renderState.wireMatrix.isWireLit(wire));

        Vec3 center = pos;
        Vec3 centerSize = new Vec3(WIRE_WIDTH, WIRE_WIDTH, WIRE_WIDTH);
        renderCuboid(quads, center, centerSize, sprite);

        boolean anyConnections = false;

        for (EnumFacing face : EnumFacing.values()) {
            boolean positive = face.getAxisDirection() == AxisDirection.POSITIVE;
            Axis axis = face.getAxis();
            if (renderState.wireMatrix.isWireConnected(wire, face)) {
                anyConnections = true;
                Vec3 start = pos;
                Vec3 end = pos.add(centerSize);
                if (positive) {
                    start = Utils.withValue(start, axis, Utils.getValue(start, axis) + WIRE_WIDTH);
                    end = Utils.withValue(end, axis, 1);
                } else {
                    start = Utils.withValue(start, axis, 0);
                    end = Utils.withValue(end, axis, Utils.getValue(end, axis) - WIRE_WIDTH);
                }
                renderCuboid(quads, start, end.subtract(start), sprite);
            }
        }

        if (!anyConnections) {

        }
    }

    private static void renderCuboid(List<BakedQuad> quads, Vec3 min, Vec3 size, TextureAtlasSprite sprite) {
        Vec3 radius = Utils.multiply(size, 0.5);
        Vector3f radiusF = Utils.convertFloat(radius);
        Vector3f center = Utils.convertFloat(min.add(radius));
        for (EnumFacing face : EnumFacing.values()) {
            float[] uvs = new float[4];
            int neg = (face.ordinal() - 2) % 6;
            if (neg < 0) {
                neg += 6;
            }
            EnumFacing uFace = EnumFacing.VALUES[neg];
            EnumFacing vFace = EnumFacing.VALUES[(face.ordinal() + 2) % 6];
            if (face.getAxis() == Axis.Z) {
                EnumFacing holder = uFace;
                uFace = vFace;
                vFace = holder;
            }
            uvs[BuildCraftBakedModel.U_MIN] = sprite.getMinU();
            uvs[BuildCraftBakedModel.U_MAX] = sprite.getInterpolatedU(Utils.getValue(size, uFace.getAxis()) * 16);
            uvs[BuildCraftBakedModel.V_MIN] = sprite.getMinV();
            uvs[BuildCraftBakedModel.V_MAX] = sprite.getInterpolatedV(Utils.getValue(size, vFace.getAxis()) * 16);
            BuildCraftBakedModel.bakeDoubleFace(quads, face, center, radiusF, uvs);
        }
    }
}

package buildcraft.transport.render.tile;

import java.util.ArrayList;
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

import buildcraft.BuildCraftTransport;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.core.lib.client.model.BuildCraftBakedModel;
import buildcraft.core.lib.client.model.MutableQuad;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.PipeRenderState;

public class PipeRendererWires {
    private static EnumMap<PipeWire, Vec3> wirePosMap = Maps.newEnumMap(PipeWire.class);
    private static EnumMap<PipeWire, AxisDirection[]> wireDirectionMap = Maps.newEnumMap(PipeWire.class);
    private static final double WIRE_WIDTH = 0.05;
    // Offset all wires very slightly out of the pipe
    private static final double WIRE_OFFSET = 0.001;

    static {
        AxisDirection neg = AxisDirection.NEGATIVE;
        AxisDirection pos = AxisDirection.POSITIVE;

        wireDirectionMap.put(PipeWire.RED, new AxisDirection[] { neg, pos, neg });
        wireDirectionMap.put(PipeWire.BLUE, new AxisDirection[] { pos, pos, pos });
        wireDirectionMap.put(PipeWire.GREEN, new AxisDirection[] { pos, neg, neg });
        wireDirectionMap.put(PipeWire.YELLOW, new AxisDirection[] { neg, neg, pos });

        wirePosMap.put(PipeWire.RED, getOffset(PipeWire.RED));
        wirePosMap.put(PipeWire.BLUE, getOffset(PipeWire.BLUE));
        wirePosMap.put(PipeWire.GREEN, getOffset(PipeWire.GREEN));
        wirePosMap.put(PipeWire.YELLOW, getOffset(PipeWire.YELLOW));
    }

    private static Vec3 getOffset(PipeWire wire) {
        double min = CoreConstants.PIPE_MIN_POS - WIRE_WIDTH;
        double max = CoreConstants.PIPE_MAX_POS;

        // Offset each wire slightly differently to avoid z-fighting between wires
        int multiple = wire.ordinal() + 1;
        double offset = WIRE_OFFSET * multiple;
        double inset = -offset;

        AxisDirection[] axis = wireDirectionMap.get(wire);
        boolean[] axisPos = new boolean[3];
        for (int i = 0; i < 3; i++) {
            axisPos[i] = axis[i] == AxisDirection.POSITIVE;
        }

        Vec3 base = new Vec3(axisPos[0] ? max : min, axisPos[1] ? max : min, axisPos[2] ? max : min);
        return base.addVector(axisPos[0] ? inset : offset, axisPos[1] ? inset : offset, axisPos[2] ? inset : offset);
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

        boolean isLit = renderState.wireMatrix.isWireLit(wire);

        TextureAtlasSprite sprite = BuildCraftTransport.instance.wireIconProvider.getIcon(wire, isLit);

        List<MutableQuad> unprocessed = new ArrayList<>();

        Vec3 center = pos;
        Vec3 centerSize = new Vec3(WIRE_WIDTH, WIRE_WIDTH, WIRE_WIDTH);
        AxisDirection[] directions = wireDirectionMap.get(wire);
        int numFaces = 0;

        for (EnumFacing face : EnumFacing.values()) {
            boolean positive = face.getAxisDirection() == AxisDirection.POSITIVE;
            Axis axis = face.getAxis();
            AxisDirection wireCenter = directions[axis.ordinal()];
            if (renderState.wireMatrix.isWireConnected(wire, face)) {
                if (wireCenter == face.getAxisDirection()) {
                    numFaces++;
                }
                numFaces++;
                Vec3 start = pos;
                Vec3 end = pos.add(centerSize);
                if (positive) {
                    start = Utils.withValue(start, axis, Utils.getValue(start, axis) + WIRE_WIDTH);
                    end = Utils.withValue(end, axis, 1);
                } else {
                    start = Utils.withValue(start, axis, 0);
                    end = Utils.withValue(end, axis, Utils.getValue(end, axis) - WIRE_WIDTH);
                }
                renderCuboid(unprocessed, start, end.subtract(start), sprite);
            } else {
                boolean anyOther = false;
                for (EnumFacing face2 : EnumFacing.values()) {
                    if (face2.getOpposite() == face) {
                        continue;
                    }
                    anyOther |= renderState.wireMatrix.isWireConnected(wire, face2);
                }
                if (anyOther) {
                    continue;
                }
                Vec3 start = pos;
                Vec3 end = pos.add(centerSize);
                if (positive) {
                    start = Utils.withValue(start, axis, Utils.getValue(start, axis) + WIRE_WIDTH);
                    end = Utils.withValue(end, axis, CoreConstants.PIPE_MAX_POS);
                } else {
                    start = Utils.withValue(start, axis, CoreConstants.PIPE_MIN_POS);
                    end = Utils.withValue(end, axis, Utils.getValue(end, axis) - WIRE_WIDTH);
                }
                Vec3 size = end.subtract(start);
                if (size.lengthVector() > WIRE_WIDTH * 2) {
                    renderCuboid(unprocessed, start, size, sprite);
                }
            }
        }
        if (numFaces != 1) {
            renderCuboid(unprocessed, center, centerSize, sprite);
        }

        for (MutableQuad quad : unprocessed) {
            if (isLit) quad.lightf(1, 0);
            quad.setCalculatedDiffuse();
            quads.add(quad.toUnpacked());
        }
    }

    private static void renderCuboid(List<MutableQuad> quads, Vec3 min, Vec3 size, TextureAtlasSprite sprite) {
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
            BCModelHelper.appendQuads(quads, BCModelHelper.createDoubleFace(face, center, radiusF, uvs));
        }
    }
}

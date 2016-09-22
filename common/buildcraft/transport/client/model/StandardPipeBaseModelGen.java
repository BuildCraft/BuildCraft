package buildcraft.transport.client.model;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.*;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;

import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTransclucentKey;

public enum StandardPipeBaseModelGen implements IPipeBaseModelGen {
    INSTANCE;

    // Gen

    private static final MutableQuad[][][] QUADS;
    private static final MutableQuad[][][] QUADS_COLOURED;

    static {
        QUADS = new MutableQuad[2][][];
        QUADS_COLOURED = new MutableQuad[2][][];
        final double colourOffset = 0.01;
        Vec3d[] faceOffset = new Vec3d[6];
        for (EnumFacing face : EnumFacing.VALUES) {
            faceOffset[face.ordinal()] = new Vec3d(face.getOpposite().getDirectionVec()).scale(colourOffset);
        }

        // not connected
        QUADS[0] = new MutableQuad[6][2];
        QUADS_COLOURED[0] = new MutableQuad[6][2];
        Tuple3f center = new Point3f(0.5f, 0.5f, 0.5f);
        Tuple3f radius = new Vector3f(0.25f, 0.25f, 0.25f);
        float[] uvs = { 4 / 16f, 12 / 16f, 4 / 16f, 12 / 16f };
        for (EnumFacing face : EnumFacing.VALUES) {
            MutableQuad quad = BCModelHelper.createFace(face, center, radius, uvs);
            quad.setDiffuse(quad.getVertex(0).normal());
            QUADS[0][face.ordinal()][0] = quad;
            dupDarker(QUADS[0][face.ordinal()]);

            MutableQuad[] colQuads = BCModelHelper.createDoubleFace(face, center, radius, uvs);
            for (MutableQuad q : colQuads) {
                q.translatevd(faceOffset[face.ordinal()]);
            }
            QUADS_COLOURED[0][face.ordinal()] = colQuads;
        }

        int[][] uvsRot = {//
            { 2, 0, 3, 3 },//
            { 0, 2, 1, 1 },//
            { 2, 0, 0, 2 },//
            { 0, 2, 2, 0 },//
            { 3, 3, 0, 2 },//
            { 1, 1, 2, 0 } //
        };

        float[][] types = {//
            { 4, 12, 0, 4 },//
            { 4, 12, 12, 16 },//
            { 0, 4, 4, 12 },//
            { 12, 16, 4, 12 } //
        };

        for (float[] f2 : types) {
            for (int i = 0; i < f2.length; i++) {
                f2[i] /= 16f;
            }
        }
        // connected
        QUADS[1] = new MutableQuad[6][8];
        QUADS_COLOURED[1] = new MutableQuad[6][8];
        for (EnumFacing side : EnumFacing.VALUES) {
            center = new Point3f(//
                    side.getFrontOffsetX() * 0.375f,//
                    side.getFrontOffsetY() * 0.375f,//
                    side.getFrontOffsetZ() * 0.375f //
            );
            radius = new Vector3f(//
                    side.getAxis() == Axis.X ? 0.125f : 0.25f,//
                    side.getAxis() == Axis.Y ? 0.125f : 0.25f,//
                    side.getAxis() == Axis.Z ? 0.125f : 0.25f //
            );//
            center.add(new Point3f(0.5f, 0.5f, 0.5f));

            int i = 0;
            for (EnumFacing face : EnumFacing.VALUES) {
                if (face.getAxis() == side.getAxis()) continue;
                MutableQuad quad = BCModelHelper.createFace(face, center, radius, types[i]);
                quad.rotateTextureUp(uvsRot[side.ordinal()][i]);

                MutableQuad col = new MutableQuad(quad);

                quad.setDiffuse(quad.getVertex(0).normal());
                QUADS[1][side.ordinal()][i] = quad;

                col.translatevd(faceOffset[face.ordinal()]);
                QUADS_COLOURED[1][side.ordinal()][i++] = col;
            }
            dupDarker(QUADS[1][side.ordinal()]);
            dupInverted(QUADS_COLOURED[1][side.ordinal()]);
        }
    }

    private static void dupDarker(MutableQuad[] quads) {
        int halfLength = quads.length / 2;
        for (int i = 0; i < halfLength; i++) {
            int n = i + halfLength;
            MutableQuad from = quads[i];
            if (from != null) {
                MutableQuad to = new MutableQuad(from);
                to.invertNormal();
                to.setCalculatedDiffuse();
                for (MutableVertex v : to.verticies()) {
                    Point4f colour = v.colourv();
                    colour.scale(OPTION_INSIDE_COLOUR_MULT.getAsFloat());
                    colour.w = 1;
                    v.colourv(colour);
                }
                quads[n] = to;
            }
        }
    }

    private static void dupInverted(MutableQuad[] quads) {
        int halfLength = quads.length / 2;
        for (int i = 0; i < halfLength; i++) {
            int n = i + halfLength;
            MutableQuad from = quads[i];
            if (from != null) {
                MutableQuad to = new MutableQuad(from);
                to.invertNormal();
                quads[n] = to;
            }
        }
    }

    // Usage

    @Override
    public List<MutableQuad> generateCutout(PipeBaseCutoutKey key) {
        List<MutableQuad> quads = new ArrayList<>();

        for (EnumFacing face : EnumFacing.values()) {
            float size = key.connections[face.ordinal()];
            if (size > 0) {
                addQuads(QUADS[1][face.ordinal()], quads, key.sides[face.ordinal()]);
            } else {
                addQuads(QUADS[0][face.ordinal()], quads, key.center);
            }
        }
        return quads;
    }

    @Override
    public List<MutableQuad> generateTranslucent(PipeBaseTransclucentKey key) {
        if (!key.shouldRender()) return ImmutableList.of();
        List<MutableQuad> quads = new ArrayList<>();
        TextureAtlasSprite sprite = BCTransportSprites.PIPE_COLOUR.getSprite();

        for (EnumFacing face : EnumFacing.values()) {
            float size = key.connections[face.ordinal()];
            if (size > 0) {
                addQuads(QUADS_COLOURED[1][face.ordinal()], quads, sprite);
            } else {
                addQuads(QUADS_COLOURED[0][face.ordinal()], quads, sprite);
            }
        }
        int colour = 0xFF_00_00_00 | ColourUtil.swapArgbToAbgr(ColourUtil.getLightHex(key.colour));
        for (MutableQuad q : quads) {
            q.colouri(colour);
        }
        return quads;
    }

    private static void addQuads(MutableQuad[] from, List<MutableQuad> to, TextureAtlasSprite sprite) {
        for (MutableQuad f : from) {
            if (f == null) {
                continue;
            }
            MutableQuad copy = new MutableQuad(f);
            copy.setSprite(sprite);
            for (MutableVertex v : copy.verticies()) {
                Point2f tex = v.tex();
                v.texf(sprite.getInterpolatedU(tex.x * 16), sprite.getInterpolatedV(tex.y * 16));
            }
            to.add(copy);
        }
    }
}

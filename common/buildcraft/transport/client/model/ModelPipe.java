package buildcraft.transport.client.model;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.*;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import net.minecraftforge.common.property.IExtendedBlockState;

import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.client.model.key.PipeModelKey;

public enum ModelPipe implements IBakedModel {
    INSTANCE;

    private static final MutableQuad[][][] QUADS;
    // TODO: Colour

    static {
        QUADS = new MutableQuad[2][][];
        // not connected
        QUADS[0] = new MutableQuad[6][2];
        Tuple3f center = new Point3f(0.5f, 0.5f, 0.5f);
        Tuple3f radius = new Vector3f(0.25f, 0.25f, 0.25f);
        float[] uvs = { 4 / 16f, 12 / 16f, 4 / 16f, 12 / 16f };
        for (EnumFacing face : EnumFacing.VALUES) {
            MutableQuad quad = BCModelHelper.createFace(face, center, radius, uvs);
            QUADS[0][face.ordinal()][0] = quad;
            quad.normalf(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
            quad.setDiffuse(quad.getVertex(0).normal());
            dupDarker(QUADS[0][face.ordinal()]);
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
                quad.normalf(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
                quad.setDiffuse(quad.getVertex(0).normal());
                QUADS[1][side.ordinal()][i++] = quad;
            }
            dupDarker(QUADS[1][side.ordinal()]);
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
                    colour.scale(0.85f);
                    colour.w = 1;
                    v.colourv(colour);
                }
                quads[n] = to;
            }
        }
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) {
            return ImmutableList.of();
        }
        PipeModelKey key = null;
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            key = ext.getValue(BlockPipeHolder.PROP_MODEL);
        }
        if (key == null) {
            key = PipeModelKey.DEFAULT_KEY;
        }
        VertexFormat vf = DefaultVertexFormats.BLOCK;
        List<BakedQuad> quads = new ArrayList<>();
        for (EnumFacing face : EnumFacing.VALUES) {
            if (key.connected[face.ordinal()]) {
                addQuads(QUADS[1][face.ordinal()], quads, key.sides[face.ordinal()], vf);
            } else {
                addQuads(QUADS[0][face.ordinal()], quads, key.center, vf);
            }
        }

        return quads;
    }

    private static void addQuads(MutableQuad[] from, List<BakedQuad> to, TextureAtlasSprite sprite, VertexFormat vf) {
        for (MutableQuad f : from) {
            if (f == null) {
                continue;
            }
            MutableQuad copy = new MutableQuad(f);
            for (MutableVertex v : copy.verticies()) {
                Point2f tex = v.tex();
                v.texf(sprite.getInterpolatedU(tex.x * 16), sprite.getInterpolatedV(tex.y * 16));
            }
            to.add(copy.toUnpacked(vf));
        }
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}

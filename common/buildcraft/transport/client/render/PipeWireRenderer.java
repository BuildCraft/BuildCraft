package buildcraft.transport.client.render;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.transport.neptune.EnumWirePart;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.client.sprite.ISprite;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.EnumWireBetween;

public class PipeWireRenderer {

    private static final Map<EnumWirePart, MutableQuad[]> partQuads = new EnumMap<>(EnumWirePart.class);
    private static final Map<EnumWireBetween, MutableQuad[]> betweenQuads = new EnumMap<>(EnumWireBetween.class);

    private static final Map<EnumDyeColor, SpriteHolderRegistry.SpriteHolder> wireSprites = new EnumMap<>(EnumDyeColor.class);
    private static final int[] wireRenderingCache = new int[(EnumWireBetween.VALUES.length + EnumWirePart.VALUES.length) * ColourUtil.COLOURS.length * 2];

    final static int WIRE_COUNT = EnumWirePart.VALUES.length * ColourUtil.COLOURS.length * 2;

    static {
        Arrays.fill(wireRenderingCache, -1);

        for (EnumDyeColor color : EnumDyeColor.values()) {
            wireSprites.put(color, SpriteHolderRegistry.getHolder("buildcrafttransport:wires/" + color.getName()));
        }

        for (EnumWirePart part : EnumWirePart.VALUES) {
            partQuads.put(part, getQuads(part));
        }
        for (EnumWireBetween part : EnumWireBetween.VALUES) {
            betweenQuads.put(part, getQuads(part));
        }
    }

    public static void clearWireCache() {
        Arrays.fill(wireRenderingCache, -1);
    }

    private static MutableQuad[] getQuads(EnumWirePart part) {
        MutableQuad[] quads = new MutableQuad[6];

        Tuple3f center = new Point3f(//
            0.5f + (part.x.getOffset() * 4.51f / 16f),//
            0.5f + (part.y.getOffset() * 4.51f / 16f),//
            0.5f + (part.z.getOffset() * 4.51f / 16f) //
        );
        Tuple3f radius = new Point3f(1 / 32f, 1 / 32f, 1 / 32f);
        UvFaceData uvs = new UvFaceData();
        int off = func(part.x) * 4 + func(part.y) * 2 + func(part.z);
        uvs.uMin = off / 16f;
        uvs.uMax = (off + 1) / 16f;
        uvs.vMin = 0;
        uvs.vMax = 1 / 16f;
        for (EnumFacing face : EnumFacing.VALUES) {
            quads[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
        }
        return quads;
    }

    private static int func(EnumFacing.AxisDirection dir) {
        return dir == AxisDirection.POSITIVE ? 1 : 0;
    }

    private static MutableQuad[] getQuads(EnumWireBetween between) {
        // 4 rather than 6 -- don't render the end caps
        MutableQuad[] quads = new MutableQuad[4];

        int i = 0;

        Vec3d center;
        Vec3d radius;

        boolean ax = between.mainAxis == Axis.X;
        boolean ay = between.mainAxis == Axis.Y;
        boolean az = between.mainAxis == Axis.Z;

        if (between.to == null) {
            double cL = 0.5f - 4.51f / 16f;
            double cU = 0.5f + 4.51f / 16f;
            center = new Vec3d(//
                ax ? 0.5f : (between.xy ? cU : cL),//
                ay ? 0.5f : ((ax ? between.xy : between.yz) ? cU : cL),//
                az ? 0.5f : (between.yz ? cU : cL) //
            );
            double rC = 4.01f / 16f;
            double rN = 1f / 16f / 2;
            radius = new Vec3d(//
                ax ? rC : rN,//
                ay ? rC : rN,//
                az ? rC : rN //
            );
        } else {// we are a connection
            double cL = (8 - 4.51) / 16;
            double cU = (8 + 4.51) / 16;
            radius = new Vec3d(//
                ax ? 2.99 / 32 : 1 / 32.0,//
                ay ? 2.99 / 32 : 1 / 32.0,//
                az ? 2.99 / 32 : 1 / 32.0 //
            );
            center = new Vec3d(//
                ax ? (0.5 + 6.505 / 16 * between.to.getFrontOffsetX()) : (between.xy ? cU : cL),//
                ay ? (0.5 + 6.505 / 16 * between.to.getFrontOffsetY()) : ((ax ? between.xy : between.yz) ? cU : cL),//
                az ? (0.5 + 6.505 / 16 * between.to.getFrontOffsetZ()) : (between.yz ? cU : cL) //
            );
        }

        UvFaceData uvBase = new UvFaceData();
        uvBase.uMin = (float) VecUtil.getValue(center.subtract(radius), between.mainAxis);
        uvBase.uMax = (float) VecUtil.getValue(center.add(radius), between.mainAxis);
        uvBase.vMin = 0;
        uvBase.vMax = 1 / 16f;

        Tuple3f centerFloat = VecUtil.convertFloat(center);
        Tuple3f radiusFloat = VecUtil.convertFloat(radius);

        for (EnumFacing face : EnumFacing.VALUES) {
            if (face.getAxis() == between.mainAxis) {
                continue;
            }
            UvFaceData uvs = new UvFaceData(uvBase);

            Axis aAxis = between.mainAxis;
            Axis fAxis = face.getAxis();
            boolean fPositive = face.getAxisDirection() == AxisDirection.POSITIVE;

            int rotations = 0;
            boolean swapU = false;
            boolean swapV = false;

            if (aAxis == Axis.X) {
                swapV = fPositive;
            } else if (aAxis == Axis.Y) {
                rotations = 1;
                swapU = fAxis == Axis.X ? !fPositive : fPositive;
                swapV = fAxis == Axis.Z;
            } else {// aAxis == Axis.Z
                if (fAxis == Axis.Y) {
                    rotations = 1;
                }
                swapU = face == EnumFacing.DOWN;
                swapV = face != EnumFacing.EAST;
            }

            if (swapU) {
                float t = uvs.uMin;
                uvs.uMin = uvs.uMax;
                uvs.uMax = t;
            }
            if (swapV) {
                float t = uvs.vMin;
                uvs.vMin = uvs.vMax;
                uvs.vMax = t;
            }

            MutableQuad quad = ModelUtil.createFace(face, centerFloat, radiusFloat, uvs);
            if (rotations > 0) quad.rotateTextureUp(rotations);
            quads[i++] = quad;
        }
        return quads;
    }

    private static void renderQuads(MutableQuad[] quads, ISprite sprite, int level) {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vb = tessellator.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float vOffset = (level & 0xF) / 16f;
        for (MutableQuad q : quads) {
            renderVertex(vb, q.vertex_0, sprite, vOffset);
            renderVertex(vb, q.vertex_1, sprite, vOffset);
            renderVertex(vb, q.vertex_2, sprite, vOffset);
            renderVertex(vb, q.vertex_3, sprite, vOffset);
        }
        tessellator.draw();
    }

    private static void renderVertex(VertexBuffer vb, MutableVertex vertex, ISprite sprite, float vOffset) {
        vertex.renderPosition(vb);
        double u = sprite.getInterpU(vertex.tex_u);
        double v = sprite.getInterpV(vertex.tex_v + vOffset);
        vb.tex(u, v);
        vb.endVertex();
    }

    private static int compileQuads(MutableQuad[] quads, EnumDyeColor colour, boolean isOn) {
        int index = GlStateManager.glGenLists(1);
        GlStateManager.glNewList(index, GL11.GL_COMPILE);

        ISprite sprite = wireSprites.get(colour);
        /* Currently pipe wire only supports two states - on or off. However all the textures supply 16 different
         * states, which could (possibly) be used for making pipe wire use all 16 states that normal redstone does. This
         * just opens up the possibility in the future. */
        renderQuads(quads, sprite, isOn ? 15 : 0);

        GL11.glEndList();
        return index;
    }

    private static int getIndex(EnumWirePart part, EnumDyeColor colour, boolean isOn) {
        return part.ordinal() * 32 + colour.ordinal() * 2 + (isOn ? 1 : 0);
    }

    private static int getIndex(EnumWireBetween bet, EnumDyeColor colour, boolean isOn) {
        return WIRE_COUNT + bet.ordinal() * 32 + colour.ordinal() * 2 + (isOn ? 1 : 0);
    }

    private static int compileWire(EnumWirePart part, EnumDyeColor colour, boolean isOn) {
        return compileQuads(getQuads(part), colour, isOn);
    }

    private static int compileWire(EnumWireBetween between, EnumDyeColor colour, boolean isOn) {
        return compileQuads(getQuads(between), colour, isOn);
    }

    public static void renderWires(TilePipeHolder pipe, double x, double y, double z, VertexBuffer vb) {
        int combinedLight = pipe.getWorld().getCombinedLight(pipe.getPipePos(), 0);
        int skyLight = combinedLight >> 16 & 0xFFFF;
        int blockLight = combinedLight & 0xFFFF;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        for (Map.Entry<EnumWirePart, EnumDyeColor> partColor : pipe.getWireManager().parts.entrySet()) {
            EnumWirePart part = partColor.getKey();
            EnumDyeColor color = partColor.getValue();
            boolean isOn = pipe.wireManager.isPowered(part);
            int idx = getIndex(part, color, isOn);
            if (wireRenderingCache[idx] == -1) {
                wireRenderingCache[idx] = compileWire(part, color, isOn);
            }
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, isOn ? 240 : blockLight, skyLight);
            GlStateManager.callList(wireRenderingCache[idx]);
        }
        for (Map.Entry<EnumWireBetween, EnumDyeColor> betweenColor : pipe.getWireManager().betweens.entrySet()) {
            EnumWireBetween between = betweenColor.getKey();
            EnumDyeColor color = betweenColor.getValue();
            boolean isOn = pipe.wireManager.isPowered(between.parts[0]);
            int idx = getIndex(between, color, isOn);
            if (wireRenderingCache[idx] == -1) {
                wireRenderingCache[idx] = compileWire(between, color, isOn);
            }
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, isOn ? 240 : blockLight, skyLight);
            GlStateManager.callList(wireRenderingCache[idx]);
        }
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        /* Directly rendering (like with a gllist) changes the colour directly, so we need to change the opengl state
         * directly */
        GL11.glColor3f(1, 1, 1);
        GlStateManager.color(1, 1, 1, 1);
    }
}

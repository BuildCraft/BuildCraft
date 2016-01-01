package buildcraft.core.lib.render;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;

import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.utils.Utils;

public class RenderResizableCuboid extends Render<EntityResizableCuboid> {
    public interface IBlockLocation {
        Vec3 transformToWorld(Vec3 vec);
    }

    public interface IFacingLocation {
        EnumFacing transformToWorld(EnumFacing face);
    }

    public enum DefaultFacingLocation implements IFacingLocation {
        INSTANCE;

        @Override
        public EnumFacing transformToWorld(EnumFacing face) {
            return face;
        }
    }

    public static class RotatedFacingLocation implements IFacingLocation {
        private final FacingRotationHelper helper;
        private final EnumFacing to;

        public RotatedFacingLocation(EnumFacing modelDirection, EnumFacing face) {
            helper = FacingRotationHelper.helperForFace(modelDirection);
            this.to = face;
        }

        @Override
        public EnumFacing transformToWorld(EnumFacing face) {
            return helper.rotateFace(to, face);
        }
    }

    public enum EnumShadeType {
        FACE(DefaultVertexFormats.COLOR_4UB),
        LIGHT(DefaultVertexFormats.TEX_2S),
        AMBIENT_OCCLUSION(DefaultVertexFormats.COLOR_4UB);

        private final VertexFormatElement element;

        private EnumShadeType(VertexFormatElement element) {
            this.element = element;
        }
    }

    public enum EnumShadeArgument {
        NONE,
        FACE(EnumShadeType.FACE),
        FACE_LIGHT(EnumShadeType.FACE, EnumShadeType.LIGHT),
        FACE_OCCLUDE(EnumShadeType.FACE, EnumShadeType.AMBIENT_OCCLUSION),
        FACE_LIGHT_OCCLUDE(EnumShadeType.FACE, EnumShadeType.LIGHT, EnumShadeType.AMBIENT_OCCLUSION),
        LIGHT(EnumShadeType.LIGHT),
        LIGHT_OCCLUDE(EnumShadeType.LIGHT, EnumShadeType.AMBIENT_OCCLUSION),
        OCCLUDE(EnumShadeType.AMBIENT_OCCLUSION);

        public final ImmutableSet<EnumShadeType> types;
        final VertexFormat vertexFormat;

        EnumShadeArgument(EnumShadeType... types) {
            this.vertexFormat = new VertexFormat();
            vertexFormat.addElement(DefaultVertexFormats.POSITION_3F);
            vertexFormat.addElement(DefaultVertexFormats.TEX_2F);
            for (EnumShadeType type : types) {
                if (!vertexFormat.getElements().contains(type.element)) vertexFormat.addElement(type.element);
            }
            this.types = ImmutableSet.copyOf(types);
        }

        public boolean isEnabled(EnumShadeType type) {
            return types.contains(type);
        }
    }

    public static final RenderResizableCuboid INSTANCE = new RenderResizableCuboid();

    /** The AO map assumes that each direction in the world has a different amount of light going towards it. */
    private static final Map<EnumFacing, Vec3> aoMap = Maps.newEnumMap(EnumFacing.class);

    private static final int U_MIN = 0;
    private static final int U_MAX = 1;
    private static final int V_MIN = 2;
    private static final int V_MAX = 3;

    static {
        // Static constants taken directly from minecraft's block renderer
        // ( net.minecraft.client.renderer.BlockModelRenderer.EnumNeighborInfo )
        aoMap.put(EnumFacing.UP, Utils.vec3(1));
        aoMap.put(EnumFacing.DOWN, Utils.vec3(0.5));
        aoMap.put(EnumFacing.NORTH, Utils.vec3(0.8));
        aoMap.put(EnumFacing.SOUTH, Utils.vec3(0.8));
        aoMap.put(EnumFacing.EAST, Utils.vec3(0.6));
        aoMap.put(EnumFacing.WEST, Utils.vec3(0.6));
    }

    protected RenderResizableCuboid() {
        super(Minecraft.getMinecraft().getRenderManager());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityResizableCuboid entity) {
        return null;
    }

    @Override
    public boolean shouldRender(EntityResizableCuboid entity, ICamera camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void doRender(EntityResizableCuboid entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        final Vec3 entPos = Utils.getInterpolatedVec(entity, partialTicks);
        IBlockLocation formula = new IBlockLocation() {
            @Override
            public Vec3 transformToWorld(Vec3 vec) {
                return entPos.add(vec);
            }
        };
        renderCube((EntityResizableCuboid) entity, EnumShadeArgument.FACE_LIGHT, formula, null);
        GL11.glPopMatrix();
    }

    /** This will render a cuboid from its middle. */
    public void renderCubeFromCentre(EntityResizableCuboid cuboid) {
        GL11.glPushMatrix();
        GL11.glTranslated(-cuboid.xSize / 2d, -cuboid.ySize / 2d, -cuboid.zSize / 2d);
        renderCube(cuboid, EnumShadeArgument.NONE, null, null);
        GL11.glPopMatrix();
    }

    public void renderCube(EntityResizableCuboid cuboid) {
        renderCube(cuboid, EnumShadeArgument.NONE, null, null);
    }

    public void renderCube(EntityResizableCuboid cube, EnumShadeArgument shadeTypes, IBlockLocation formula, IFacingLocation faceFormula) {
        if (faceFormula == null) {
            faceFormula = DefaultFacingLocation.INSTANCE;
        }

        TextureAtlasSprite[] sprites = cube.textures;
        if (sprites == null) {
            sprites = new TextureAtlasSprite[6];
            for (int i = 0; i < 6; i++) {
                sprites[i] = cube.texture;
            }
        }

        int[] flips = cube.textureFlips;
        if (flips == null) {
            flips = new int[6];
        }

        Vec3 textureStart = new Vec3(cube.textureStartX / 16D, cube.textureStartY / 16D, cube.textureStartZ / 16D);
        Vec3 textureSize = new Vec3(cube.textureSizeX / 16D, cube.textureSizeY / 16D, cube.textureSizeZ / 16D);
        Vec3 textureOffset = new Vec3(cube.textureOffsetX / 16D, cube.textureOffsetY / 16D, cube.textureOffsetZ / 16D);
        Vec3 size = new Vec3(cube.xSize, cube.ySize, cube.zSize);

        bindTexture(cube.resource == null ? TextureMap.locationBlocksTexture : cube.resource);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableLighting();

        wr.begin(GL11.GL_QUADS, shadeTypes.vertexFormat);

        for (EnumFacing face : EnumFacing.values()) {
            renderCuboidFace(wr, face, sprites, flips, textureStart, textureSize, size, textureOffset, shadeTypes, formula, faceFormula,
                    cube.worldObj);
        }

        tess.draw();

        GlStateManager.disableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.enableFog();
    }

    private void renderCuboidFace(WorldRenderer wr, EnumFacing face, TextureAtlasSprite[] sprites, int[] flips, Vec3 textureStart, Vec3 textureSize,
            Vec3 size, Vec3 textureOffset, EnumShadeArgument shadeTypes, IBlockLocation locationFormula, IFacingLocation faceFormula,
            IBlockAccess access) {
        int ordinal = face.ordinal();
        if (sprites[ordinal] == null) {
            return;
        }

        Vec3 textureEnd = textureStart.add(textureSize);
        float[] uv = getUVArray(sprites[ordinal], flips[ordinal], face, textureStart, textureEnd);
        List<RenderInfo> renderInfoList = getRenderInfos(uv, face, size, textureSize, textureOffset);

        Axis u = face.getAxis() == Axis.X ? Axis.Z : Axis.X;
        Axis v = face.getAxis() == Axis.Y ? Axis.Z : Axis.Y;
        double other = face.getAxisDirection() == AxisDirection.POSITIVE ? Utils.getValue(size, face.getAxis()) : 0;

        /* Swap the face if this is positive: the renderer returns indexes that ALWAYS are for the negative face, so
         * light it properly this way */
        face = face.getAxisDirection() == AxisDirection.NEGATIVE ? face : face.getOpposite();

        EnumFacing opposite = face.getOpposite();

        for (RenderInfo ri : renderInfoList) {
            renderPoint(wr, face, u, v, other, ri, true, false, locationFormula, faceFormula, access, shadeTypes);
            renderPoint(wr, face, u, v, other, ri, true, true, locationFormula, faceFormula, access, shadeTypes);
            renderPoint(wr, face, u, v, other, ri, false, true, locationFormula, faceFormula, access, shadeTypes);
            renderPoint(wr, face, u, v, other, ri, false, false, locationFormula, faceFormula, access, shadeTypes);

            renderPoint(wr, opposite, u, v, other, ri, false, false, locationFormula, faceFormula, access, shadeTypes);
            renderPoint(wr, opposite, u, v, other, ri, false, true, locationFormula, faceFormula, access, shadeTypes);
            renderPoint(wr, opposite, u, v, other, ri, true, true, locationFormula, faceFormula, access, shadeTypes);
            renderPoint(wr, opposite, u, v, other, ri, true, false, locationFormula, faceFormula, access, shadeTypes);
        }
    }

    private void renderPoint(WorldRenderer wr, EnumFacing face, Axis u, Axis v, double other, RenderInfo ri, boolean minU, boolean minV,
            IBlockLocation locationFormula, IFacingLocation faceFormula, IBlockAccess access, EnumShadeArgument shadeTypes) {
        int U_ARRAY = minU ? U_MIN : U_MAX;
        int V_ARRAY = minV ? V_MIN : V_MAX;

        Vec3 vertex = Utils.withValue(Utils.VEC_ZERO, u, ri.xyz[U_ARRAY]);
        vertex = Utils.withValue(vertex, v, ri.xyz[V_ARRAY]);
        vertex = Utils.withValue(vertex, face.getAxis(), other);

        wr.pos(vertex.xCoord, vertex.yCoord, vertex.zCoord);
        wr.tex(ri.uv[U_ARRAY], ri.uv[V_ARRAY]);

        if (shadeTypes.isEnabled(EnumShadeType.FACE)) {
            RenderUtils.setWorldRendererRGB(wr, aoMap.get(faceFormula.transformToWorld(face)));
        }

        if (shadeTypes.isEnabled(EnumShadeType.AMBIENT_OCCLUSION)) {
            applyLocalAO(wr, faceFormula.transformToWorld(face), locationFormula, access, shadeTypes, vertex);
        } else if (shadeTypes.isEnabled(EnumShadeType.LIGHT)) {
            Vec3 transVertex = locationFormula.transformToWorld(vertex);
            BlockPos pos = Utils.convertFloor(transVertex);
            Block block = access.getBlockState(pos).getBlock();
            int combindedLight = block.getMixedBrightnessForBlock(access, pos);
            wr.lightmap(combindedLight >> 16 & 65535, combindedLight & 65535);
        }

        wr.endVertex();
    }

    private void applyLocalAO(WorldRenderer wr, EnumFacing face, IBlockLocation locationFormula, IBlockAccess access, EnumShadeArgument shadeTypes,
            Vec3 vertex) {
        // This doesn't work. At all.
        boolean allAround = false;

        int numPositions = allAround ? 7 : 5;
        int[] skyLight = new int[numPositions];
        int[] blockLight = new int[numPositions];
        float[] colorMultiplier = new float[numPositions];
        double[] distances = new double[numPositions];
        double totalDist = 0;
        Vec3 transVertex = locationFormula.transformToWorld(vertex);
        BlockPos pos = Utils.convertFloor(transVertex);
        Block block = access.getBlockState(pos).getBlock();
        int combindedLight = block.getMixedBrightnessForBlock(access, pos);

        skyLight[0] = combindedLight / 0x10000;
        blockLight[0] = combindedLight % 0x10000;
        colorMultiplier[0] = block.getAmbientOcclusionLightValue();
        distances[0] = transVertex.distanceTo(Utils.convertMiddle(pos));

        int index = 0;
        EnumFacing[] testArray = allAround ? EnumFacing.values() : Utils.getNeighbours(face);
        for (EnumFacing otherFace : testArray) {
            Vec3 nearestOther = vertex.add(Utils.convert(otherFace));
            pos = Utils.convertFloor(locationFormula.transformToWorld(nearestOther));
            block = access.getBlockState(pos).getBlock();
            combindedLight = block.getMixedBrightnessForBlock(access, pos);

            index++;

            skyLight[index] = (int) (combindedLight / 0x10000);
            blockLight[index] = (int) (combindedLight % 0x10000);
            colorMultiplier[index] = block.getAmbientOcclusionLightValue();
            // The extra 0.1 is to stop any 1 divided by 0 errors
            distances[index] = 1 / (transVertex.distanceTo(Utils.convertMiddle(pos)) + 0.1);
            totalDist += distances[index];
        }

        double avgBlockLight = 0;
        double avgSkyLight = 0;
        float avgColorMultiplier = 0;
        for (int i = 0; i < numPositions; i++) {
            double part = distances[i] / totalDist;
            avgBlockLight += blockLight[i] * part;
            avgSkyLight += skyLight[i] * part;
            avgColorMultiplier += colorMultiplier[i] * part;
        }

        if (shadeTypes.isEnabled(EnumShadeType.LIGHT)) {
            int capBlockLight = (int) avgBlockLight;
            int capSkyLight = (int) avgSkyLight;
            wr.lightmap(capBlockLight, capSkyLight);
        }

        Vec3 color;
        if (shadeTypes.isEnabled(EnumShadeType.FACE)) {
            color = aoMap.get(face);
        } else {
            color = Utils.VEC_ONE;
        }
        color = Utils.multiply(color, avgColorMultiplier);
        RenderUtils.setWorldRendererRGB(wr, color);
    }

    /** Note that this method DOES take into account its position. But not its rotation. (Open an issue on github if you
     * need rotation, and a second method will be made that does all the trig required) */
    public void renderCubeStatic(List<BakedQuad> quads, EntityResizableCuboid cuboid) {
        TextureAtlasSprite[] sprites = cuboid.textures;
        if (sprites == null) {
            sprites = new TextureAtlasSprite[6];
            for (int i = 0; i < 6; i++) {
                sprites[i] = cuboid.texture;
            }
        }

        int[] flips = cuboid.textureFlips;
        if (flips == null) {
            flips = new int[6];
        }

        double textureStartX = cuboid.textureStartX / 16D;
        double textureStartY = cuboid.textureStartY / 16D;
        double textureStartZ = cuboid.textureStartZ / 16D;

        double textureSizeX = cuboid.textureSizeX / 16D;
        double textureSizeY = cuboid.textureSizeY / 16D;
        double textureSizeZ = cuboid.textureSizeZ / 16D;

        double textureEndX = textureSizeX + textureStartX;
        double textureEndY = textureSizeY + textureStartY;
        double textureEndZ = textureSizeZ + textureStartZ;

        double textureOffsetX = cuboid.textureOffsetX / 16D;
        double textureOffsetY = cuboid.textureOffsetY / 16D;
        double textureOffsetZ = cuboid.textureOffsetZ / 16D;

        double sizeX = cuboid.xSize;
        double sizeY = cuboid.ySize;
        double sizeZ = cuboid.zSize;

        if (sprites[0] != null) {
            // Down
            float[] uv = getUVArray(sprites[0], flips[0], textureStartX, textureEndX, textureStartZ, textureEndZ);
            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeZ, textureSizeX, textureSizeZ, textureOffsetX, textureOffsetZ)) {
                ri = ri.offset(cuboid, Axis.Y);
                double[][] arr = new double[4][];
                arr[0] = new double[] { ri.xyz[U_MAX], cuboid.posY, ri.xyz[V_MIN], -1, ri.uv[U_MAX], ri.uv[V_MIN], 0 };
                arr[1] = new double[] { ri.xyz[U_MAX], cuboid.posY, ri.xyz[V_MAX], -1, ri.uv[U_MAX], ri.uv[V_MAX], 0 };
                arr[2] = new double[] { ri.xyz[U_MIN], cuboid.posY, ri.xyz[V_MAX], -1, ri.uv[U_MIN], ri.uv[V_MAX], 0 };
                arr[3] = new double[] { ri.xyz[U_MIN], cuboid.posY, ri.xyz[V_MIN], -1, ri.uv[U_MIN], ri.uv[V_MIN], 0 };
                convertToDoubleQuads(quads, arr, EnumFacing.DOWN);
            }
        }

        if (sprites[1] != null) {
            // Up
            float[] uv = getUVArray(sprites[1], flips[1], textureStartX, textureEndX, textureStartZ, textureEndZ);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeZ, textureSizeX, textureSizeZ, textureOffsetX, textureOffsetZ)) {
                ri = ri.offset(cuboid, Axis.Y);
                double[][] arr = new double[4][];
                arr[0] = new double[] { ri.xyz[U_MAX], sizeY + cuboid.posY, ri.xyz[V_MIN], -1, ri.uv[U_MAX], ri.uv[V_MIN], 0 };
                arr[1] = new double[] { ri.xyz[U_MAX], sizeY + cuboid.posY, ri.xyz[V_MAX], -1, ri.uv[U_MAX], ri.uv[V_MAX], 0 };
                arr[2] = new double[] { ri.xyz[U_MIN], sizeY + cuboid.posY, ri.xyz[V_MAX], -1, ri.uv[U_MIN], ri.uv[V_MAX], 0 };
                arr[3] = new double[] { ri.xyz[U_MIN], sizeY + cuboid.posY, ri.xyz[V_MIN], -1, ri.uv[U_MIN], ri.uv[V_MIN], 0 };
                convertToDoubleQuads(quads, arr, EnumFacing.UP);
            }
        }

        if (sprites[2] != null) {
            // North (-Z)
            float[] uv = getUVArray(sprites[2], flips[2], textureStartX, textureEndX, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeY, textureSizeX, textureSizeY, textureOffsetX, textureOffsetY)) {
                ri = ri.offset(cuboid, Axis.Z);
                double[][] arr = new double[4][];
                arr[0] = new double[] { ri.xyz[U_MAX], ri.xyz[V_MIN], cuboid.posZ, -1, ri.uv[U_MAX], ri.uv[V_MIN], 0 };
                arr[1] = new double[] { ri.xyz[U_MAX], ri.xyz[V_MAX], cuboid.posZ, -1, ri.uv[U_MAX], ri.uv[V_MAX], 0 };
                arr[2] = new double[] { ri.xyz[U_MIN], ri.xyz[V_MAX], cuboid.posZ, -1, ri.uv[U_MIN], ri.uv[V_MAX], 0 };
                arr[3] = new double[] { ri.xyz[U_MIN], ri.xyz[V_MIN], cuboid.posZ, -1, ri.uv[U_MIN], ri.uv[V_MIN], 0 };
                convertToDoubleQuads(quads, arr, EnumFacing.NORTH);
            }
        }

        if (sprites[3] != null) {
            // South (+Z)
            float[] uv = getUVArray(sprites[3], flips[3], textureStartX, textureEndX, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeY, textureSizeX, textureSizeY, textureOffsetX, textureOffsetY)) {
                ri = ri.offset(cuboid, Axis.Z);
                double[][] arr = new double[4][];
                arr[0] = new double[] { ri.xyz[U_MAX], ri.xyz[V_MIN], cuboid.posZ + sizeZ, -1, ri.uv[U_MAX], ri.uv[V_MIN], 0 };
                arr[1] = new double[] { ri.xyz[U_MAX], ri.xyz[V_MAX], cuboid.posZ + sizeZ, -1, ri.uv[U_MAX], ri.uv[V_MAX], 0 };
                arr[2] = new double[] { ri.xyz[U_MIN], ri.xyz[V_MAX], cuboid.posZ + sizeZ, -1, ri.uv[U_MIN], ri.uv[V_MAX], 0 };
                arr[3] = new double[] { ri.xyz[U_MIN], ri.xyz[V_MIN], cuboid.posZ + sizeZ, -1, ri.uv[U_MIN], ri.uv[V_MIN], 0 };
                convertToDoubleQuads(quads, arr, EnumFacing.SOUTH);
            }
        }

        if (sprites[4] != null) {
            // West (-X)
            float[] uv = getUVArray(sprites[4], flips[4], textureStartZ, textureEndZ, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeZ, sizeY, textureSizeZ, textureSizeY, textureOffsetZ, textureOffsetY)) {
                ri = ri.offset(cuboid, Axis.X);
                double[][] arr = new double[4][];
                arr[0] = new double[] { cuboid.posX, ri.xyz[V_MIN], ri.xyz[U_MAX], -1, ri.uv[U_MAX], ri.uv[V_MIN], 0 };
                arr[1] = new double[] { cuboid.posX, ri.xyz[V_MAX], ri.xyz[U_MAX], -1, ri.uv[U_MAX], ri.uv[V_MAX], 0 };
                arr[2] = new double[] { cuboid.posX, ri.xyz[V_MAX], ri.xyz[U_MIN], -1, ri.uv[U_MIN], ri.uv[V_MAX], 0 };
                arr[3] = new double[] { cuboid.posX, ri.xyz[V_MIN], ri.xyz[U_MIN], -1, ri.uv[U_MIN], ri.uv[V_MIN], 0 };
                convertToDoubleQuads(quads, arr, EnumFacing.WEST);
            }
        }

        if (sprites[5] != null) {
            // East (+X)
            float[] uv = getUVArray(sprites[5], flips[5], textureStartZ, textureEndZ, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeZ, sizeY, textureSizeZ, textureSizeY, textureOffsetZ, textureOffsetY)) {
                ri = ri.offset(cuboid, Axis.X);
                double[][] arr = new double[4][];
                arr[0] = new double[] { cuboid.posX + sizeX, ri.xyz[V_MIN], ri.xyz[U_MAX], -1, ri.uv[U_MAX], ri.uv[V_MIN], 0 };
                arr[1] = new double[] { cuboid.posX + sizeX, ri.xyz[V_MAX], ri.xyz[U_MAX], -1, ri.uv[U_MAX], ri.uv[V_MAX], 0 };
                arr[2] = new double[] { cuboid.posX + sizeX, ri.xyz[V_MAX], ri.xyz[U_MIN], -1, ri.uv[U_MIN], ri.uv[V_MAX], 0 };
                arr[3] = new double[] { cuboid.posX + sizeX, ri.xyz[V_MIN], ri.xyz[U_MIN], -1, ri.uv[U_MIN], ri.uv[V_MIN], 0 };
                convertToDoubleQuads(quads, arr, EnumFacing.EAST);
            }
        }
    }

    private void convertToDoubleQuads(List<BakedQuad> quads, double[][] points, EnumFacing face) {
        BakedQuad quad = convertToQuad(points, face);
        quads.add(quad);

        double[][] otherPoints = new double[][] { points[3], points[2], points[1], points[0] };
        quad = convertToQuad(otherPoints, face);
        quads.add(quad);
    }

    private BakedQuad convertToQuad(double[][] points, EnumFacing face) {
        int[] list = new int[points.length * points[0].length];
        for (int i = 0; i < points.length; i++) {
            double[] arr = points[i];
            for (int j = 0; j < arr.length; j++) {
                double d = arr[j];
                int used = 0;
                if (j == 3 || j == 6) {// Shade or unused
                    used = (int) d;
                } else {
                    used = Float.floatToRawIntBits((float) d);
                }
                list[i * arr.length + j] = used;
            }
        }
        return new BakedQuad(list, -1, face);
    }

    /** Returns an array containing [uMin, uMax, vMin, vMax]. start* and end* must be doubles between 0 and 1 */
    private float[] getUVArray(TextureAtlasSprite sprite, int flips, double startU, double endU, double startV, double endV) {
        float minU = sprite.getInterpolatedU(startU * 16);
        float maxU = sprite.getInterpolatedU(endU * 16);
        float minV = sprite.getInterpolatedV(startV * 16);
        float maxV = sprite.getInterpolatedV(endV * 16);
        float[] uvarray = new float[] { minU, maxU, minV, maxV };
        if (flips % 2 == 1) {
            float holder = uvarray[0];
            uvarray[0] = uvarray[1];
            uvarray[1] = holder;
        }
        if (flips >> 1 % 2 == 1) {
            float holder = uvarray[2];
            uvarray[2] = uvarray[3];
            uvarray[3] = holder;
        }
        return uvarray;
    }

    private float[] getUVArray(TextureAtlasSprite sprite, int flips, EnumFacing face, Vec3 start, Vec3 end) {
        Axis u = face.getAxis() == Axis.X ? Axis.Z : Axis.X;
        Axis v = face.getAxis() == Axis.Y ? Axis.Z : Axis.Y;

        float minU = sprite.getInterpolatedU(Utils.getValue(start, u) * 16);
        float maxU = sprite.getInterpolatedU(Utils.getValue(end, u) * 16);
        float minV = sprite.getInterpolatedV(Utils.getValue(start, v) * 16);
        float maxV = sprite.getInterpolatedV(Utils.getValue(end, v) * 16);

        float[] uvarray = new float[] { minU, maxU, minV, maxV };
        if (flips % 2 == 1) {
            float holder = uvarray[0];
            uvarray[0] = uvarray[1];
            uvarray[1] = holder;
        }
        if (flips >> 1 % 2 == 1) {
            float holder = uvarray[2];
            uvarray[2] = uvarray[3];
            uvarray[3] = holder;
        }
        return uvarray;
    }

    private List<RenderInfo> getRenderInfos(float[] uv, EnumFacing face, Vec3 size, Vec3 texSize, Vec3 texOffset) {
        Axis u = face.getAxis() == Axis.X ? Axis.Z : Axis.X;
        Axis v = face.getAxis() == Axis.Y ? Axis.Z : Axis.Y;

        double sizeU = Utils.getValue(size, u);
        double sizeV = Utils.getValue(size, v);
        double textureSizeU = Utils.getValue(texSize, u);
        double textureSizeV = Utils.getValue(texSize, v);
        double textureOffsetU = Utils.getValue(texOffset, u);
        double textureOffsetV = Utils.getValue(texOffset, v);

        return getRenderInfos(uv, sizeU, sizeV, textureSizeU, textureSizeV, textureOffsetU, textureOffsetV);
    }

    /** A way to automatically generate the different positions given the same arguments.
     * 
     * @param rotation TODO */
    private List<RenderInfo> getRenderInfos(float[] uv, double sizeU, double sizeV, double textureSizeU, double textureSizeV, double textureOffsetU,
            double textureOffsetV) {

        List<RenderInfo> infos = Lists.newArrayList();
        boolean firstU = true;
        for (double u = 0; u < sizeU; u += textureSizeU) {
            float[] uvCu = Arrays.copyOf(uv, 4);
            double addU = textureSizeU;
            boolean lowerU = false;

            // If there is an offset then make sure the texture positions are changed properly
            if (firstU && textureOffsetU != 0) {
                uvCu[U_MIN] = uvCu[U_MIN] + (uvCu[U_MAX] - uvCu[U_MIN]) * (float) textureOffsetU;
                addU -= textureOffsetU;
                // addU = 1 - textureOffsetU;
                lowerU = true;
            }

            // If the size of the texture is greater than the cuboid goes on for then make sure the texture
            // positions are lowered
            if (u + addU > sizeU) {
                addU = sizeU - u;
                if (firstU && textureOffsetU != 0) {
                    uvCu[U_MAX] = uvCu[U_MIN] + (uvCu[U_MAX] - uvCu[U_MIN]) * (float) (addU / (textureSizeU - textureOffsetU));
                } else {
                    uvCu[U_MAX] = uvCu[U_MIN] + (uvCu[U_MAX] - uvCu[U_MIN]) * (float) (addU / textureSizeU);
                }
            }
            firstU = false;
            boolean firstV = true;
            for (double v = 0; v < sizeV; v += textureSizeV) {
                float[] uvCv = Arrays.copyOf(uvCu, 4);

                double addV = textureSizeV;

                boolean lowerV = false;

                if (firstV && textureOffsetV != 0) {
                    uvCv[V_MIN] = uvCv[V_MIN] + (uvCv[V_MAX] - uvCv[V_MIN]) * (float) textureOffsetV;
                    addV -= textureOffsetV;
                    lowerV = true;
                }
                if (v + addV > sizeV) {
                    addV = sizeV - v;
                    if (firstV && textureOffsetV != 0) {
                        uvCv[V_MAX] = uvCv[V_MIN] + (uvCv[V_MAX] - uvCv[V_MIN]) * (float) (addV / (textureSizeV - textureOffsetV));
                    } else {
                        uvCv[V_MAX] = uvCv[V_MIN] + (uvCv[V_MAX] - uvCv[V_MIN]) * (float) (addV / textureSizeV);
                    }
                }

                double[] xyz = new double[4];
                xyz[U_MIN] = u;
                xyz[U_MAX] = u + addU;
                xyz[V_MIN] = v;
                xyz[V_MAX] = v + addV;
                infos.add(new RenderInfo(uvCv, xyz));

                if (lowerV) {
                    v -= textureOffsetV;
                }
                firstV = false;
            }
            // If we lowered the U because the cuboid started on an offset, reset it back to what was actually
            // rendered, not what the for loop assumes
            if (lowerU) {
                u -= textureOffsetU;
            }
        }
        return infos;
    }

    private static final class RenderInfo {
        private final float[] uv;
        private final double[] xyz;

        public RenderInfo(float[] uv, double[] xyz) {
            this.uv = uv;
            this.xyz = xyz;
        }

        public RenderInfo offset(Entity ent, Axis axis) {
            switch (axis) {
                case X: {
                    return new RenderInfo(uv, new double[] { xyz[0] + ent.posZ, xyz[1] + ent.posZ, xyz[2] + ent.posY, xyz[3] + ent.posY });
                }
                case Y: {
                    return new RenderInfo(uv, new double[] { xyz[0] + ent.posX, xyz[1] + ent.posX, xyz[2] + ent.posZ, xyz[3] + ent.posZ });
                }
                case Z: {
                    return new RenderInfo(uv, new double[] { xyz[0] + ent.posX, xyz[1] + ent.posX, xyz[2] + ent.posY, xyz[3] + ent.posY });
                }
            }
            return new RenderInfo(uv, xyz);
        }
    }
}

package buildcraft.core.lib.client.render;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.client.model.FacingRotationHelper;
import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.misc.VecUtil;

public class RenderResizableCuboid extends Render<EntityResizableCuboid> {
    public interface IBlockLocation {
        Vec3d transformToWorld(Vec3d vec);
    }

    public interface IFacingLocation {
        EnumFacing transformToWorld(EnumFacing face);
    }

    public enum DefaultFacingLocation implements IFacingLocation {
        FACING_INSTANCE;

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
    private static final Map<EnumFacing, Vec3d> aoMap = Maps.newEnumMap(EnumFacing.class);

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
        final Vec3d entPos = Utils.getInterpolatedVec(entity, partialTicks);
        IBlockLocation formula = new IBlockLocation() {
            @Override
            public Vec3d transformToWorld(Vec3d vec) {
                return entPos.add(vec);
            }
        };
        RenderHelper.disableStandardItemLighting();
        renderCube(entity, EnumShadeArgument.FACE_LIGHT, formula, null, true, true);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
    }

    /** This will render a cuboid from its middle. */
    public void renderCubeFromCentre(EntityResizableCuboid cuboid) {
        GL11.glPushMatrix();
        GL11.glTranslated(-cuboid.xSize / 2d, -cuboid.ySize / 2d, -cuboid.zSize / 2d);
        renderCube(cuboid, EnumShadeArgument.NONE, null, null, true, true);
        GL11.glPopMatrix();
    }

    public void renderCube(EntityResizableCuboid cuboid) {
        renderCube(cuboid, EnumShadeArgument.NONE, null, null, true, true);
    }

    public void renderCube(EntityResizableCuboid cuboid, boolean renderOut, boolean renderIn) {
        renderCube(cuboid, EnumShadeArgument.NONE, null, null, renderOut, renderIn);
    }

    public void renderCube(EntityResizableCuboid cube, EnumShadeArgument shadeTypes, IBlockLocation formula, IFacingLocation faceFormula) {
        renderCube(cube, shadeTypes, formula, faceFormula, true, true);
    }

    public void renderCube(EntityResizableCuboid cube, EnumShadeArgument shadeTypes, IBlockLocation formula, IFacingLocation faceFormula,
            boolean renderOut, boolean renderIn) {
        if (faceFormula == null) {
            faceFormula = DefaultFacingLocation.FACING_INSTANCE;
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

        Vec3d textureStart = new Vec3d(cube.textureStartX / 16D, cube.textureStartY / 16D, cube.textureStartZ / 16D);
        Vec3d textureSize = new Vec3d(cube.textureSizeX / 16D, cube.textureSizeY / 16D, cube.textureSizeZ / 16D);
        Vec3d textureOffset = new Vec3d(cube.textureOffsetX / 16D, cube.textureOffsetY / 16D, cube.textureOffsetZ / 16D);
        Vec3d size = new Vec3d(cube.xSize, cube.ySize, cube.zSize);

        bindTexture(cube.resource == null ? TextureMap.LOCATION_BLOCKS_TEXTURE : cube.resource);

        Tessellator tess = Tessellator.getInstance();
        VertexBuffer vb = tess.getBuffer();

        vb.begin(GL11.GL_QUADS, shadeTypes.vertexFormat);

        for (EnumFacing face : EnumFacing.values()) {
            renderCuboidFace(vb, face, sprites, flips, textureStart, textureSize, size, textureOffset, shadeTypes, formula, faceFormula,
                    cube.worldObj, renderOut, renderIn);
        }

        tess.draw();
    }

    private static void renderCuboidFace(VertexBuffer vb, EnumFacing face, TextureAtlasSprite[] sprites, int[] flips, Vec3d textureStart,
            Vec3d textureSize, Vec3d size, Vec3d textureOffset, EnumShadeArgument shadeTypes, IBlockLocation locationFormula,
            IFacingLocation faceFormula, IBlockAccess access, boolean out, boolean in) {
        int ordinal = face.ordinal();
        if (sprites[ordinal] == null) {
            return;
        }

        Vec3d textureEnd = textureStart.add(textureSize);
        float[] uv = getUVArray(sprites[ordinal], flips[ordinal], face, textureStart, textureEnd);
        List<RenderInfo> renderInfoList = getRenderInfos(uv, face, size, textureSize, textureOffset);

        Axis u = face.getAxis() == Axis.X ? Axis.Z : Axis.X;
        Axis v = face.getAxis() == Axis.Y ? Axis.Z : Axis.Y;
        double other = face.getAxisDirection() == AxisDirection.POSITIVE ? VecUtil.getValue(size, face.getAxis()) : 0;

        boolean flip = ModelUtil.shouldInvertForRender(face);

        // Flip it to be negative as the light renderer doesn't handle light proeprly
        face = face.getAxisDirection() == AxisDirection.NEGATIVE ? face : face.getOpposite();
        EnumFacing opposite = face.getOpposite();

        for (RenderInfo ri : renderInfoList) {
            if (flip ? out : in) {
                renderPoint(vb, face, u, v, other, ri, true, false, locationFormula, faceFormula, access, shadeTypes);
                renderPoint(vb, face, u, v, other, ri, true, true, locationFormula, faceFormula, access, shadeTypes);
                renderPoint(vb, face, u, v, other, ri, false, true, locationFormula, faceFormula, access, shadeTypes);
                renderPoint(vb, face, u, v, other, ri, false, false, locationFormula, faceFormula, access, shadeTypes);
            }
            if (flip ? in : out) {
                renderPoint(vb, opposite, u, v, other, ri, false, false, locationFormula, faceFormula, access, shadeTypes);
                renderPoint(vb, opposite, u, v, other, ri, false, true, locationFormula, faceFormula, access, shadeTypes);
                renderPoint(vb, opposite, u, v, other, ri, true, true, locationFormula, faceFormula, access, shadeTypes);
                renderPoint(vb, opposite, u, v, other, ri, true, false, locationFormula, faceFormula, access, shadeTypes);
            }
        }
    }

    private static void renderPoint(VertexBuffer wr, EnumFacing face, Axis u, Axis v, double other, RenderInfo ri, boolean minU, boolean minV,
            IBlockLocation locationFormula, IFacingLocation faceFormula, IBlockAccess access, EnumShadeArgument shadeTypes) {
        int U_ARRAY = minU ? U_MIN : U_MAX;
        int V_ARRAY = minV ? V_MIN : V_MAX;

        Vec3d vertex = VecUtil.replaceValue(Utils.VEC_ZERO, u, ri.xyz[U_ARRAY]);
        vertex = VecUtil.replaceValue(vertex, v, ri.xyz[V_ARRAY]);
        vertex = VecUtil.replaceValue(vertex, face.getAxis(), other);

        wr.pos(vertex.xCoord, vertex.yCoord, vertex.zCoord);
        wr.tex(ri.uv[U_ARRAY], ri.uv[V_ARRAY]);

        if (shadeTypes.isEnabled(EnumShadeType.FACE)) {
            RenderUtils.setWorldRendererRGB(wr, aoMap.get(faceFormula.transformToWorld(face)));
        }

        if (shadeTypes.isEnabled(EnumShadeType.AMBIENT_OCCLUSION)) {
            applyLocalAO(wr, faceFormula.transformToWorld(face), locationFormula, access, shadeTypes, vertex);
        } else if (shadeTypes.isEnabled(EnumShadeType.LIGHT)) {
            Vec3d transVertex = locationFormula.transformToWorld(vertex);
            BlockPos pos = Utils.convertFloor(transVertex);
            Block block = access.getBlockState(pos).getBlock();
            int combindedLight = block.getPackedLightmapCoords(access.getBlockState(pos), access, pos);
            wr.lightmap(combindedLight >> 16 & 65535, combindedLight & 65535);
        }

        wr.endVertex();
    }

    private static void applyLocalAO(VertexBuffer wr, EnumFacing face, IBlockLocation locationFormula, IBlockAccess access,
            EnumShadeArgument shadeTypes, Vec3d vertex) {
        // This doesn't work. At all.
        boolean allAround = false;

        int numPositions = allAround ? 7 : 5;
        int[] skyLight = new int[numPositions];
        int[] blockLight = new int[numPositions];
        float[] colorMultiplier = new float[numPositions];
        double[] distances = new double[numPositions];
        double totalDist = 0;
        Vec3d transVertex = locationFormula.transformToWorld(vertex);
        BlockPos pos = Utils.convertFloor(transVertex);
        IBlockState state = access.getBlockState(pos);
        Block block = state.getBlock();
        int combindedLight = block.getPackedLightmapCoords(state, access, pos);

        skyLight[0] = combindedLight / 0x10000;
        blockLight[0] = combindedLight % 0x10000;
        colorMultiplier[0] = block.getAmbientOcclusionLightValue(state);
        distances[0] = transVertex.distanceTo(Utils.convertMiddle(pos));

        int index = 0;
        EnumFacing[] testArray = allAround ? EnumFacing.values() : Utils.getNeighbours(face);
        for (EnumFacing otherFace : testArray) {
            Vec3d nearestOther = vertex.add(Utils.convert(otherFace));
            pos = Utils.convertFloor(locationFormula.transformToWorld(nearestOther));
            state = access.getBlockState(pos);
            block = state.getBlock();
            combindedLight = block.getPackedLightmapCoords(state, access, pos);

            index++;

            skyLight[index] = combindedLight / 0x10000;
            blockLight[index] = combindedLight % 0x10000;
            colorMultiplier[index] = block.getAmbientOcclusionLightValue(null);
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

        Vec3d color;
        if (shadeTypes.isEnabled(EnumShadeType.FACE)) {
            color = aoMap.get(face);
        } else {
            color = Utils.VEC_ONE;
        }
        color = Utils.multiply(color, avgColorMultiplier);
        RenderUtils.setWorldRendererRGB(wr, color);
    }

    public static void bakeCube(List<MutableQuad> quads, EntityResizableCuboid cuboid, boolean outsideFace, boolean insideFace) {
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

        Vec3d textureStart = new Vec3d(cuboid.textureStartX / 16D, cuboid.textureStartY / 16D, cuboid.textureStartZ / 16D);
        Vec3d textureSize = new Vec3d(cuboid.textureSizeX / 16D, cuboid.textureSizeY / 16D, cuboid.textureSizeZ / 16D);
        Vec3d textureOffset = new Vec3d(cuboid.textureOffsetX / 16D, cuboid.textureOffsetY / 16D, cuboid.textureOffsetZ / 16D);
        Vec3d size = new Vec3d(cuboid.xSize, cuboid.ySize, cuboid.zSize);

        for (EnumFacing face : EnumFacing.values()) {
            bakeCuboidFace(quads, cuboid, face, sprites, flips, textureStart, textureSize, size, textureOffset, outsideFace, insideFace);
        }
    }

    private static void bakeCuboidFace(List<MutableQuad> quads, EntityResizableCuboid cuboid, EnumFacing face, TextureAtlasSprite[] sprites,
            int[] flips, Vec3d textureStart, Vec3d textureSize, Vec3d size, Vec3d textureOffset, boolean out, boolean in) {
        int ordinal = face.ordinal();
        if (sprites[ordinal] == null) {
            return;
        }

        Vec3d textureEnd = textureStart.add(textureSize);
        float[] uv = getUVArray(sprites[ordinal], flips[ordinal], face, textureStart, textureEnd);
        List<RenderInfo> renderInfoList = getRenderInfos(uv, face, size, textureSize, textureOffset);

        Axis u = face.getAxis() == Axis.X ? Axis.Z : Axis.X;
        Axis v = face.getAxis() == Axis.Y ? Axis.Z : Axis.Y;
        double other = face.getAxisDirection() == AxisDirection.POSITIVE ? VecUtil.getValue(size, face.getAxis()) : 0;

        /* Swap the face if this is positive: the renderer returns indexes that ALWAYS are for the negative face, so
         * light it properly this way */
        // face = face.getAxisDirection() == AxisDirection.NEGATIVE ? face : face.getOpposite();

        EnumFacing opposite = face.getOpposite();

        boolean flip = ModelUtil.shouldInvertForRender(face);

        for (RenderInfo ri : renderInfoList) {
            ri = ri.offset(cuboid, face.getAxis());
            double otherMoved = other + VecUtil.getValue(cuboid.getPositionVector(), face.getAxis());

            if (flip ? out : in) {
                MutableQuad mutable = new MutableQuad(-1, face);
                bakePoint(mutable.vertex_0, face, u, v, otherMoved, ri, true, false);
                bakePoint(mutable.vertex_1, face, u, v, otherMoved, ri, true, true);
                bakePoint(mutable.vertex_2, face, u, v, otherMoved, ri, false, true);
                bakePoint(mutable.vertex_3, face, u, v, otherMoved, ri, false, false);
                quads.add(mutable);
            }
            if (flip ? in : out) {
                MutableQuad mutable = new MutableQuad(-1, face);
                bakePoint(mutable.vertex_0, opposite, u, v, otherMoved, ri, false, false);
                bakePoint(mutable.vertex_1, opposite, u, v, otherMoved, ri, false, true);
                bakePoint(mutable.vertex_2, opposite, u, v, otherMoved, ri, true, true);
                bakePoint(mutable.vertex_3, opposite, u, v, otherMoved, ri, true, false);
                quads.add(mutable);
            }
        }
    }

    private static void bakePoint(MutableVertex mutable, EnumFacing face, Axis u, Axis v, double other, RenderInfo ri, boolean minU, boolean minV) {
        int U_ARRAY = minU ? U_MIN : U_MAX;
        int V_ARRAY = minV ? V_MIN : V_MAX;

        Vec3d vertex = VecUtil.replaceValue(Utils.VEC_ZERO, u, ri.xyz[U_ARRAY]);
        vertex = VecUtil.replaceValue(vertex, v, ri.xyz[V_ARRAY]);
        vertex = VecUtil.replaceValue(vertex, face.getAxis(), other);

        mutable.positionv(Utils.convertFloat(vertex));
        mutable.colouri(0xFF_FF_FF_FF);
        mutable.texf(ri.uv[U_ARRAY], ri.uv[V_ARRAY]);
    }

    private static float[] getUVArray(TextureAtlasSprite sprite, int flips, EnumFacing face, Vec3d start, Vec3d end) {
        Axis u = face.getAxis() == Axis.X ? Axis.Z : Axis.X;
        Axis v = face.getAxis() == Axis.Y ? Axis.Z : Axis.Y;

        float minU = sprite.getInterpolatedU(VecUtil.getValue(start, u) * 16);
        float maxU = sprite.getInterpolatedU(VecUtil.getValue(end, u) * 16);
        float minV = sprite.getInterpolatedV(VecUtil.getValue(start, v) * 16);
        float maxV = sprite.getInterpolatedV(VecUtil.getValue(end, v) * 16);

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

    private static List<RenderInfo> getRenderInfos(float[] uv, EnumFacing face, Vec3d size, Vec3d texSize, Vec3d texOffset) {
        Axis u = face.getAxis() == Axis.X ? Axis.Z : Axis.X;
        Axis v = face.getAxis() == Axis.Y ? Axis.Z : Axis.Y;

        double sizeU = VecUtil.getValue(size, u);
        double sizeV = VecUtil.getValue(size, v);
        double textureSizeU = VecUtil.getValue(texSize, u);
        double textureSizeV = VecUtil.getValue(texSize, v);
        double textureOffsetU = VecUtil.getValue(texOffset, u);
        double textureOffsetV = VecUtil.getValue(texOffset, v);

        return getRenderInfos(uv, sizeU, sizeV, textureSizeU, textureSizeV, textureOffsetU, textureOffsetV);
    }

    /** A way to automatically generate the different positions given the same arguments.
     * 
     * @param rotation TODO */
    private static List<RenderInfo> getRenderInfos(float[] uv, double sizeU, double sizeV, double textureSizeU, double textureSizeV,
            double textureOffsetU, double textureOffsetV) {

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

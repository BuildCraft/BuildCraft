package buildcraft.core.lib.client.model;

import java.util.List;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.model.TRSRTransformation;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MatrixUtil;

public class BuildCraftBakedModel extends PerspAwareModelBase {
    public static final int U_MIN = 0;
    public static final int U_MAX = 1;
    public static final int V_MIN = 2;
    public static final int V_MAX = 3;

    // Baked Quad array indices
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public static final int SHADE = 3;
    public static final int U = 4;
    public static final int V = 5;
    /** Represents either the normal (for items) or lightmap (for blocks) */
    public static final int UNUSED = 6;

    // Size of each array
    public static final int ARRAY_SIZE = 7;

    @SuppressWarnings("deprecation")
    public BuildCraftBakedModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format, ImmutableMap<TransformType, TRSRTransformation> transforms) {
        super(format, quads, particle, transforms);
    }

    public BuildCraftBakedModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        this(quads, particle, format, getBlockTransforms());
    }

    @SuppressWarnings("deprecation")
    /** Get the default transformations for inside inventories and third person */
    protected static ImmutableMap<TransformType, TRSRTransformation> getBlockTransforms() {
        ImmutableMap.Builder<TransformType, TRSRTransformation> builder = ImmutableMap.builder();

        // Copied from ForgeBlockStateV1
        builder.put(TransformType.THIRD_PERSON, TRSRTransformation.blockCenterToCorner(new TRSRTransformation(new Vector3f(0, 1.5f / 16, -2.75f / 16), TRSRTransformation.quatFromYXZDegrees(new Vector3f(10, -45, 170)), new Vector3f(0.375f, 0.375f,
                0.375f), null)));

        // Gui
        {
            Matrix4f rotationMatrix = new Matrix4f();
            rotationMatrix.setIdentity();
            rotationMatrix = MatrixUtil.rotateTowardsFace(EnumFacing.SOUTH, EnumFacing.EAST);

            Matrix4f result = new Matrix4f();
            result.setIdentity();
            // Multiply by the last matrix transformation FIRST
            result.mul(rotationMatrix);

            TRSRTransformation trsr = new TRSRTransformation(result);

            builder.put(TransformType.GUI, trsr);
        }

        return builder.build();
    }

    @SuppressWarnings("deprecation")
    /** Get the default transformations for inside inventories and third person */
    protected static ImmutableMap<TransformType, TRSRTransformation> getItemTransforms() {
        ImmutableMap.Builder<TransformType, TRSRTransformation> builder = ImmutableMap.builder();

        float scale = 0.375f;
        Vector3f translation = new Vector3f(0, 1.5F * scale, -2.75F * scale);
        TRSRTransformation trsr = new TRSRTransformation(translation, new Quat4f(10, -45, 170, 1), new Vector3f(0.375F, 0.375F, 0.375F), null);
        builder.put(TransformType.THIRD_PERSON, trsr);

        translation = new Vector3f(1, 1, 0);
        trsr = new TRSRTransformation(translation, new Quat4f(0, 0, 0, 1), new Vector3f(1, 1, 1), new Quat4f(0, -90, 90, 1));
        builder.put(TransformType.GUI, trsr);

        return builder.build();
    }

    @SuppressWarnings("deprecation")
    /** Get the default transformations for models designed to be displayed on the SOUTH side of a pipe. */
    protected static ImmutableMap<TransformType, TRSRTransformation> getPluggableTransforms() {
        ImmutableMap.Builder<TransformType, TRSRTransformation> builder = ImmutableMap.builder();
        // Third person
        {
            // Magic Quat4f from ForgeBlockModelV1
            Quat4f rotation = TRSRTransformation.quatFromYXZDegrees(new Vector3f(10, -45, 170));
            TRSRTransformation trsr = new TRSRTransformation(new Vector3f(0, 1.5f / 16, -2.75f / 16), rotation, new Vector3f(0.375f, 0.375f, 0.375f), null);
            trsr = TRSRTransformation.blockCenterToCorner(trsr);

            Matrix4f trsrMatrix = trsr.getMatrix();

            Matrix4f result = new Matrix4f();
            result.setIdentity();

            // Matrix4f translationMatrix = new Matrix4f();
            // translationMatrix.setIdentity();
            // translationMatrix.setTranslation(new Vector3f(0, 0, -0.2f));

            Matrix4f rotationMatrix = new Matrix4f();
            rotationMatrix.setIdentity();
            rotationMatrix = MatrixUtil.rotateTowardsFace(EnumFacing.SOUTH, EnumFacing.NORTH);

            // Multiply by the last matrix transformation FIRST
            result.mul(trsrMatrix);
            result.mul(rotationMatrix);
            // result.mul(translationMatrix);

            trsr = new TRSRTransformation(result);

            builder.put(TransformType.THIRD_PERSON, trsr);
        }
        // First person
        {
            // Matrix4f translationMatrix = new Matrix4f();
            // translationMatrix.setIdentity();
            // translationMatrix.setTranslation(new Vector3f(0, 0, -0.2f));

            Matrix4f rotationMatrix = new Matrix4f();
            rotationMatrix.setIdentity();
            rotationMatrix = MatrixUtil.rotateTowardsFace(EnumFacing.SOUTH, EnumFacing.EAST);

            Matrix4f result = new Matrix4f();
            result.setIdentity();
            // Multiply by the last matrix transformation FIRST
            result.mul(rotationMatrix);
            // result.mul(translationMatrix);

            TRSRTransformation trsr = new TRSRTransformation(result);

            builder.put(TransformType.FIRST_PERSON, trsr);
        }
        return builder.build();
    }

    public static String toStringPretty(BakedQuad unpacked) {
        StringBuilder builder = new StringBuilder();
        int[] data = unpacked.getVertexData();
        int stride = data.length / 4;
        for (int v = 0; v < 4; v++) {
            builder.append("\nV#" + v + "=[");
            for (int e = 0; e < stride; e++) {
                if (e != 0) builder.append(", ");
                int d = data[v * stride + e];
                builder.append(Integer.toHexString(d));
            }
            builder.append("]");
        }
        String s = builder.toString();
        return s;
    }

    public static IBakedModel createModelItemLayer(TextureAtlasSprite sprite) {
        return createModelItemLayer(Lists.newArrayList(sprite));
    }

    public static IBakedModel createModelItemLayer(final List<TextureAtlasSprite> sprites) {
        List<BakedQuad> quads = BCModelHelper.bakeList(createQuadsItemLayer(sprites));
        return new BuildCraftBakedModel(ImmutableList.copyOf(quads), sprites.get(0), MutableQuad.ITEM_LMAP);
    }

    public static List<MutableQuad> createQuadsItemLayer(TextureAtlasSprite sprite) {
        return createQuadsItemLayer(Lists.newArrayList(sprite));
    }

    public static List<MutableQuad> createQuadsItemLayer(final List<TextureAtlasSprite> sprites) {
        ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
        for (int i = 0; i < sprites.size(); i++) {
            builder.add(new ResourceLocation("buildcraftbakedmodel:spriteindex" + i));
        }

        final ImmutableList<ResourceLocation> locations = builder.build();

        ItemLayerModel model = new ItemLayerModel(locations);
        IBakedModel baked = model.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, new Function<ResourceLocation, TextureAtlasSprite>() {
            @Override
            public TextureAtlasSprite apply(ResourceLocation input) {
                return sprites.get(locations.indexOf(input));
            }
        });

        Matrix4f itemToEdge = new Matrix4f();
        itemToEdge.setIdentity();
        itemToEdge.setRotation(new AxisAngle4f(0, 1, 0, (float) (Math.PI / 2)));

        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        translation.setTranslation(new Vector3f(-15 / 32f, 0, 1));
        translation.mul(itemToEdge);

        List<MutableQuad> mutableQuads = BCModelHelper.toMutableQuadList(baked, false);
        for (MutableQuad mutable : mutableQuads) {
            mutable.transform(translation);
            mutable.setTint(-1);
        }

        return mutableQuads;
    }

    public static Function<ResourceLocation, TextureAtlasSprite> singleTextureFunction(final TextureAtlasSprite sprite) {
        if (sprite == null) throw new NullPointerException("sprite");
        return new Function<ResourceLocation, TextureAtlasSprite>() {
            @Override
            public TextureAtlasSprite apply(ResourceLocation input) {
                return sprite;
            }
        };
    }
}

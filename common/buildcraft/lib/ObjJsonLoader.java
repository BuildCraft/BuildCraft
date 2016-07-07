package buildcraft.lib;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum ObjJsonLoader implements ICustomModelLoader {
    INSTANCE;

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getResourcePath().endsWith(".obj.json");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        Gson gson = new GsonBuilder().registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3fDeserializer()).registerTypeAdapter(ItemCameraTransforms.class, new ItemCameraTransformsDeserializer()).create();
        JsonObject modelDesc = gson.fromJson(new JsonReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(modelLocation).getInputStream(), Charsets.UTF_8)), JsonObject.class);
        ResourceLocation objLocation = new ResourceLocation(modelDesc.get("model").getAsString());
        objLocation = new ResourceLocation(objLocation.getResourceDomain(), "models/" + objLocation.getResourcePath());
        ItemCameraTransforms transforms = ItemCameraTransforms.DEFAULT;
        if(modelDesc.has("display")) {
            transforms = gson.fromJson(modelDesc.get("display").getAsJsonObject(), ItemCameraTransforms.class);
        }
        OBJModel objModel = (OBJModel) OBJLoader.INSTANCE.loadModel(objLocation);
        return new ObjJsonModel(objModel, transforms);
    }
}

class ObjJsonModel implements IModel {
    private OBJModel obj;
    private ItemCameraTransforms transforms;

    public ObjJsonModel(OBJModel obj, ItemCameraTransforms transforms) {
        this.obj = obj;
        this.transforms = transforms;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return new ArrayList<>();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return obj.getTextures();
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new ObjJsonBakedModel((OBJModel.OBJBakedModel) obj.bake(state, format, bakedTextureGetter), this, state, transforms);
    }

    @Override
    public IModelState getDefaultState() {
        return ModelRotation.X0_Y0;
    }
}

class ObjJsonBakedModel implements IBakedModel, IPerspectiveAwareModel {
    private OBJModel.OBJBakedModel obj;
    private ObjJsonModel model;
    private IModelState modelState;
    private ItemCameraTransforms transforms;

    public ObjJsonBakedModel(OBJModel.OBJBakedModel obj, ObjJsonModel model, IModelState modelState, ItemCameraTransforms transforms) {
        this.obj = obj;
        this.model = model;
        this.modelState = modelState;
        this.transforms = transforms;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> quads = new ArrayList<>();
        quads = obj.getQuads(state, side, rand);
        return quads;
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
        return obj.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new ObjJsonItemOverrideList();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return new ImmutablePair<>(this, TRSRTransformation.blockCornerToCenter(transforms.getTransform(cameraTransformType).apply(Optional.absent()).get()).getMatrix());
    }
}

class ObjJsonItemOverrideList extends ItemOverrideList {
    public ObjJsonItemOverrideList() {
        super(new ArrayList<>());
    }

    @Override
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
        return super.handleItemState(originalModel, stack, world, entity);
    }
}

// Copy from vanilla because in it this class is private
class ItemTransformVec3fDeserializer implements JsonDeserializer<ItemTransformVec3f>
{
    private static final org.lwjgl.util.vector.Vector3f ROTATION_DEFAULT = new org.lwjgl.util.vector.Vector3f(0.0F, 0.0F, 0.0F);
    private static final org.lwjgl.util.vector.Vector3f TRANSLATION_DEFAULT = new org.lwjgl.util.vector.Vector3f(0.0F, 0.0F, 0.0F);
    private static final org.lwjgl.util.vector.Vector3f SCALE_DEFAULT = new org.lwjgl.util.vector.Vector3f(1.0F, 1.0F, 1.0F);

    public ItemTransformVec3f deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException
    {
        JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
        org.lwjgl.util.vector.Vector3f vector3f = this.parseVector3f(jsonobject, "rotation", ROTATION_DEFAULT);
        org.lwjgl.util.vector.Vector3f vector3f1 = this.parseVector3f(jsonobject, "translation", TRANSLATION_DEFAULT);
        vector3f1.scale(0.0625F);
        vector3f1.x = MathHelper.clamp_float(vector3f1.x, -5.0F, 5.0F);
        vector3f1.y = MathHelper.clamp_float(vector3f1.y, -5.0F, 5.0F);
        vector3f1.z = MathHelper.clamp_float(vector3f1.z, -5.0F, 5.0F);
        org.lwjgl.util.vector.Vector3f vector3f2 = this.parseVector3f(jsonobject, "scale", SCALE_DEFAULT);
        vector3f2.x = MathHelper.clamp_float(vector3f2.x, -4.0F, 4.0F);
        vector3f2.y = MathHelper.clamp_float(vector3f2.y, -4.0F, 4.0F);
        vector3f2.z = MathHelper.clamp_float(vector3f2.z, -4.0F, 4.0F);
        return new ItemTransformVec3f(vector3f, vector3f1, vector3f2);
    }

    private org.lwjgl.util.vector.Vector3f parseVector3f(JsonObject jsonObject, String key, org.lwjgl.util.vector.Vector3f defaultValue)
    {
        if (!jsonObject.has(key))
        {
            return defaultValue;
        }
        else
        {
            JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, key);

            if (jsonarray.size() != 3)
            {
                throw new JsonParseException("Expected 3 " + key + " values, found: " + jsonarray.size());
            }
            else
            {
                float[] afloat = new float[3];

                for (int i = 0; i < afloat.length; ++i)
                {
                    afloat[i] = JsonUtils.getFloat(jsonarray.get(i), key + "[" + i + "]");
                }

                return new org.lwjgl.util.vector.Vector3f(afloat[0], afloat[1], afloat[2]);
            }
        }
    }
}

class ItemCameraTransformsDeserializer implements JsonDeserializer<ItemCameraTransforms>
{
    public ItemCameraTransforms deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException
    {
        JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
        ItemTransformVec3f itemtransformvec3f = this.getTransform(p_deserialize_3_, jsonobject, "thirdperson_righthand");
        ItemTransformVec3f itemtransformvec3f1 = this.getTransform(p_deserialize_3_, jsonobject, "thirdperson_lefthand");

        if (itemtransformvec3f1 == ItemTransformVec3f.DEFAULT)
        {
            itemtransformvec3f1 = itemtransformvec3f;
        }

        ItemTransformVec3f itemtransformvec3f2 = this.getTransform(p_deserialize_3_, jsonobject, "firstperson_righthand");
        ItemTransformVec3f itemtransformvec3f3 = this.getTransform(p_deserialize_3_, jsonobject, "firstperson_lefthand");

        if (itemtransformvec3f3 == ItemTransformVec3f.DEFAULT)
        {
            itemtransformvec3f3 = itemtransformvec3f2;
        }

        ItemTransformVec3f itemtransformvec3f4 = this.getTransform(p_deserialize_3_, jsonobject, "head");
        ItemTransformVec3f itemtransformvec3f5 = this.getTransform(p_deserialize_3_, jsonobject, "gui");
        ItemTransformVec3f itemtransformvec3f6 = this.getTransform(p_deserialize_3_, jsonobject, "ground");
        ItemTransformVec3f itemtransformvec3f7 = this.getTransform(p_deserialize_3_, jsonobject, "fixed");
        return new ItemCameraTransforms(itemtransformvec3f1, itemtransformvec3f, itemtransformvec3f3, itemtransformvec3f2, itemtransformvec3f4, itemtransformvec3f5, itemtransformvec3f6, itemtransformvec3f7);
    }

    private ItemTransformVec3f getTransform(JsonDeserializationContext p_181683_1_, JsonObject p_181683_2_, String p_181683_3_)
    {
        return p_181683_2_.has(p_181683_3_) ? (ItemTransformVec3f)p_181683_1_.deserialize(p_181683_2_.get(p_181683_3_), ItemTransformVec3f.class) : ItemTransformVec3f.DEFAULT;
    }
}

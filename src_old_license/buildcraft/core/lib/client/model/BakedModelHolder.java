package buildcraft.core.lib.client.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public abstract class BakedModelHolder extends BuildCraftBakedModel {

    public BakedModelHolder(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format,
            ImmutableMap<TransformType, TRSRTransformation> transforms) {
        super(quads, particle, format, transforms);
    }

    public BakedModelHolder(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format) {
        this(quads, particle, format, getBlockTransforms());
    }

    private final Map<ResourceLocation, IModel> models = Maps.newHashMap();
    private final Map<ResourceLocation, IBakedModel> bakedModels = Maps.newHashMap();

    public BakedModelHolder() {
        this(null, null, null);
    }

    @SubscribeEvent
    public void modelBakeEvent(ModelBakeEvent event) {
        models.clear();
    }

    protected IModel getModelJSON(ResourceLocation loc) {
        if (!models.containsKey(loc)) {
            IModel model = null;
            try {
                model = ModelLoaderRegistry.getModel(loc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (model == null) model = ModelLoaderRegistry.getMissingModel();
            models.put(loc, model);
        }
        return models.get(loc);
    }

    protected IModel getModelOBJ(ResourceLocation loc) {
        if (!models.containsKey(loc)) {
            IModel model = null;
            try {
                model = OBJLoader.instance.loadModel(loc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (model == null) model = ModelLoaderRegistry.getMissingModel();
            models.put(loc, model);
        }
        return models.get(loc);
    }

    protected IBakedModel getModelItemLayer(ResourceLocation identifier, TextureAtlasSprite sprite) {
        return getModelItemLayer(identifier, ImmutableList.of(sprite));
    }

    protected IBakedModel getModelItemLayer(ResourceLocation identifier, List<TextureAtlasSprite> sprites) {
        if (!bakedModels.containsKey(identifier)) {
            bakedModels.put(identifier, createModelItemLayer(sprites));
        }
        return bakedModels.get(identifier);
    }
}

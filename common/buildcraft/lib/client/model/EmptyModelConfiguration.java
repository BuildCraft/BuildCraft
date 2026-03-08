package buildcraft.lib.client.model;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.model.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

import javax.annotation.Nullable;
import java.util.Map;

public class EmptyModelConfiguration implements IModelConfiguration {
    public static final ResourceLocation LOCATION = new ResourceLocation("");

    public static final EmptyModelConfiguration INSTANCE = new EmptyModelConfiguration(LOCATION, Maps.newHashMap());

    private final Map<String, ResourceLocation> textures;

    public EmptyModelConfiguration(ResourceLocation modelName, Map<String, ResourceLocation> textures) {
        this.textures = textures;
    }

    @Nullable
    @Override
    public IUnbakedModel getOwnerModel() {
        return null;
    }

    @Override
    public String getModelName() {
        return LOCATION.toString();
    }

    @Override
    public boolean isTexturePresent(String name) {
        return textures.containsKey(name);
    }

    private static final ResourceLocation RL = new ResourceLocation("buildcraft", "render_material");

    @Override
    public RenderMaterial resolveTexture(String name) {
        return new RenderMaterial(RL, textures.getOrDefault(name, RL));
    }

    @Override
    public boolean isShadedInGui() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean useSmoothLighting() {
        return true;
    }

    @Override
    public ItemCameraTransforms getCameraTransforms() {
        return ItemCameraTransforms.NO_TRANSFORMS;
    }

    @Override
    public IModelTransform getCombinedTransform() {
        return ModelRotation.X0_Y0;
    }
}

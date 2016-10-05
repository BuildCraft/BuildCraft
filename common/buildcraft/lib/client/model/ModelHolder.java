package buildcraft.lib.client.model;

import java.util.List;

import net.minecraft.util.ResourceLocation;

public abstract class ModelHolder {
    public final ResourceLocation modelLocation;
    protected String failReason = "";

    public ModelHolder(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        CustomModelLoader.HOLDERS.add(this);
    }

    public ModelHolder(String modelLocation) {
        this(new ResourceLocation(modelLocation));
    }

    protected abstract void onModelBake();

    protected abstract void onTextureStitchPre(List<ResourceLocation> toRegisterSprites);

    public abstract boolean hasBakedQuads();
}

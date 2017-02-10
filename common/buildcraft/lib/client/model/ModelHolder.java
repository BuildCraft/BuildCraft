package buildcraft.lib.client.model;

import java.util.List;

import net.minecraft.util.ResourceLocation;

/** Defines an object that will hold a model, and is automatically refreshed from the filesystem when the client reloads
 * all of its resources. */
public abstract class ModelHolder {
    public final ResourceLocation modelLocation;
    protected String failReason = "";

    public ModelHolder(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        ModelHolderRegistry.HOLDERS.add(this);
    }

    public ModelHolder(String modelLocation) {
        this(new ResourceLocation(modelLocation));
    }

    protected abstract void onModelBake();

    protected abstract void onTextureStitchPre(List<ResourceLocation> toRegisterSprites);

    public abstract boolean hasBakedQuads();
}

package buildcraft.core.lib.render;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public abstract class BakedModelHolder extends BuildCraftBakedModel {
    private final Map<ResourceLocation, IModel> models = Maps.newHashMap();

    public BakedModelHolder() {
        super(null, null, null);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void modelBakeEvent(ModelBakeEvent event) {
        for (ResourceLocation loc : models.keySet()) {
            models.put(loc, null);
        }
    }

    protected IModel getModel(ResourceLocation loc) {
        IModel model = models.get(loc);
        if (model == null) {
            try {
                model = OBJLoader.instance.loadModel(loc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (model == null) {
            model = ModelLoaderRegistry.getMissingModel();
        }
        return model;
    }
}

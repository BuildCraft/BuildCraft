package buildcraft.lib.client.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonParseException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.model.ModelUtil.TexturedFace;
import buildcraft.lib.client.model.json.JsonModelRule;
import buildcraft.lib.client.model.json.JsonTexture;
import buildcraft.lib.client.model.json.JsonVariableModel;
import buildcraft.lib.client.model.json.JsonVariableModelPart;
import buildcraft.lib.client.reload.ReloadManager;
import buildcraft.lib.client.reload.ReloadSource;
import buildcraft.lib.client.reload.SourceType;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.ITickableNode;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.transport.BCTransportModels;

/** Holds a model that can be changed by variables. Models are defined in this way by firstly creating a
 * {@link FunctionContext}, and then defining all of the variables with FunctionContext.getOrAddX(). It is recommended
 * that you define all models inside of static initialiser block. For a complete usage example look in
 * {@link BCTransportModels}. <br>
 * The json model definition of a variable model matches the vanilla format, except that any of the static numbers may
 * be replaced with an expression, that may use any of the variables you have defined. */
public class ModelHolderVariable extends ModelHolder {
    private final FunctionContext context;
    private JsonVariableModel rawModel;
    private boolean unseen = true;

    public ModelHolderVariable(String modelLocation, FunctionContext context) {
        super(modelLocation);
        this.context = context;
    }

    @Override
    public boolean hasBakedQuads() {
        return rawModel != null;
    }

    @Override
    protected void onTextureStitchPre(Set<ResourceLocation> toRegisterSprites) {
        rawModel = null;
        failReason = null;

        try {
            rawModel = JsonVariableModel.deserialize(modelLocation, context);
        } catch (JsonParseException jse) {
            rawModel = null;
            failReason = "The model had errors: " + jse.getMessage();
            BCLog.logger.warn("[lib.model.holder] Failed to load the model " + modelLocation + " because ", jse);
        } catch (IOException io) {
            rawModel = null;
            failReason = "The model did not exist in any resource pack: " + io.getMessage();
            BCLog.logger.warn("[lib.model.holder] Failed to load the model " + modelLocation + " because ", io);
        }
        if (rawModel != null) {
            if (ModelHolderRegistry.DEBUG) {
                BCLog.logger.info("[lib.model.holder] The model " + modelLocation + " requires these sprites:");
            }
            ReloadSource srcModel = new ReloadSource(modelLocation, SourceType.MODEL);
            for (Entry<String, JsonTexture> entry : rawModel.textures.entrySet()) {
                JsonTexture lookup = entry.getValue();
                String location = lookup.location;
                if (location.startsWith("#")) {
                    // its somewhere else in the map so we don't need to register it twice
                    continue;
                }
                ResourceLocation textureLoc = new ResourceLocation(location);
                toRegisterSprites.add(textureLoc);
                // Allow transisitve deps
                ReloadSource srcSprite = new ReloadSource(SpriteUtil.transformLocation(textureLoc), SourceType.SPRITE);
                ReloadManager.INSTANCE.addDependency(srcSprite, srcModel);
                if (ModelHolderRegistry.DEBUG) {
                    BCLog.logger.info("[lib.model.holder]  - " + location);
                }
            }
        }
    }

    @Override
    protected void onModelBake() {
        // NO-OP: we bake every time get{Cutout/Translucent}Quads is called as this is a variable model
    }

    private MutableQuad[] bakePart(JsonVariableModelPart[] a) {
        List<MutableQuad> list = new ArrayList<>();
        for (JsonVariableModelPart part : a) {
            part.addQuads(list, this::lookupTexture);
        }
        for (JsonModelRule rule : rawModel.rules) {
            if (rule.when.evaluate()) {
                rule.apply(list);
            }
        }
        return list.toArray(new MutableQuad[list.size()]);
    }

    private TexturedFace lookupTexture(String lookup) {
        int attempts = 0;
        JsonTexture texture = new JsonTexture(lookup);
        while (texture.location.startsWith("#") && attempts < 10) {
            JsonTexture tex = rawModel.textures.get(texture.location);
            if (tex == null) break;
            else texture = texture.inParent(tex);
            attempts++;
        }
        lookup = texture.location;
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(lookup);
        TexturedFace face = new TexturedFace();
        face.sprite = sprite;
        face.faceData = texture.faceData;
        return face;
    }

    private void printNoModelWarning() {
        if (unseen) {
            unseen = false;
            String warnText = "[lib.model.holder] Tried to use the model " + modelLocation + " before it was baked!";
            if (ModelHolderRegistry.DEBUG) {
                BCLog.logger.warn(warnText, new Throwable());
            } else {
                BCLog.logger.warn(warnText);
            }
        }
    }

    public ITickableNode[] createTickableNodes() {
        if (rawModel == null) {
            printNoModelWarning();
            return new ITickableNode[0];
        }
        return rawModel.createTickableNodes();
    }

    public MutableQuad[] getCutoutQuads() {
        if (rawModel == null) {
            printNoModelWarning();
            return MutableQuad.EMPTY_ARRAY;
        }
        return bakePart(rawModel.cutoutElements);
    }

    public MutableQuad[] getTranslucentQuads() {
        if (rawModel == null) {
            printNoModelWarning();
            return MutableQuad.EMPTY_ARRAY;
        }
        return bakePart(rawModel.translucentElements);
    }
}

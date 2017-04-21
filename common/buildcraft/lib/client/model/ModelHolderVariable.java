package buildcraft.lib.client.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.model.json.JsonModelRule;
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
    private final ImmutableMap<String, String> textureLookup;
    private final boolean allowTextureFallthrough;
    private JsonVariableModel rawModel;
    private boolean unseen = true;

    public ModelHolderVariable(String location, FunctionContext context) {
        this(location, context, ImmutableMap.of(), false);
    }

    public ModelHolderVariable(String location, FunctionContext context, String[][] textures, boolean allowTextureFallthrough) {
        this(location, context, genTextureMap(textures), allowTextureFallthrough);
    }

    public ModelHolderVariable(String modelLocation, FunctionContext context, ImmutableMap<String, String> textureLookup, boolean allowTextureFallthrough) {
        super(modelLocation);
        this.context = context;
        this.textureLookup = textureLookup;
        this.allowTextureFallthrough = allowTextureFallthrough;
    }

    private static ImmutableMap<String, String> genTextureMap(String[][] textures) {
        if (textures == null || textures.length == 0) {
            return ImmutableMap.of();
        }
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (String[] ar : textures) {
            if (ar.length != 2) {
                throw new IllegalArgumentException("Must have 2 elements (key,value) but got " + Arrays.toString(ar));
            }
            if (!ar[0].startsWith("~")) {
                throw new IllegalArgumentException("Key must start with '~' otherwise it will never be used!");
            }
            builder.put(ar[0], ar[1]);
        }
        return builder.build();
    }

    @Override
    public boolean hasBakedQuads() {
        return rawModel != null;
    }

    @Override
    protected void onTextureStitchPre(List<ResourceLocation> toRegisterSprites) {
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
            for (Entry<String, String> entry : rawModel.textures.entrySet()) {
                String lookup = entry.getValue();
                if (lookup.startsWith("#")) {
                    // its somewhere else in the map so we don't need to register it twice
                    continue;
                }
                if (lookup.startsWith("~") && textureLookup.containsKey(lookup)) {
                    lookup = textureLookup.get(lookup);
                }
                if (lookup == null || lookup.startsWith("#") || lookup.startsWith("~")) {
                    if (!allowTextureFallthrough) {
                        failReason = "The sprite lookup '" + lookup + "' did not exist in ay of the maps";
                        rawModel = null;
                        break;
                    }
                } else {
                    ResourceLocation textureLoc = new ResourceLocation(lookup);
                    toRegisterSprites.add(textureLoc);
                    // Allow transisitve deps
                    ReloadSource srcSprite = new ReloadSource(SpriteUtil.transformLocation(textureLoc), SourceType.SPRITE);
                    ReloadManager.INSTANCE.addDependency(srcSprite, srcModel);
                }
                if (ModelHolderRegistry.DEBUG) {
                    BCLog.logger.info("[lib.model.holder]  - " + lookup);
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

    private TextureAtlasSprite lookupTexture(String lookup) {
        int attempts = 0;
        while (lookup.startsWith("#") && rawModel.textures.containsKey(lookup) && attempts < 10) {
            lookup = rawModel.textures.get(lookup);
            attempts++;
        }
        if (lookup.startsWith("~") && textureLookup.containsKey(lookup)) {
            lookup = textureLookup.get(lookup);
        }
        TextureAtlasSprite sprite;
        if (lookup.startsWith("#") || lookup.startsWith("~")) {
            if (allowTextureFallthrough) {
                // Let the caller manually replace the sprite (as we don't know what to replace it with)
                // But only if the model user is aware of this (so its not an error)
                sprite = null;
            } else {
                sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
            }
        } else {
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(lookup);
        }
        return sprite;
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
            return new MutableQuad[0];
        }
        return bakePart(rawModel.cutoutElements);
    }

    public MutableQuad[] getTranslucentQuads() {
        if (rawModel == null) {
            printNoModelWarning();
            return new MutableQuad[0];
        }
        return bakePart(rawModel.translucentElements);
    }
}

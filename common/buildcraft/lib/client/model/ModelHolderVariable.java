package buildcraft.lib.client.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.model.json.JsonVariableModel;
import buildcraft.lib.client.model.json.JsonVariableModelPart;
import buildcraft.lib.expression.FunctionContext;

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
        Map<String, String> map = new HashMap<>();
        for (String[] ar : textures) {
            if (ar.length != 2) {
                throw new IllegalArgumentException("Must have 2 elements (key,value) but got " + Arrays.toString(ar));
            }
            if (!ar[0].startsWith("~")) {
                throw new IllegalArgumentException("Key must start with '~' otherwise it will never be used!");
            }
            map.put(ar[0], ar[1]);
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
        try (IResource res = Minecraft.getMinecraft().getResourceManager().getResource(modelLocation)) {
            InputStream is = res.getInputStream();
            try (InputStreamReader isr = new InputStreamReader(is)) {
                rawModel = JsonVariableModel.deserialize(isr, context);
            } catch (JsonSyntaxException jse) {
                rawModel = null;
                failReason = "The model had errors: " + jse.getMessage();
                BCLog.logger.warn("[lib.model.holder] Failed to load the model " + modelLocation + " because " + jse.getMessage());
            }
        } catch (IOException io) {
            BCLog.logger.warn("[lib.model.holder] Failed to load the model " + modelLocation + " because " + io.getMessage());
            rawModel = null;
            failReason = "The model did not exist in any resource pack: " + io.getMessage();
        }
        if (rawModel != null) {
            if (CustomModelLoader.DEBUG) {
                BCLog.logger.info("[lib.model.holder] The model " + modelLocation + " requires these sprites:");
            }
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
                    toRegisterSprites.add(new ResourceLocation(lookup));
                }
                if (CustomModelLoader.DEBUG) {
                    BCLog.logger.info("[lib.model.holder]  - " + lookup);
                }
            }
        }
    }

    @Override
    protected void onModelBake() {}

    private MutableQuad[] bakePart(JsonVariableModelPart[] a) {
        List<MutableQuad> list = new ArrayList<>();
        for (JsonVariableModelPart part : a) {
            part.addQuads(list, this::lookupTexture);
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
                sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();;
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
            if (CustomModelLoader.DEBUG) {
                BCLog.logger.warn(warnText, new Throwable());
            } else {
                BCLog.logger.warn(warnText);
            }
        }
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

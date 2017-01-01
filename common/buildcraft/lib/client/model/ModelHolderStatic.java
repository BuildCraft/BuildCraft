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

import buildcraft.lib.client.model.json.JsonModel;
import buildcraft.lib.client.model.json.JsonModelPart;
import buildcraft.lib.client.model.json.JsonQuad;

/** Holds a model that will never change except if the json file it is defined from is changed. */
public class ModelHolderStatic extends ModelHolder {
    private final ImmutableMap<String, String> textureLookup;
    private final boolean allowTextureFallthrough;
    private MutableQuad[][] quads;
    private JsonModel rawModel;
    private boolean unseen = true;

    public ModelHolderStatic(String location) {
        this(location, ImmutableMap.of(), false);
    }

    public ModelHolderStatic(String location, String[][] textures, boolean allowTextureFallthrough) {
        this(location, genTextureMap(textures), allowTextureFallthrough);
    }

    public ModelHolderStatic(String modelLocation, ImmutableMap<String, String> textureLookup, boolean allowTextureFallthrough) {
        super(modelLocation);
        this.textureLookup = textureLookup;
        this.allowTextureFallthrough = allowTextureFallthrough;
    }

    @Override
    public boolean hasBakedQuads() {
        return quads != null;
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
    protected void onTextureStitchPre(List<ResourceLocation> toRegisterSprites) {
        rawModel = null;
        quads = null;
        failReason = null;
        try (IResource res = Minecraft.getMinecraft().getResourceManager().getResource(modelLocation)) {
            InputStream is = res.getInputStream();
            try (InputStreamReader isr = new InputStreamReader(is)) {
                rawModel = JsonModel.deserialize(isr);
            } catch (JsonSyntaxException jse) {
                rawModel = null;
                failReason = "The model had errors: " + jse.getMessage();
                BCLog.logger.warn("Failed to load the model " + modelLocation + " because " + jse.getMessage());
            }
        } catch (IOException io) {
            BCLog.logger.warn("Failed to load the model " + modelLocation + " because " + io.getMessage());
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
    protected void onModelBake() {
        if (rawModel == null) {
            quads = null;
        } else {
            MutableQuad[] cut = bakePart(rawModel.cutoutElements);
            MutableQuad[] trans = bakePart(rawModel.translucentElements);
            quads = new MutableQuad[][] { cut, trans };
            rawModel = null;
        }
    }

    private MutableQuad[] bakePart(JsonModelPart[] a) {
        TextureAtlasSprite missingSprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        List<MutableQuad> list = new ArrayList<>();
        for (JsonModelPart part : a) {
            for (JsonQuad quad : part.quads) {
                String lookup = quad.texture;
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
                        sprite = missingSprite;
                    }
                } else {
                    sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(lookup);
                }
                list.add(quad.toQuad(sprite));
            }
        }
        return list.toArray(new MutableQuad[list.size()]);
    }

    private MutableQuad[][] getQuadsChecking() {
        if (quads == null) {
            if (unseen) {
                unseen = false;
                String warnText = "[lib.model.holder] Tried to use the model " + modelLocation + " before it was baked!";
                if (CustomModelLoader.DEBUG) {
                    BCLog.logger.warn(warnText, new Throwable());
                } else {
                    BCLog.logger.warn(warnText);
                }
            }
            return new MutableQuad[][] { MutableQuad.EMPTY_ARRAY, MutableQuad.EMPTY_ARRAY };
        }
        return quads;
    }

    public MutableQuad[] getCutoutQuads() {
        return getQuadsChecking()[0];
    }

    public MutableQuad[] getTranslucentQuads() {
        return getQuadsChecking()[1];
    }
}

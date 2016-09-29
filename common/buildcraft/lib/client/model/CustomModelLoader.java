package buildcraft.lib.client.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.client.model.json.JsonModel;
import buildcraft.lib.client.model.json.JsonModelPart;
import buildcraft.lib.client.model.json.JsonQuad;

public class CustomModelLoader {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.model.holder");

    private static final List<ModelHolder> HOLDERS = new ArrayList<>();

    public static void onTextureStitchPre(TextureMap map) {
        List<ResourceLocation> toStitch = new ArrayList<>();
        for (ModelHolder holder : HOLDERS) {
            holder.onTextureStitchPre(toStitch);
        }

        for (ResourceLocation res : toStitch) {
            map.registerSprite(res);
        }
    }

    public static void onModelBake() {
        for (ModelHolder holder : HOLDERS) {
            holder.onModelBake();
        }
        if (DEBUG && Loader.instance().isInState(LoaderState.AVAILABLE)) {
            BCLog.logger.info("[lib.model.holder] List of registered Models:");
            List<ModelHolder> holders = new ArrayList<>();
            holders.addAll(HOLDERS);
            holders.sort((a, b) -> a.modelLocation.toString().compareTo(b.modelLocation.toString()));

            for (ModelHolder holder : holders) {
                MutableQuad[][] baked = holder.quads;
                String status = "  ";
                if (holder.failReason != null) {
                    status += "(" + holder.failReason + ")";
                } else if (baked == null) {
                    status += "(Model was registered too late)";
                }

                BCLog.logger.info("  - " + holder.modelLocation + status);
            }
            BCLog.logger.info("[lib.model.holder] Total of " + HOLDERS.size() + " models");
        }
    }

    public static class ModelHolder {
        // TODO: Add a complex version of this that can use expressions
        public final ResourceLocation modelLocation;
        private final Map<String, String> textureLookup;
        private final boolean allowTextureFallthrough;
        private MutableQuad[][] quads;
        private JsonModel rawModel;
        private boolean unseen = true;
        private String failReason;

        public ModelHolder(String location) {
            this(new ResourceLocation(location), ImmutableMap.of(), false);
        }

        public ModelHolder(String location, Map<String, String> textureLookup, boolean allowTextureFallthrough) {
            this(new ResourceLocation(location), textureLookup, allowTextureFallthrough);
        }

        public ModelHolder(ResourceLocation modelLocation, Map<String, String> textureLookup, boolean allowTextureFallthrough) {
            HOLDERS.add(this);
            this.modelLocation = modelLocation;
            this.textureLookup = textureLookup;
            this.allowTextureFallthrough = allowTextureFallthrough;
        }

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
                if (DEBUG) {
                    BCLog.logger.info("[lib.model.holder] The model " + modelLocation + " requires these sprites:");
                }
                for (Entry<String, String> entry : rawModel.textures.entrySet()) {
                    String lookup = entry.getValue();
                    if (lookup.startsWith("~")) {
                        lookup = textureLookup.get(lookup);
                        if (lookup == null) {
                            lookup = entry.getValue();
                        }
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
                    if (DEBUG) {
                        BCLog.logger.info("[lib.model.holder]  - " + lookup);
                    }
                }
            }
        }

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
                    if (lookup.startsWith("#")) {
                        lookup = rawModel.textures.get(lookup);
                    }
                    if (lookup.startsWith("~")) {
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
                    if (DEBUG) {
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
}

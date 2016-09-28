package buildcraft.lib.client.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.core.lib.client.model.BCModelHelper;

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

    public static void onTextureStitchPost() {
        if (DEBUG && Loader.instance().isInState(LoaderState.AVAILABLE)) {
            BCLog.logger.info("[lib.model.holder] List of registered Models:");
            List<ModelHolder> holders = new ArrayList<>();
            holders.addAll(HOLDERS);
            holders.sort((a, b) -> a.modelLocation.toString().compareTo(b.modelLocation.toString()));

            for (ModelHolder holder : holders) {
                MutableQuad[] baked = holder.quads;
                String status = "  ";
                if (baked == null) {
                    status += "(Model was registered too late)";
                } else if (holder.hasBaked) {
                    status += "(Model did not exist in a resource pack)";
                }

                BCLog.logger.info("  - " + holder.modelLocation + status);
            }
            BCLog.logger.info("[lib.model.holder] Total of " + HOLDERS.size() + " models");
        }
    }

    public static void onModelBake() {
        for (ModelHolder holder : HOLDERS) {
            holder.onModelBake();
        }
    }

    public static class ModelHolder {
        public final ResourceLocation modelLocation;
        private final Map<String, String> textureLookup;
        private MutableQuad[] quads;
        private ModelBlock rawModel;
        private boolean unseen = true, hasBaked = false;

        public ModelHolder(String location) {
            this(new ResourceLocation(location));
        }

        public ModelHolder(ResourceLocation modelLocation) {
            this(modelLocation, ImmutableMap.of());
        }

        public ModelHolder(ResourceLocation modelLocation, Map<String, String> textureLookup) {
            HOLDERS.add(this);
            this.modelLocation = modelLocation;
            this.textureLookup = textureLookup;
        }

        protected void onTextureStitchPre(List<ResourceLocation> toRegisterSprites) {
            rawModel = null;
            quads = null;
            try (IResource res = Minecraft.getMinecraft().getResourceManager().getResource(modelLocation)) {
                InputStream is = res.getInputStream();
                try (InputStreamReader isr = new InputStreamReader(is)) {
                    rawModel = ModelBlock.deserialize(isr);
                }
            } catch (IOException e) {
                throw new Error(e);
            }
            if (rawModel != null) {
                for (Entry<String, String> entry : rawModel.textures.entrySet()) {
                    String lookup = entry.getValue();
                    if (lookup.startsWith("~")) {
                        lookup = textureLookup.get(lookup);
                    }
                    toRegisterSprites.add(new ResourceLocation(lookup));
                }
            }
        }

        protected void onModelBake() {
            hasBaked = true;
            if (rawModel == null) {
                BCLog.logger.warn("Raw Model for " + modelLocation + " was null!");
            } else {
                List<MutableQuad> mutableQuads = new ArrayList<>();

                for (BlockPart part : rawModel.getElements()) {
                    Vector3f radius = new Vector3f(//
                            part.positionTo.x - part.positionFrom.x,//
                            part.positionTo.y - part.positionFrom.y,//
                            part.positionTo.z - part.positionFrom.z//
                    );
                    radius.scale(0.5f);
                    Vector3f center = new Vector3f(part.positionFrom.x, part.positionFrom.y, part.positionFrom.z);
                    center.add(radius);
                    center.scale(1 / 16f);
                    radius.scale(1 / 16f);

                    for (Entry<EnumFacing, BlockPartFace> e : part.mapFaces.entrySet()) {
                        EnumFacing side = e.getKey();
                        BlockPartFace face = e.getValue();

                        String lookup = face.texture;
                        if (lookup.startsWith("#")) {
                            lookup = rawModel.textures.get(lookup);
                        }
                        if (lookup.startsWith("~")) {
                            lookup = textureLookup.get(lookup);
                        }

                        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(lookup);

                        float[] uvs = new float[4];
                        uvs[0] = sprite.getInterpolatedU(face.blockFaceUV.uvs[0]);
                        uvs[1] = sprite.getInterpolatedU(face.blockFaceUV.uvs[2]);
                        uvs[2] = sprite.getInterpolatedV(face.blockFaceUV.uvs[1]);
                        uvs[3] = sprite.getInterpolatedV(face.blockFaceUV.uvs[3]);
                        // int rot = face.blockFaceUV.rotation;
                        MutableQuad quad = BCModelHelper.createFace(side, center, radius, uvs);
                        // quad.rotateTextureUp(rot);
                        mutableQuads.add(quad);
                    }
                }

                this.quads = mutableQuads.toArray(new MutableQuad[mutableQuads.size()]);
                rawModel = null;
            }
        }

        private MutableQuad[] getQuadsChecking() {
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
                return MutableQuad.EMPTY_ARRAY;
            }
            return quads;
        }

        public MutableQuad[] getQuads() {
            return getQuadsChecking();
        }
    }
}

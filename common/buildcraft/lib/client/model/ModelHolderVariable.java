/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.model.json.JsonTexture;
import buildcraft.lib.client.model.json.JsonVariableModel;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.ITickableNode;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.transport.BCTransportModels;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Holds a model that can be changed by variables. Models are defined in this way by firstly creating a
 * {@link FunctionContext}, and then defining all of the variables with FunctionContext.getOrAddX(). It is recommended
 * that you define all models inside of static initializer block. For a complete usage example look in
 * {@link BCTransportModels}. <br>
 * The json model definition of a variable model matches the vanilla format, except that any of the static numbers may
 * be replaced with an expression, that may use any of the variables you have defined. */
public class ModelHolderVariable extends ModelHolder {
    public final Map<String, TextureAtlasSprite> customSprites = new HashMap<>();
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
            rawModel.onTextureStitchPre(modelLocation, toRegisterSprites);
        }
    }

    @Override
    protected void onModelBake() {
        // NO-OP: we bake every time get{Cutout/Translucent}Quads is called as this is a variable model
    }

    private ModelUtil.TexturedFace lookupTexture(String lookup) {
        int attempts = 0;
        JsonTexture texture = new JsonTexture(lookup);
        TextureAtlasSprite sprite;
        while (texture.location.startsWith("#") && attempts < 10) {
            JsonTexture tex = rawModel.textures.get(texture.location);
            if (tex == null) break;
            else texture = texture.inParent(tex);
            attempts++;
        }
        lookup = texture.location;
        if (lookup.startsWith("~")) {
            sprite = customSprites.get(lookup.substring(1));
            if (sprite == null) {
//                sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
                sprite = SpriteUtil.missingSprite();
            }
        } else {
//            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(lookup);
            sprite = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(new ResourceLocation(lookup));
        }
        ModelUtil.TexturedFace face = new ModelUtil.TexturedFace();
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

    @Nullable
    public JsonVariableModel getModel() {
        if (rawModel == null) {
            printNoModelWarning();
        }
        return rawModel;
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
        return rawModel.bakePart(rawModel.cutoutElements, this::lookupTexture);
    }

    public MutableQuad[] getTranslucentQuads() {
        if (rawModel == null) {
            printNoModelWarning();
            return MutableQuad.EMPTY_ARRAY;
        }
        return rawModel.bakePart(rawModel.translucentElements, this::lookupTexture);
    }
}

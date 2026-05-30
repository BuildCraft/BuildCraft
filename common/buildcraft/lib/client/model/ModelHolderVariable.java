/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.client.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.gson.JsonParseException;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.model.ModelUtil.TexturedFace;
import buildcraft.lib.client.model.json.JsonTexture;
import buildcraft.lib.client.model.json.JsonVariableModel;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.ITickableNode;

/**
 * STUB(R.Chen): client model — Phase 5.
 *
 * The Forge original resolved sprites via {@code Minecraft.getMinecraft().getTextureMapBlocks()}
 * during ModelBakeEvent. In Fabric 1.20.1 sprite resolution goes through a Fabric atlas pipeline.
 * The public API surface (fields, getCutoutQuads/getTranslucentQuads) is preserved so callers
 * compile. Sprite lookup stubs return null; baking produces empty arrays until Phase 5.
 */
@Environment(EnvType.CLIENT)
public class ModelHolderVariable extends ModelHolder {
    // STUB(R.Chen): Sprite map used by Phase-5 render pass to inject custom sprites.
    public final Map<String, Sprite> customSprites = new HashMap<>();
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
    protected void onTextureStitchPre(Set<Identifier> toRegisterSprites) {
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
        // NO-OP: variable models are baked on demand
    }

    // STUB(R.Chen): Phase 5 — sprite resolution via Fabric atlas pipeline.
    private TexturedFace lookupTexture(String lookup) {
        TexturedFace face = new TexturedFace();
        face.sprite = null; // STUB
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

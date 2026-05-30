/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.util.Identifier;

import buildcraft.api.core.BCDebugging;

/**
 * STUB(R.Chen): client model — Phase 5.
 *
 * The Forge original fired callbacks on TextureStitchEvent.Pre (atlas sprite registration) and
 * ModelBakeEvent (model baking). In Fabric 1.20.1 these are replaced by ModelLoadingPlugin +
 * ClientSpriteRegistryCallback. The full wiring is deferred to the dedicated render migration pass.
 * Only the compile-time surface (static {@link #HOLDERS} list, {@link #onTextureStitchPre} and
 * {@link #onModelBake} entry points) is retained so dependent classes compile.
 */
@Environment(EnvType.CLIENT)
public class ModelHolderRegistry {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.model.holder");

    static final List<ModelHolder> HOLDERS = new ArrayList<>();

    // STUB(R.Chen): Phase 5 — replace with ModelLoadingPlugin / ClientSpriteRegistryCallback.
    public static void onTextureStitchPre(Set<Identifier> toStitch) {
        for (ModelHolder holder : HOLDERS) {
            holder.onTextureStitchPre(toStitch);
        }
    }

    // STUB(R.Chen): Phase 5 — replace with ModelLoadingPlugin#onInitializeModelLoader.
    public static void onModelBake() {
        for (ModelHolder holder : HOLDERS) {
            holder.onModelBake();
        }
    }
}

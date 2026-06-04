/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.client.model.json;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.texture.Sprite;

import buildcraft.lib.client.model.MutableQuad;

// STUB(R.Chen): model/json — Phase 5.
@Environment(EnvType.CLIENT)
public class JsonQuad {
    public String texture;

    JsonQuad() {}

    // STUB(R.Chen): Phase 5 — quad baking with Fabric Sprite UVs.
    public MutableQuad toQuad(Sprite sprite) {
        return new MutableQuad();
    }
}

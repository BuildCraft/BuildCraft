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

import buildcraft.lib.client.model.ModelUtil.UvFaceData;

/** STUB(R.Chen): Phase 5 — preserves the compile-time surface used by ModelHolderVariable. */
@Environment(EnvType.CLIENT)
public class JsonTexture {
    public final String location;
    public final UvFaceData faceData;

    public JsonTexture(String location, UvFaceData faceData) {
        this.location = location;
        this.faceData = faceData;
    }

    public JsonTexture(String location) {
        this(location, new UvFaceData(0, 0, 1, 1));
    }

    public JsonTexture inParent(JsonTexture parent) {
        UvFaceData combined = faceData.inParent(parent.faceData);
        return new JsonTexture(parent.location, combined);
    }
}

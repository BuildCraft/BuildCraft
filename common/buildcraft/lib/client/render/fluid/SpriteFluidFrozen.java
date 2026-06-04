/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): frozen-sprite mechanism removed — Sprite subclassing API changed in 1.20.1
package buildcraft.lib.client.render.fluid;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SpriteFluidFrozen {

    public final Identifier srcLocation;

    public SpriteFluidFrozen(Identifier srcLocation) {
        this.srcLocation = srcLocation;
    }

    public Sprite getSprite() {
        // STUB
        return null;
    }
}

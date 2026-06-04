/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): Sprite subclassing / swappable-sprite pattern removed in 1.20.1 — deferred
package buildcraft.lib.client.sprite;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public abstract class AtlasSpriteSwappable {

    protected final Identifier id;

    protected AtlasSpriteSwappable(String baseName) {
        this.id = new Identifier(baseName);
    }

    public abstract Sprite getSprite();
}

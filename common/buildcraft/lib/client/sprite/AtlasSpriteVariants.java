/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): AtlasSpriteVariants deferred — swappable sprite API removed in 1.20.1
package buildcraft.lib.client.sprite;

import java.util.Set;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

import buildcraft.lib.client.reload.IReloadable;
import buildcraft.lib.client.reload.ReloadSource;

@Environment(EnvType.CLIENT)
public class AtlasSpriteVariants extends AtlasSpriteSwappable implements IReloadable {

    public AtlasSpriteVariants(String baseName) {
        super(baseName);
    }

    @Override
    public Sprite getSprite() {
        return null;
    }

    @Override
    public boolean reload(Set<ReloadSource> changed) {
        return false;
    }
}

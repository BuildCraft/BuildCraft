/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): guide manager resource loading deferred
package buildcraft.lib.client.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

@Environment(EnvType.CLIENT)
public enum GuideManager implements SynchronousResourceReloader {
    INSTANCE;

    @Override
    public void reload(ResourceManager manager) {
        // STUB
    }
}

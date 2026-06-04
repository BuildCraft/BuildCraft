/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// STUB(R.Chen): render tick listener deferred — Forge RenderWorldLastEvent removed
package buildcraft.core.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RenderTickListener {

    public static final RenderTickListener INSTANCE = new RenderTickListener();

    public void onRenderTick() {
        // STUB
    }
}

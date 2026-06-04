/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): AdvDebuggerQuarry render deferred
package buildcraft.builders.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.client.render.DetachedRenderer;

@Environment(EnvType.CLIENT)
public class AdvDebuggerQuarry implements DetachedRenderer.IDetachedRenderer {

    public static final AdvDebuggerQuarry INSTANCE = new AdvDebuggerQuarry();

    @Override
    public void render(float partialTicks) {
        // STUB
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// STUB(R.Chen): the full Forge world-last-event detached renderer (RenderMatrixType registry,
// RenderWorldLastEvent hook, GL matrix setup) is deferred to the client-render migration pass. Only
// the IDetachedRenderer interface — referenced by IAdvDebugTarget / TileBC_Neptune — is retained so the
// debug-target contract compiles. Full implementation: full implementation deferred.
public class DetachedRenderer {

    @Environment(EnvType.CLIENT)
    public interface IDetachedRenderer {
        // STUB(R.Chen): original signature was render(EntityPlayer, float partialTicks); reduced to the
        // partial-ticks arg until the Fabric WorldRenderEvents-based render pipeline is ported.
        void render(float partialTicks);
    }
}

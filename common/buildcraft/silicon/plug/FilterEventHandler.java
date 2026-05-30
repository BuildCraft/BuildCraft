/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.plug;

import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;

// STUB(R.Chen): silicon module Phase 7. TilePipeHolder registers this class as an item-pipe event handler
// (FilterEventHandler.class) when the silicon module is loaded. The real lens-filtering logic depends on
// PluggableLens (unmigrated silicon.plug). The handler is kept as a no-op so the pipe event bus has a valid
// target; restore the body once PluggableLens lands.
public class FilterEventHandler {
    @PipeEventHandler
    public static void sideCheck(PipeEventItem.SideCheck event) {
        // STUB(R.Chen): full lens priority logic restored with PluggableLens in silicon Phase 7.
    }
}

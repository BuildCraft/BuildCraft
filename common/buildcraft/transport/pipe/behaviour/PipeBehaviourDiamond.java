/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

import buildcraft.lib.tile.item.ItemHandlerSimple;

// STUB(R.Chen): full PipeBehaviourDiamond migration deferred — pipeline dep. Minimal fields for containers.
public abstract class PipeBehaviourDiamond extends PipeBehaviour {

    public static final int FILTERS_PER_SIDE = 9;

    public final ItemHandlerSimple filters = new ItemHandlerSimple(FILTERS_PER_SIDE * 6, null);

    public PipeBehaviourDiamond(IPipe pipe) {
        super(pipe);
    }
}

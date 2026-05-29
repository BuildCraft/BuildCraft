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

// STUB(R.Chen): full PipeBehaviourWoodDiamond (extends PipeBehaviourWood) deferred — pipeline dep.
// Minimal fields + FilterMode for container compile.
public class PipeBehaviourWoodDiamond extends PipeBehaviour {

    public enum FilterMode {
        WHITE_LIST, BLACK_LIST, ROUND_ROBIN;

        public static FilterMode get(int index) {
            FilterMode[] values = values();
            return (index >= 0 && index < values.length) ? values[index] : WHITE_LIST;
        }
    }

    public final ItemHandlerSimple filters = new ItemHandlerSimple(9, null);
    public FilterMode filterMode = FilterMode.WHITE_LIST;

    public PipeBehaviourWoodDiamond(IPipe pipe) {
        super(pipe);
    }
}

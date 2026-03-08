/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Direction;

public class KeyPlugPulsar extends PluggableModelKey {
    public KeyPlugPulsar(Direction side) {
        super(RenderType.cutout(), side);
    }
}

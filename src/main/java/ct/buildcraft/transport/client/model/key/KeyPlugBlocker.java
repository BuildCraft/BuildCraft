/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.client.model.key;

import ct.buildcraft.api.transport.pluggable.PluggableModelKey;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;

public final class KeyPlugBlocker extends PluggableModelKey {
    public KeyPlugBlocker(Direction side) {
        super(RenderType.cutout(), side);
    }

    /** Factory for {@link #KeyPlugBlocker(Direction)} that avoids class verification errors. */
    public static PluggableModelKey create(Direction side) {
        return new KeyPlugBlocker(side);
    }
}

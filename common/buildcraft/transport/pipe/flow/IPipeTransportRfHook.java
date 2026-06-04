/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe.flow;

import net.minecraft.util.math.Direction;

public interface IPipeTransportRfHook {

    /** Override default behavior on receiving energy into the pipe.
     *
     * @return The amount of power used, or -1 for default behavior. */
    int receivePower(Direction from, int val);

    /** Override default requested power. */
    int requestPower(Direction from, int amount);
}

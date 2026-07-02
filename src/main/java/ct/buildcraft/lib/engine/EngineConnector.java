/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.engine;

import javax.annotation.Nonnull;

import ct.buildcraft.api.mj.IMjConnector;
import ct.buildcraft.api.mj.IMjReceiver;
import ct.buildcraft.api.mj.IMjRedstoneReceiver;

public class EngineConnector implements IMjConnector {
    public final boolean redstoneOnly;

    public EngineConnector(boolean redstoneOnly) {
        this.redstoneOnly = redstoneOnly;
    }

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        if (other instanceof IMjReceiver && ((IMjReceiver) other).canReceive()) {
            if (redstoneOnly) {
                return other instanceof IMjRedstoneReceiver;
            }
            return true;
        }
        return false;
    }
}

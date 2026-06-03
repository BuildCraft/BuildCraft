/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): ClientSnapshots deferred
package buildcraft.builders.snapshot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientSnapshots {

    public static final ClientSnapshots INSTANCE = new ClientSnapshots();

    public void onClientTick() {
        // STUB
    }
}

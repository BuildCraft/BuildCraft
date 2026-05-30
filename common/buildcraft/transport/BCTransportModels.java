/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// STUB(R.Chen): model registration (Forge ModelLoader/ModelBakeEvent/IBakedModel) → Fabric FRAPI Phase 5.
// ModelHolderStatic/Variable, IPluggableStaticBaker fields and registerModels() restored in Phase 5.
@Environment(EnvType.CLIENT)
public class BCTransportModels {
    public static void preInit() {} // STUB Phase 5
    public static void init() {}    // STUB Phase 5
}

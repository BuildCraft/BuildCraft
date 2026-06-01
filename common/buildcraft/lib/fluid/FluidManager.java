/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.fluid;

// STUB(R.Chen): FluidManager is deeply Forge-dependent (BlockFluidClassic, FluidRegistry, BCMaterialFluid).
// Full rewrite required to use Fabric fluid registration. Stubbed to compile-only for now.
// TODO(R.Chen): re-implement with Fabric fluid blocks (net.fabricmc.fabric.api.block.v1.FabricBlockSettings,
//               Fabric fluid API, etc.) after BCFluid / BCFluidBlock are fully migrated.

public final class FluidManager {

    private FluidManager() {}

    /** STUB: no-op until Fabric fluid block registration is implemented. */
    public static <F extends BCFluid> F register(F fluid) {
        // TODO(R.Chen): register fluid and fluid block via Fabric APIs
        return fluid;
    }
}

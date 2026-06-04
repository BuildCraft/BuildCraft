/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.factory.tile;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.tiles.IDebuggable;

// Forge→Fabric migration notes (R.Chen):
//   Tank/Fluid/FluidStack          → STUB (Transfer-API, Phase 4E)
//   CapUtil.CAP_FLUIDS             → STUB (Phase 4F)
//   TankManager.addAll             → STUB
public class TileAutoWorkbenchFluids extends TileAutoWorkbenchBase implements IDebuggable {

    // STUB(R.Chen): Tank deferred to Phase 4E — Transfer-API fluid migration.
    // private final Tank tank1 = new Tank("tank1", Fluid.BUCKET_VOLUME * 6, this);
    // private final Tank tank2 = new Tank("tank2", Fluid.BUCKET_VOLUME * 6, this);

    public TileAutoWorkbenchFluids(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 2, 2);
        // STUB(R.Chen): tankManager.addAll / caps fluid registration deferred (Phase 4E).
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("Tanks: STUB(not yet migrated)");
    }
}

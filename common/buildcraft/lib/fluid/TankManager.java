/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.fluid;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

// STUB(R.Chen): the full Forge tank manager (Tank/SingleUseTank list, FluidStack draining, bucket
// fill/empty onActivated, IFluidTankProperties capability exposure, the FluidUtilBC/FluidItemDrops
// helpers) is deferred to the Transfer-API fluid migration pass. Only the no-op surface referenced by
// TileBC_Neptune is retained so the tile base compiles. Real tank storage is restored when the
// Transfer-API fluid layer lands; see BCFluidStorage.
public class TankManager {

    public TankManager() {}

    public void addDrops(DefaultedList<ItemStack> toDrop) {
        // STUB(R.Chen): no tanks are tracked yet.
    }

    public boolean onActivated(PlayerEntity player, BlockPos pos, Hand hand) {
        // STUB(R.Chen): bucket fill/empty interaction deferred until the fluid layer is migrated.
        return false;
    }

    public NbtCompound serializeNBT() {
        return new NbtCompound();
    }

    public void deserializeNBT(NbtCompound nbt) {
        // STUB(R.Chen): no tanks are tracked yet.
    }
}

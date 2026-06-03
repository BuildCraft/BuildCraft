/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.factory.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

// Forge→Fabric migration notes (R.Chen):
//   ITickable.update()         → tick() + static ticker()
//   FluidStack/FluidTankProperties/IFluidHandlerAdv → STUB (Transfer-API, Phase 4E)
//   Tank/FluidSmoother         → STUB (Phase 4E)
//   IFluidFilter/IFluidHandlerAdv → STUB (api.core not fully in libLeaf)
//   onActivated(PlayerEntity)  → onActivated(PlayerEntity) in TileBC_Neptune (already migrated)
//   onPlacedBy(LivingEntity) → onPlacedBy(LivingEntity) in TileBC_Neptune
//   PlayerEntity               → PlayerEntity
//   Direction                 → Direction
//   NbtCompound             → NbtCompound
//   Side                       → NetSide
//   balanceTankFluids / getTanks → STUB (Tank not in libLeaf)
public class TileTank extends TileBC_Neptune implements IDebuggable {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("tank");
    public static final int NET_FLUID_DELTA = IDS.allocId("FLUID_DELTA");

    // STUB(R.Chen): Tank + FluidSmoother deferred to Phase 4E (Transfer-API fluid migration).
    public final buildcraft.lib.fluid.Tank tank;
    // public final FluidSmoother smoothedTank;

    private int lastComparatorLevel;

    public TileTank(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // STUB(R.Chen): Tank capacity / capability registration deferred.
        tank = new buildcraft.lib.fluid.Tank("tank", 16000, this);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public int getComparatorLevel() {
        // STUB(R.Chen): tank.getFluidAmount() / getCapacity() deferred.
        return 0;
    }

    /** BlockEntityTicker wired in BlockTank.getTicker(). Replaces ITickable.update(). */
    @SuppressWarnings("unchecked")
    public static <T extends TileTank> BlockEntityTicker<T> ticker() {
        return (world, pos, state, be) -> be.tick();
    }

    public void tick() {
        // STUB(R.Chen): smoothedTank.tick(world) + comparator level check deferred (Phase 4E).
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        // STUB(R.Chen): smoothedTank payloads deferred — FluidSmoother not in libLeaf.
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        // STUB(R.Chen): smoothedTank payloads deferred — FluidSmoother not in libLeaf.
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("fluid = STUB(Tank not yet migrated)");
    }
}

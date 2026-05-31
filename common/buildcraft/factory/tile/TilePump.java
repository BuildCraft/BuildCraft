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

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.mj.IMjReceiver;

import buildcraft.lib.mj.MjRedstoneBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;

// Forge→Fabric migration notes (R.Chen):
//   ITickable.update()     → tick() via TileMiner.ticker()
//   Fluid/FluidStack/FluidRegistry → FluidVariant + Storage<FluidVariant> (Transfer-API)
//   BlockUtil.getFluidWithFlowing / drainBlock → STUB (Phase 4E)
//   FluidUtilBC.pushFluidAround / areFluidsEqual → STUB (Phase 4E)
//   Tank (lib.fluid)       → STUB (Transfer-API fluid storage, Phase 4E)
//   AdvancementUtil        → deferred (owner UUID available, but ServerPlayer needed)
//   ITileOilSpring         → STUB (BCCoreBlocks / energy module not yet in libLeaf)
//   buildQueue/buildQueue0 → STUB (all fluid BFS logic deferred to Phase 4E)
//   NBT: only oilSpringPos persisted; tank NBT deferred
public class TilePump extends TileMiner {

    @Nullable
    private BlockPos oilSpringPos;

    // STUB(R.Chen): Tank + fluid queue + path map deferred — Transfer-API fluid migration (Phase 4E).
    // private final Tank tank = new Tank("tank", 16 * Fluid.BUCKET_VOLUME, this);
    // private final Deque<BlockPos> queue = new ArrayDeque<>();
    // private final Map<BlockPos, FluidPath> paths = new HashMap<>();

    public TilePump(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // STUB(R.Chen): tank.setCanFill(false); tankManager.add(tank); caps.addCapabilityInstance(CAP_FLUIDS, ...)
        //               deferred to Phase 4E (Transfer-API fluid lookup registration).
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjRedstoneBatteryReceiver(battery);
    }

    @Override
    public void tick() {
        // STUB(R.Chen): queue build check + fluid push deferred to Phase 4E.
        super.tick();
    }

    @Override
    protected void mine() {
        // STUB(R.Chen): entire fluid drain + BFS logic deferred to Phase 4E.
        // Full algorithm: buildQueue() → BFS flood-fill → drain source blocks →
        //                 fill internal tank → push via FluidUtilBC.pushFluidAround.
    }

    // NBT

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("oilSpringPos")) {
            // STUB(R.Chen): NBTUtilBC.readBlockPos not yet in libLeaf — inline decode.
            NbtCompound posTag = nbt.getCompound("oilSpringPos");
            oilSpringPos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (oilSpringPos != null) {
            NbtCompound posTag = new NbtCompound();
            posTag.putInt("x", oilSpringPos.getX());
            posTag.putInt("y", oilSpringPos.getY());
            posTag.putInt("z", oilSpringPos.getZ());
            nbt.put("oilSpringPos", posTag);
        }
    }

    // Networking — tank payload deferred to Phase 4E

    @Override
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        // STUB(R.Chen): tank.writeToBuffer(buffer) deferred — Tank not in libLeaf.
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        // STUB(R.Chen): tank.readFromBuffer(buffer) deferred — Tank not in libLeaf.
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("fluid = STUB(Tank not yet migrated)");
        left.add("oilSpring = " + oilSpringPos);
    }

    @Override
    protected long getBatteryCapacity() {
        return 50 * buildcraft.api.mj.MjAPI.MJ;
    }
}

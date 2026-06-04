/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.factory.tile;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

// Forge→Fabric migration notes (R.Chen):
//   ITickable.update()         → tick() + static ticker()
//   Tank/FluidSmoother/Fluid   → STUB (Transfer-API, Phase 4E)
//   IRefineryRecipeManager     → STUB (api.recipes not in libLeaf)
//   Direction                 → Direction
//   NbtCompound             → NbtCompound
//   Side                       → NetSide
//   ICustomRotationHandler     → STUB (api.blocks not in libLeaf)
//   EnumParticleTypes / MinecraftClient.getInstance() → STUB (@Environment client only)
//   ExchangeSection inner classes: all fluid/recipe logic STUBbed (Phase 4E)
//   findAdjacentExchangers: Direction.rotateYClockwise() → Direction helper (Phase 4E)
public class TileHeatExchange extends TileBC_Neptune implements IDebuggable {

    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("HeatExchanger");
    public static final int NET_ID_CHANGE_SECTION = IDS.allocId("CHANGE_SECTION");
    public static final int NET_ID_TANK_IN = IDS.allocId("TANK_IN");
    public static final int NET_ID_TANK_OUT = IDS.allocId("TANK_OUT");
    public static final int NET_ID_STATE = IDS.allocId("STATE");

    @Nullable
    protected ExchangeSection section;
    private boolean checkNeighbours;

    public TileHeatExchange(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // STUB(R.Chen): ICustomRotationHandler capability deferred — api.blocks not in libLeaf.
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    /** BlockEntityTicker wired in BlockHeatExchange.getTicker(). Replaces ITickable.update(). */
    @SuppressWarnings("unchecked")
    public static <T extends TileHeatExchange> BlockEntityTicker<T> ticker() {
        return (world, pos, state, be) -> be.tick();
    }

    public void tick() {
        if (checkNeighbours) {
            checkNeighbours = false;
            // STUB(R.Chen): findAdjacentExchangers() + section linking deferred (Phase 4E).
            // Uses Direction.rotateYClockwise() equivalent not available until Direction migration complete.
        }
        if (section != null) {
            section.tick();
        }
    }

    public boolean isStart() {
        return section instanceof ExchangeSectionStart;
    }

    public boolean isEnd() {
        return section instanceof ExchangeSectionEnd;
    }

    protected void setSection(ExchangeSection section) {
        this.section = section;
        sendNetworkUpdate(NET_ID_CHANGE_SECTION);
    }

    // NBT

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        NbtCompound nbtSection = nbt.getCompound("section");
        if (!nbtSection.isEmpty()) {
            if (nbtSection.getBoolean("start")) {
                section = new ExchangeSectionStart(this, nbtSection);
            } else {
                section = new ExchangeSectionEnd(this, nbtSection);
            }
        }
        checkNeighbours = true;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (section != null) {
            nbt.put("section", section.writeToNbt());
        }
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        // STUB(R.Chen): section/tank payloads deferred — ExchangeSection fluid logic (Phase 4E).
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        // STUB(R.Chen): section/tank payloads deferred.
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("section = " + (section == null ? "null" : section.getClass().getSimpleName()));
        left.add("tanks = STUB(not yet migrated)");
    }

    // ---------------------------------------------------------------------------
    // Inner section classes — fluid/recipe logic stubbed; structure preserved for
    // NBT round-trip and client-render type checks.
    // ---------------------------------------------------------------------------

    public static abstract class ExchangeSection {
        protected final TileHeatExchange tile;

        public ExchangeSection(TileHeatExchange tile) {
            this.tile = tile;
        }

        /** NBT read constructor. */
        public ExchangeSection(TileHeatExchange tile, NbtCompound nbt) {
            this.tile = tile;
        }

        public NbtCompound writeToNbt() {
            return new NbtCompound();
        }

        /** Called each tick from TileHeatExchange.tick(). */
        public void tick() {
            // STUB(R.Chen): fluid exchange / recipe processing deferred to Phase 4E.
        }
    }

    public static class ExchangeSectionStart extends ExchangeSection {
        public int middleCount = 0;
        @Nullable
        public ExchangeSectionEnd endSection;

        public ExchangeSectionStart(TileHeatExchange tile) {
            super(tile);
        }

        public ExchangeSectionStart(TileHeatExchange tile, NbtCompound nbt) {
            super(tile, nbt);
            middleCount = nbt.getInt("middleCount");
            // STUB(R.Chen): tank NBT deferred — Tank not in libLeaf.
        }

        @Override
        public NbtCompound writeToNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("start", true);
            nbt.putInt("middleCount", middleCount);
            // STUB(R.Chen): tank serialisation deferred.
            return nbt;
        }
    }

    public static class ExchangeSectionEnd extends ExchangeSection {
        public ExchangeSectionEnd(TileHeatExchange tile) {
            super(tile);
        }

        public ExchangeSectionEnd(TileHeatExchange tile, NbtCompound nbt) {
            super(tile, nbt);
            // STUB(R.Chen): tank NBT deferred.
        }

        @Override
        public NbtCompound writeToNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("start", false);
            // STUB(R.Chen): tank serialisation deferred.
            return nbt;
        }
    }
}

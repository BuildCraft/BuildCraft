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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

// Forge→Fabric migration notes (R.Chen):
//   ITickable.update()       → tick() + static ticker()
//   Fluid/FluidStack         → FluidVariant + Storage<FluidVariant> (Phase 4E)
//   Tank (lib.fluid)         → STUB (Transfer-API, Phase 4E)
//   buildQueue/canFill/canSearch/update fluid logic → STUB (Phase 4E)
//   FakePlayer/BuildCraftAPI.fakePlayerProvider → STUB (Phase 4E)
//   Direction               → Direction
//   NbtCompound           → NbtCompound
//   MessageContext           → Object
//   Side                     → NetSide
//   NBTPrimitive/NbtByteArray open-sides decode → simplified to BitSet pattern
public class TileFloodGate extends TileBC_Neptune implements IDebuggable {
    private static final int[] REBUILD_DELAYS = { 16, 32, 64, 128, 256 };

    // STUB(R.Chen): BlockFloodGate.CONNECTED_MAP not in libLeaf — initialise directly (UP excluded per original).
    public final Set<Direction> openSides = EnumSet.of(
        Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    );

    // STUB(R.Chen): Tank + fluid queue/path map deferred to Phase 4E.
    public final Deque<BlockPos> queue = new ArrayDeque<>();
    private int delayIndex = 0;
    private int tick = 0;

    public TileFloodGate(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // STUB(R.Chen): caps.addCapabilityInstance(CAP_FLUIDS, tank, ...) deferred to Phase 4E.
        // STUB(R.Chen): tankManager.add(tank) deferred — Tank not in libLeaf.
    }

    private int getCurrentDelay() {
        return REBUILD_DELAYS[delayIndex];
    }

    /** BlockEntityTicker wired in BlockFloodGate.getTicker(). Replaces ITickable.update(). */
    @SuppressWarnings("unchecked")
    public static <T extends TileFloodGate> BlockEntityTicker<T> ticker() {
        return (world, pos, state, be) -> be.tick();
    }

    public void tick() {
        if (world.isClient) {
            return;
        }
        // STUB(R.Chen): entire fluid fill logic (buildQueue + place fluid + FakePlayer) deferred
        //               to Phase 4E (Transfer-API fluid + FakePlayer migration).
    }

    // NBT

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        byte b = 0;
        for (Direction face : Direction.values()) {
            if (openSides.contains(face)) {
                b |= 1 << face.ordinal();
            }
        }
        nbt.putByte("openSides", b);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("openSides")) {
            byte sides = nbt.getByte("openSides");
            for (Direction face : Direction.values()) {
                if (((sides >> face.ordinal()) & 1) == 1) {
                    openSides.add(face);
                } else {
                    openSides.remove(face);
                }
            }
        }
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        if (side == NetSide.SERVER && id == NET_RENDER_DATA) {
            // Encode openSides as a 6-bit bitmask (one bit per Direction ordinal).
            byte b = 0;
            for (Direction face : Direction.values()) {
                if (openSides.contains(face)) {
                    b |= (byte) (1 << face.ordinal());
                }
            }
            buffer.writeByte(b);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetSide.CLIENT && id == NET_RENDER_DATA) {
            byte b = buffer.readByte();
            EnumSet<Direction> newSides = EnumSet.noneOf(Direction.class);
            for (Direction face : Direction.values()) {
                if (((b >> face.ordinal()) & 1) == 1) {
                    newSides.add(face);
                }
            }
            if (!newSides.equals(openSides)) {
                openSides.clear();
                openSides.addAll(newSides);
                redrawBlock();
            }
        }
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("fluid = STUB(Tank not yet migrated)");
        left.add("open sides = " + openSides.stream().map(Enum::name).collect(Collectors.joining(", ")));
        left.add("delay = " + getCurrentDelay());
        left.add("tick = " + tick);
        left.add("queue size = " + queue.size());
    }
}

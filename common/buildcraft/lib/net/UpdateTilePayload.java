/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.net;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Fabric 1.20.1 replacement for the original {@code MessageUpdateTile} (Forge {@code IMessage}).
 *
 * Carries an arbitrary NBT blob keyed by a tile's {@link BlockPos}. The tile's
 * {@code TileBC_Neptune.receivePayload} method is invoked on the receiving side
 * after the NBT is unpacked by {@link BCNetworkManager}.
 *
 * Wire format (legacy parity):
 *   BlockPos    pos — target tile position
 *   NbtCompound tag — payload blob produced by {@code IPayloadWriter}
 *
 * TODO(R.Chen): the original layered packet format (PacketBufferBC with bit-level
 * read/write + IdAllocator-routed sub-messages) is preserved in the NBT blob for
 * now. A later pass can move the routing IDs into the typed packet itself.
 */
public record UpdateTilePayload(BlockPos pos, NbtCompound tag) implements FabricPacket {

    public static final PacketType<UpdateTilePayload> TYPE = PacketType.create(
        new Identifier("buildcraft", "update_tile"),
        UpdateTilePayload::read
    );

    public static UpdateTilePayload read(PacketByteBuf buf) {
        return new UpdateTilePayload(buf.readBlockPos(), buf.readNbt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(tag);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}

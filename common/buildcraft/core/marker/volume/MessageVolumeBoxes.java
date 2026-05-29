/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.core.marker.volume;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.netty.buffer.Unpooled;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.net.BCNetworkManager;
import buildcraft.lib.net.PacketBufferBC;

/**
 * Fabric 1.20.1 port of the original Forge {@code IMessage} MessageVolumeBoxes.
 *
 * Wire format is identical to the original (int count + [varInt len + bytes] per VolumeBox buffer).
 * The Forge IMessageHandler is replaced by a static {@link #handle} method registered via
 * {@link BCNetworkManager#registerClientReceiver} from the core module's client initializer.
 */
public final class MessageVolumeBoxes implements FabricPacket {

    public static final PacketType<MessageVolumeBoxes> TYPE = PacketType.create(
        new Identifier("buildcraft", "volume_boxes"),
        MessageVolumeBoxes::read
    );

    private final List<PacketBufferBC> buffers;

    // Single private canonical constructor; use static factories to construct instances.
    private MessageVolumeBoxes(List<PacketBufferBC> buffers) {
        this.buffers = buffers;
    }

    /** Server-side factory: serialises each VolumeBox into its own PacketBufferBC. */
    public static MessageVolumeBoxes of(List<VolumeBox> volumeBoxes) {
        List<PacketBufferBC> bufs = volumeBoxes.stream()
            .map(vb -> {
                PacketBufferBC buf = new PacketBufferBC(Unpooled.buffer());
                vb.toBytes(buf);
                return buf;
            })
            .collect(Collectors.toList());
        return new MessageVolumeBoxes(bufs);
    }

    public static MessageVolumeBoxes read(PacketByteBuf buf) {
        PacketBufferBC bc = PacketBufferBC.asPacketBufferBc(buf);
        List<PacketBufferBC> bufs = new ArrayList<>();
        int count = bc.readInt();
        for (int i = 0; i < count; i++) {
            int bytes = bc.readVarInt();
            bufs.add(new PacketBufferBC(bc.readBytes(bytes)));
        }
        return new MessageVolumeBoxes(bufs);
    }

    @Override
    public void write(PacketByteBuf buf) {
        PacketBufferBC bc = PacketBufferBC.asPacketBufferBc(buf);
        bc.writeInt(buffers.size());
        for (PacketBufferBC local : buffers) {
            bc.writeVarInt(local.readableBytes());
            bc.writeBytes(local, 0, local.readableBytes());
        }
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    // -----------------------------------------------------------------------
    // Client-side handler — registered via BCNetworkManager.registerClientReceiver from core's
    // ClientModInitializer. Ported from the original IMessageHandler.HANDLER static field.
    // -----------------------------------------------------------------------

    @Environment(EnvType.CLIENT)
    public static void registerClientHandler() {
        BCNetworkManager.registerClientReceiver(TYPE, MessageVolumeBoxes::handle);
    }

    @Environment(EnvType.CLIENT)
    private static void handle(MessageVolumeBoxes packet,
                                ClientPlayerEntity player,
                                PacketSender responseSender) {
        Map<PacketBufferBC, VolumeBox> incoming = packet.buffers.stream()
            .map(buf -> {
                VolumeBox vb;
                try {
                    vb = new VolumeBox(BCLibProxy.getProxy().getClientWorld(), buf);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                PacketBufferBC out = new PacketBufferBC(Unpooled.buffer());
                vb.toBytes(out);
                return Pair.of(out, vb);
            })
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        ClientVolumeBoxes.INSTANCE.volumeBoxes.removeIf(vb -> !incoming.values().contains(vb));
        for (Map.Entry<PacketBufferBC, VolumeBox> entry : incoming.entrySet()) {
            boolean wasContained = false;
            for (VolumeBox existing : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
                if (existing.equals(entry.getValue())) {
                    try {
                        existing.fromBytes(entry.getKey());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    wasContained = true;
                    break;
                }
            }
            if (!wasContained) {
                ClientVolumeBoxes.INSTANCE.volumeBoxes.add(entry.getValue());
                for (Addon addon : entry.getValue().addons.values()) {
                    if (addon != null) {
                        addon.onAdded();
                    }
                }
            }
        }
    }
}

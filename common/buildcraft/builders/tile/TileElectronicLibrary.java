/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.tile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.google.common.primitives.Bytes;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.data.NbtSquishConstants;

import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.nbt.NbtSquisher;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.Snapshot;

public class TileElectronicLibrary extends TileBC_Neptune implements ITickable {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("library");
    @SuppressWarnings("WeakerAccess")
    public static final int NET_DOWN = IDS.allocId("DOWN");
    @SuppressWarnings("WeakerAccess")
    public static final int NET_UP = IDS.allocId("UP");

    public final ItemHandlerSimple invDownIn = itemManager.addInvHandler("downIn", 1, EnumAccess.INSERT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invDownOut = itemManager.addInvHandler("downOut", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invUpIn = itemManager.addInvHandler("upIn", 1, EnumAccess.INSERT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invUpOut = itemManager.addInvHandler("upOut", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);
    public Snapshot.Key selected = null;
    private int progressDown = -1;
    private int progressUp = -1;
    public final DeltaInt deltaProgressDown = deltaManager.addDelta("progressDown", DeltaManager.EnumNetworkVisibility.GUI_ONLY);
    public final DeltaInt deltaProgressUp = deltaManager.addDelta("progressUp", DeltaManager.EnumNetworkVisibility.GUI_ONLY);
    private final Map<Pair<UUID, Snapshot.Key>, List<byte[]>> upSnapshotsParts = new HashMap<>();

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (handler == invDownIn) {
            if (progressDown > 0) {
                progressDown = -1;
                deltaProgressDown.setValue(0);
            }
        }
        if (handler == invUpIn) {
            if (progressUp > 0) {
                progressUp = -1;
                deltaProgressUp.setValue(0);
            }
        }
    }

    @Override
    public void update() {
        deltaManager.tick();

        if (world.isRemote) {
            return;
        }

        if (!invDownIn.getStackInSlot(0).isEmpty() && invDownOut.getStackInSlot(0).isEmpty()) {
            if (progressDown == -1) {
                progressDown = 0;
                deltaProgressDown.addDelta(0, 50, 1);
                deltaProgressDown.addDelta(50, 55, -1);
            }
            if (progressDown >= 50) {
                sendNetworkGuiUpdate(NET_DOWN);
                invDownOut.setStackInSlot(0, invDownIn.getStackInSlot(0));
                invDownIn.setStackInSlot(0, StackUtil.EMPTY);
                progressDown = -1;
            } else {
                progressDown++;
            }
        } else if (progressDown != -1) {
            progressDown = -1;
            deltaProgressDown.setValue(0);
        }

        if (selected != null && !invUpIn.getStackInSlot(0).isEmpty() && invUpOut.getStackInSlot(0).isEmpty()) {
            if (progressUp == -1) {
                progressUp = 0;
                deltaProgressUp.addDelta(0, 50, 1);
                deltaProgressUp.addDelta(50, 55, -1);
            }
            if (progressUp >= 50) {
                sendNetworkGuiUpdate(NET_UP);
                progressUp = -1;
            } else {
                progressUp++;
            }
        } else if (progressUp != -1) {
            progressUp = -1;
            deltaProgressUp.setValue(0);
        }
    }

    // How networking works here:
    // down:
    // 1. server sends NET_DOWN with snapshot to clients
    // 2. clients add snapshot to their local database
    // up:
    // 1. server sends empty NET_UP to clients
    // 2. client who have selected snapshot sends NET_UP with it back to server
    // 3. server adds snapshot to its database

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeBoolean(selected != null);
                if (selected != null) {
                    selected.writeToByteBuf(buffer);
                }
            }
            if (id == NET_DOWN) {
                Snapshot.Header header = BCBuildersItems.snapshot.getHeader(invDownIn.getStackInSlot(0));
                if (header != null) {
                    Snapshot snapshot = GlobalSavedDataSnapshots.get(world).getSnapshot(header.key);
                    if (snapshot != null) {
                        snapshot = snapshot.copy();
                        snapshot.key = new Snapshot.Key(snapshot.key, header);
                        buffer.writeBoolean(true);
                        NbtSquisher.squish(
                            Snapshot.writeToNBT(snapshot),
                            NbtSquishConstants.BUILDCRAFT_V1_COMPRESSED,
                            buffer
                        );
                    } else {
                        buffer.writeBoolean(false);
                    }
                } else {
                    buffer.writeBoolean(false);
                }
            }
            // noinspection StatementWithEmptyBody
            if (id == NET_UP) {
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                if (buffer.readBoolean()) {
                    selected = new Snapshot.Key(buffer);
                } else {
                    selected = null;
                }
            }
            if (id == NET_DOWN) {
                if (buffer.readBoolean()) {
                    Snapshot snapshot = Snapshot.readFromNBT(NbtSquisher.expand(buffer));
                    snapshot.computeKey();
                    GlobalSavedDataSnapshots.get(world).addSnapshot(snapshot);
                }
            }
            if (id == NET_UP) {
                if (selected != null) {
                    Snapshot snapshot = GlobalSavedDataSnapshots.get(world).getSnapshot(selected);
                    if (snapshot != null) {
                        try (OutputStream outputStream = new OutputStream() {
                            private byte[] buf = new byte[4 * 1024];
                            private int pos = 0;
                            private boolean closed = false;

                            private void write(boolean last) throws IOException {
                                MessageManager.sendToServer(createMessage(NET_UP, localBuffer -> {
                                    localBuffer.writeUniqueId(ctx.getClientHandler().getGameProfile().getId());
                                    selected.writeToByteBuf(localBuffer);
                                    localBuffer.writeBoolean(last);
                                    localBuffer.writeByteArray(buf);
                                }));
                            }

                            @Override
                            public void write(int b) throws IOException {
                                buf[pos++] = (byte) b;
                                if (pos >= buf.length) {
                                    write(false);
                                    buf = new byte[buf.length];
                                    pos = 0;
                                }
                            }

                            @Override
                            public void close() throws IOException {
                                if (closed) {
                                    return;
                                }
                                closed = true;
                                buf = Arrays.copyOf(buf, pos);
                                pos = 0;
                                write(true);
                            }
                        }) {
                            NbtSquisher.squish(
                                Snapshot.writeToNBT(snapshot),
                                NbtSquishConstants.BUILDCRAFT_V1_COMPRESSED,
                                outputStream
                            );
                        }
                    }
                }
            }
        }
        if (side == Side.SERVER) {
            if (id == NET_UP) {
                UUID playerId = buffer.readUniqueId();
                Snapshot.Key key = new Snapshot.Key(buffer);
                Pair<UUID, Snapshot.Key> pair = Pair.of(playerId, key);
                boolean last = buffer.readBoolean();
                upSnapshotsParts.computeIfAbsent(pair, localPair -> new ArrayList<>()).add(buffer.readByteArray());
                if (last && upSnapshotsParts.containsKey(pair)) {
                    try {
                        Snapshot snapshot = Snapshot.readFromNBT(
                            NbtSquisher.expand(
                                Bytes.concat(
                                    upSnapshotsParts.get(pair)
                                        .toArray(new byte[0][])
                                )
                            )
                        );
                        Snapshot.Header header = snapshot.key.header;
                        snapshot = snapshot.copy();
                        snapshot.key = new Snapshot.Key(snapshot.key, (Snapshot.Header) null);
                        snapshot.computeKey();
                        GlobalSavedDataSnapshots.get(world).addSnapshot(snapshot);
                        invUpOut.setStackInSlot(0, BCBuildersItems.snapshot.getUsed(snapshot.getType(), header));
                        invUpIn.setStackInSlot(0, StackUtil.EMPTY);
                    } finally {
                        upSnapshotsParts.remove(pair);
                    }
                }
            }
        }
    }
}

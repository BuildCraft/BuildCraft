/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.builders.tile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;
import com.google.common.primitives.Bytes;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.data.NbtSquishConstants;
import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.builders.item.ItemSnapshot;
import ct.buildcraft.builders.menu.ContainerElectronicLibrary;
import ct.buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.nbt.NbtSquisher;
import ct.buildcraft.lib.net.MessageManager;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.lib.tile.item.StackInsertionFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkEvent;

public class TileElectronicLibrary extends TileBC_Neptune implements MenuProvider{

	public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("library");
    public static final int NET_DOWN = IDS.allocId("DOWN");
    public static final int NET_UP = IDS.allocId("UP");

    public final ItemHandlerSimple invDownIn = itemManager.addInvHandler(
        "downIn",
        1,
        (slot, stack) -> isUsedSnapshot(stack),
        StackInsertionFunction.getInsertionFunction(1),
        EnumAccess.INSERT,
        EnumPipePart.VALUES
    );
    public final ItemHandlerSimple invDownOut = itemManager.addInvHandler(
        "downOut",
        1,
        StackInsertionFunction.getInsertionFunction(1),
        EnumAccess.EXTRACT,
        EnumPipePart.VALUES
    );
    public final ItemHandlerSimple invUpIn = itemManager.addInvHandler(
        "upIn",
        1,
        (slot, stack) -> isCleanSnapshot(stack),
        StackInsertionFunction.getInsertionFunction(1),
        EnumAccess.INSERT,
        EnumPipePart.VALUES
    );
    public final ItemHandlerSimple invUpOut = itemManager.addInvHandler(
        "upOut",
        1,
        StackInsertionFunction.getInsertionFunction(1),
        EnumAccess.EXTRACT,
        EnumPipePart.VALUES
    );
    public static final int TRANSFER_TIME = 50;

    public Snapshot.Key selected = null;
    private int progressDown = -1;
    private int progressUp = -1;
    private int progressDownLast = -1;
    private int progressUpLast = -1;
    private final Map<Pair<UUID, Snapshot.Key>, List<byte[]>> upSnapshotsParts = new HashMap<>();

    public TileElectronicLibrary(BlockPos pos, BlockState state) {
		super(BCBuildersBlocks.LIBRARY_TILE_BC8.get(), pos, state);
	}

    public static boolean isUsedSnapshot(ItemStack stack) {
        return ItemSnapshot.getHeader(stack) != null;
    }

    public static boolean isCleanSnapshot(ItemStack stack) {
        return stack.getItem() instanceof ItemSnapshot && !ItemSnapshot.EnumItemSnapshotType.getFromStack(stack).used;
    }

    private Snapshot getSnapshotForDownload() {
        Snapshot.Header header = ItemSnapshot.getHeader(invDownIn.getStackInSlot(0));
        return header == null || level == null ? null : GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
    }
    
    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (handler == invDownIn && progressDown > 0) {
            progressDown = -1;
        }
        if (handler == invUpIn && progressUp > 0) {
            progressUp = -1;
        }
    }

    @Override
    public void update() {
        if (level.isClientSide) {
            progressDownLast = progressDown;
            progressUpLast = progressUp;
            return;
        }

        progressDownLast = progressDown;
        progressUpLast = progressUp;

        boolean canDownload = getSnapshotForDownload() != null && invDownOut.getStackInSlot(0).isEmpty();
        if (canDownload) {
            if (progressDown == -1) {
                progressDown = 0;
            }
            if (progressDown >= TRANSFER_TIME) {
                sendNetworkGuiUpdate(NET_DOWN);
                ItemStack downloaded = invDownIn.getStackInSlot(0).copy();
                downloaded.setCount(1);
                invDownOut.setStackInSlot(0, downloaded);
                invDownIn.setStackInSlot(0, StackUtil.EMPTY);
                progressDown = -1;
            } else {
                progressDown++;
            }
        } else if (progressDown != -1) {
            progressDown = -1;
        }

        boolean canUpload = selected != null && isCleanSnapshot(invUpIn.getStackInSlot(0)) && invUpOut.getStackInSlot(0).isEmpty();
        if (canUpload) {
            if (progressUp == -1) {
                progressUp = 0;
            }
            if (progressUp >= TRANSFER_TIME) {
                sendNetworkGuiUpdate(NET_UP);
                progressUp = -1;
            } else {
                progressUp++;
            }
        } else if (progressUp != -1) {
            progressUp = -1;
        }
    }

    public double getDownloadProgress(float partialTicks) {
        return interpolateProgress(progressDownLast, progressDown, partialTicks);
    }

    public double getUploadProgress(float partialTicks) {
        return interpolateProgress(progressUpLast, progressUp, partialTicks);
    }

    private static double interpolateProgress(int last, int current, float partialTicks) {
        if (current < 0) {
            return 0;
        }
        if (last < 0) {
            last = current;
        }
        double value = last * (1.0D - partialTicks) + current * partialTicks;
        if (value <= 0) {
            return 0;
        }
        if (value >= TRANSFER_TIME) {
            return 1;
        }
        return value / TRANSFER_TIME;
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
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeBoolean(selected != null);
                if (selected != null) {
                    selected.writeToByteBuf(buffer);
                }
            }
            if (id == NET_DOWN) {
                Snapshot.Header header = ItemSnapshot.getHeader(invDownIn.getStackInSlot(0));
                Snapshot snapshot = getSnapshotForDownload();
                if (header != null && snapshot != null) {
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
            }
            // noinspection StatementWithEmptyBody
            if (id == NET_UP) {
            }
            if (id == NET_GUI_DATA || (id == NET_GUI_TICK && (progressDown != progressDownLast || progressUp != progressUpLast))) {
                buffer.writeVarInt(progressDown);
                buffer.writeVarInt(progressUp);
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
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
                    GlobalSavedDataSnapshots.get(level).addSnapshot(snapshot);
                }
            }
            if (id == NET_GUI_TICK || id == NET_GUI_DATA) {
                progressDownLast = progressDown;
                progressUpLast = progressUp;
                progressDown = buffer.readVarInt();
                progressUp = buffer.readVarInt();
            }
            if (id == NET_UP) {
                if (selected != null) {
                    Snapshot snapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(selected);
                    if (snapshot != null) {
                        try (OutputStream outputStream = new OutputStream() {
                            private byte[] buf = new byte[4 * 1024];
                            private int pos = 0;
                            private boolean closed = false;
                            
                            private void write(boolean last) throws IOException {
                                MessageManager.sendToServer(createMessage(NET_UP, localBuffer -> {
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
        if (side == LogicalSide.SERVER) {
            if (id == NET_UP) {
                ServerPlayer sender = ctx == null ? null : ctx.getSender();
                UUID playerId = sender == null ? new UUID(0L, 0L) : sender.getUUID();
                Snapshot.Key key = new Snapshot.Key(buffer);
                Pair<UUID, Snapshot.Key> pair = Pair.of(playerId, key);
                boolean last = buffer.readBoolean();
                upSnapshotsParts.computeIfAbsent(pair, localPair -> new ArrayList<>()).add(buffer.readByteArray());
                if (last && upSnapshotsParts.containsKey(pair)) {
                    try {
                        if (selected == null || !selected.equals(key) || !isCleanSnapshot(invUpIn.getStackInSlot(0)) || !invUpOut.getStackInSlot(0).isEmpty()) {
                            return;
                        }
                        Snapshot snapshot = Snapshot.readFromNBT(
                            NbtSquisher.expand(
                                Bytes.concat(
                                    upSnapshotsParts.get(pair)
                                        .toArray(new byte[0][])
                                )
                            )
                        );
                        Snapshot.Header header = snapshot.key.header;
                        if (header == null) {
                            return;
                        }
                        snapshot = snapshot.copy();
                        snapshot.key = new Snapshot.Key(snapshot.key, (Snapshot.Header) null);
                        snapshot.computeKey();
                        GlobalSavedDataSnapshots.get(level).addSnapshot(snapshot);
                        invUpOut.setStackInSlot(0, ItemSnapshot.getUsed(snapshot.getType(), header));
                        invUpIn.setStackInSlot(0, StackUtil.EMPTY);
                    } finally {
                        upSnapshotsParts.remove(pair);
                    }
                }
            }
        }
    }
    
    

	@Override
	public void saveAdditional(CompoundTag nbt) {
		// TODO Auto-generated method stub
		super.saveAdditional(nbt);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerElectronicLibrary(id, inv, invDownOut, invDownIn, invUpIn, invUpOut, ContainerLevelAccess.create(level, worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return getBlockState().getBlock().getName();
	}
}

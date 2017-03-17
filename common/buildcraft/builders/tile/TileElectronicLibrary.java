/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class TileElectronicLibrary extends TileBC_Neptune implements ITickable {
    public static final int NET_DOWN = 20;
    public static final int NET_UP = 21;

    public final ItemHandlerSimple invDownIn = itemManager.addInvHandler("downIn", 1, EnumAccess.INSERT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invDownOut = itemManager.addInvHandler("downOut", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invUpIn = itemManager.addInvHandler("upIn", 1, EnumAccess.INSERT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invUpOut = itemManager.addInvHandler("upOut", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);
    public Snapshot.Header selected = null;

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (handler == invDownIn) {
            sendNetworkGuiUpdate(NET_DOWN);
        }
        if (handler == invUpIn) {
            sendNetworkGuiUpdate(NET_UP);
        }
    }

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }
    }

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
                    Snapshot snapshot = GlobalSavedDataSnapshots.get(world).getSnapshotByHeader(header);
                    if (snapshot != null) {
                        buffer.writeBoolean(true);
                        NBTTagCompound nbt = snapshot.serializeNBT();
                        nbt.setTag("type", NBTUtilBC.writeEnum(snapshot.getType()));
                        buffer.writeCompoundTag(nbt);
                    } else {
                        buffer.writeBoolean(false);
                    }
                } else {
                    buffer.writeBoolean(false);
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                if (buffer.readBoolean()) {
                    selected = new Snapshot.Header();
                    selected.readFromByteBuf(buffer);
                } else {
                    selected = null;
                }
            }
            if (id == NET_DOWN) {
                if (buffer.readBoolean()) {
                    NBTTagCompound nbt = buffer.readCompoundTag();
                    Objects.requireNonNull(nbt);
                    Snapshot snapshot = NBTUtilBC.readEnum(nbt.getTag("type"), Snapshot.EnumSnapshotType.class).create.get();
                    snapshot.deserializeNBT(nbt);
                    snapshot.header.id = UUID.randomUUID();
                    GlobalSavedDataSnapshots.get(world).snapshots.add(snapshot);
                }
            }
            if (id == NET_UP) {
                if (selected != null) {
                    Snapshot snapshot = GlobalSavedDataSnapshots.get(world).getSnapshotByHeader(selected);
                    if (snapshot != null) {
                        MessageUtil.getWrapper().sendToServer(createMessage(NET_UP, localBuffer -> {
                            NBTTagCompound nbt = snapshot.serializeNBT();
                            nbt.setTag("type", NBTUtilBC.writeEnum(snapshot.getType()));
                            localBuffer.writeCompoundTag(nbt);
                        }));
                    }
                }
            }
        }
        if (side == Side.SERVER) {
            NBTTagCompound nbt = buffer.readCompoundTag();
            Objects.requireNonNull(nbt);
            Snapshot snapshot = NBTUtilBC.readEnum(nbt.getTag("type"), Snapshot.EnumSnapshotType.class).create.get();
            snapshot.deserializeNBT(nbt);
            snapshot.header.id = UUID.randomUUID();
            invUpIn.setStackInSlot(0, ItemStack.EMPTY);
            GlobalSavedDataSnapshots.get(world).snapshots.add(snapshot);
            invUpOut.setStackInSlot(0, BCBuildersItems.snapshot.getUsed(snapshot.getType(), snapshot.header));
        }
    }
}

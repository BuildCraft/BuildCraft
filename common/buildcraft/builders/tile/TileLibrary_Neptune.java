/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package buildcraft.builders.tile;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ITickable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.builders.gui.GuiBlueprintLibrary;
import buildcraft.lib.BCLibDatabase;
import buildcraft.lib.library.ILibraryEntryData;
import buildcraft.lib.library.LibraryEntry;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public class TileLibrary_Neptune extends TileBCInventory_Neptune implements ITickable {
    public static final int NET_SELECTED = 20;

    public enum LibSlot {
        SAVE_IN,
        SAVE_OUT,
        LOAD_IN,
        LOAD_OUT
    }

    // TODO: some sort of library entry database
    // -- sync server <-> client
    // -- load disk <-> db
    // -- read + writers

    public final IItemHandlerModifiable inv;
    public LibraryEntryHeader selected = null;

    public TileLibrary_Neptune() {
        inv = addInventory("inv", 4, EnumAccess.NONE);
    }

    @Override
    public void update() {
        if (worldObj.isRemote) {
            return;
        }

        ItemStack saving = get(LibSlot.SAVE_IN);
        if (get(LibSlot.SAVE_OUT) == null && saving != null) {
            LibraryEntry entry = BCLibDatabase.readEntryFromStack(saving.copy());
            if (entry != null) {
                set(LibSlot.SAVE_IN, null);
                set(LibSlot.SAVE_OUT, saving);

                BCLibDatabase.LOCAL_DB.addNew(entry.header, entry.data);
                BCLibDatabase.fillEntries();
            }
        }

        ItemStack loading = get(LibSlot.LOAD_IN);
        if (get(LibSlot.LOAD_OUT) == null && loading != null) {
            ILibraryEntryData data = BCLibDatabase.LOCAL_DB.getEntry(selected);
            if (data != null) {
                ItemStack out = BCLibDatabase.writeEntryToStack(loading.copy(), selected, data);
                if (out != null) {
                    set(LibSlot.LOAD_IN, null);
                    set(LibSlot.LOAD_OUT, out);
                }
            }
        }
    }

    public ItemStack get(LibSlot slot) {
        return inv.getStackInSlot(slot.ordinal());
    }

    public void set(LibSlot slot, ItemStack stack) {
        inv.setStackInSlot(slot.ordinal(), stack);
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);

        if (id == NET_GUI_DATA || id == NET_SELECTED) {
            if (selected == null) {
                buffer.writeBoolean(false);
            } else {
                buffer.writeBoolean(true);
                selected.writeToByteBuf(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);

        if (id == NET_GUI_DATA || id == NET_SELECTED) {
            boolean has = buffer.readBoolean();
            if (has) {
                selected = new LibraryEntryHeader(buffer);
            } else {
                selected = null;
            }
            if (side == Side.SERVER) {
                MessageUtil.doDelayed(() -> {
                    sendNetworkUpdate(NET_SELECTED);
                });
            } else if (side == Side.CLIENT) {
                updateSelected();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void updateSelected() {
        GuiScreen currentGui = Minecraft.getMinecraft().currentScreen;
        if (currentGui instanceof GuiBlueprintLibrary) {
            GuiBlueprintLibrary guiBptLib = (GuiBlueprintLibrary) currentGui;
            if (guiBptLib.container.tile == this) {
                guiBptLib.selected = selected;
            }
        }
    }
}

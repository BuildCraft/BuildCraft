package buildcraft.builders.tile;

import java.time.LocalDateTime;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.lib.BCLibDatabase;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.library.book.LibraryEntryBook;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.permission.PlayerOwner;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public class TileLibrary_Neptune extends TileBCInventory_Neptune implements ITickable {
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
            if (saving.getItem() == Items.WRITTEN_BOOK) {

                LibraryEntryBook data = LibraryEntryBook.create(saving);
                NBTTagCompound nbt = NBTUtils.getItemData(saving);
                if (data != null && nbt != null) {
                    PlayerOwner author = PlayerOwner.lookup(nbt.getString("author"));
                    String title = nbt.getString("title");

                    LibraryEntryHeader header = new LibraryEntryHeader(title, LibraryEntryBook.KIND, LocalDateTime.of(2016, 1, 1, 0, 0), author);

                    BCLibDatabase.LOCAL_DB.addNew(header, data);

                    set(LibSlot.SAVE_IN, null);
                    set(LibSlot.SAVE_OUT, saving);

                    BCLibDatabase.fillEntries();
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
}

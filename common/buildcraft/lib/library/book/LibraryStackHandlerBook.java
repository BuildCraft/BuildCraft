package buildcraft.lib.library.book;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.lib.library.ILibraryEntryData;
import buildcraft.lib.library.ILibraryStackHandler;
import buildcraft.lib.library.LibraryEntry;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.permission.PlayerOwner;

public enum LibraryStackHandlerBook implements ILibraryStackHandler {
    INSTANCE;

    @Override
    @Nullable
    public LibraryEntry readEntryFromStack(@Nonnull ItemStack from) {
        if (from.getItem() == Items.WRITTEN_BOOK) {

            LibraryEntryBook data = LibraryEntryBook.create(from);
            NBTTagCompound nbt = NBTUtils.getItemData(from);
            if (data != null && nbt != null) {
                PlayerOwner author = PlayerOwner.lookup(nbt.getString("author"));
                String title = nbt.getString("title");

                LibraryEntryHeader header = new LibraryEntryHeader(title, LibraryEntryBook.KIND, LocalDateTime.now(), author);

                return new LibraryEntry(header, data);
            }
        }
        return null;
    }

    @Override
    @Nullable
    public ItemStack writeEntryToStack(@Nonnull ItemStack to, LibraryEntryHeader hader, ILibraryEntryData data) {
        if (data instanceof LibraryEntryBook) {
            
        }
        return null;
    }
}

package buildcraft.lib.library.book;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.BCLog;
import buildcraft.lib.library.ILibraryEntryData;
import buildcraft.lib.library.ILibraryStackHandler;
import buildcraft.lib.library.LibraryEntry;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.permission.PlayerOwner;

public enum LibraryStackHandlerBook implements ILibraryStackHandler {
    INSTANCE;

    private static final String NBT_DATE = "buildcraft_creation_date";

    @Override
    @Nullable
    public LibraryEntry readEntryFromStack(@Nonnull ItemStack from) {
        if (from.getItem() == Items.WRITTEN_BOOK) {

            LibraryEntryBook data = LibraryEntryBook.create(from);
            NBTTagCompound nbt = NBTUtils.getItemData(from);
            if (data != null && nbt != null) {
                PlayerOwner author = PlayerOwner.lookup(nbt.getString("author"));
                String title = nbt.getString("title");

                LocalDateTime dateTime = null;
                if (nbt.hasKey(NBT_DATE)) {
                    dateTime = NBTUtils.readLocalDateTime(nbt.getCompoundTag(NBT_DATE));
                } else {
                    dateTime = LocalDateTime.now();
                }

                LibraryEntryHeader header = new LibraryEntryHeader(title, LibraryEntryBook.KIND, dateTime, author);

                return new LibraryEntry(header, data);
            }
        }
        return null;
    }

    @Override
    @Nullable
    public ItemStack writeEntryToStack(@Nonnull ItemStack to, LibraryEntryHeader header, ILibraryEntryData data) {
        if (to.getItem() != Items.BOOK || to.stackSize != 1) {
            return null;
        }
        if (data instanceof LibraryEntryBook) {
            LibraryEntryBook book = (LibraryEntryBook) data;
            ItemStack newStack = book.saveToStack();
            NBTTagCompound nbt = NBTUtils.getItemData(newStack);
            nbt.setTag(NBT_DATE, NBTUtils.writeLocalDateTime(header.creation));
            String auth = header.author.getOwnerName();
            if (auth == null) {
                BCLog.logger.warn("Unknown author! (" + header + ")");
                return null;
            }
            nbt.setString("author", auth);
            nbt.setString("title", header.name);

            if (ItemWrittenBook.validBookTagContents(nbt)) {
                return newStack;
            }
        }
        return null;
    }
}

package buildcraft.lib.library.book;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;

import buildcraft.api.core.BCLog;

import buildcraft.lib.library.ILibraryEntryData;
import buildcraft.lib.library.ILibraryStackHandler;
import buildcraft.lib.library.LibraryEntry;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.misc.NBTUtilBC;

public enum LibraryStackHandlerBook implements ILibraryStackHandler {
    INSTANCE;

    private static final String NBT_DATE = "buildcraft_creation_date";

    @Override
    @Nullable
    public LibraryEntry readEntryFromStack(@Nonnull ItemStack from) {
        if (from.getItem() == Items.WRITTEN_BOOK) {

            LibraryEntryBook data = LibraryEntryBook.create(from);
            NBTTagCompound nbt = NBTUtilBC.getItemData(from);
            if (data != null && nbt != null) {
                GameProfile author = NBTUtil.readGameProfileFromNBT(nbt.getCompoundTag("author"));
                String title = nbt.getString("title");

                LocalDateTime dateTime = null;
                if (nbt.hasKey(NBT_DATE)) {
                    dateTime = NBTUtilBC.readLocalDateTime(nbt.getCompoundTag(NBT_DATE));
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
        if (to.getItem() != Items.BOOK || to.getCount() != 1) {
            return null;
        }
        if (data instanceof LibraryEntryBook) {
            LibraryEntryBook book = (LibraryEntryBook) data;
            ItemStack newStack = book.saveToStack();
            NBTTagCompound nbt = NBTUtilBC.getItemData(newStack);
            nbt.setTag(NBT_DATE, NBTUtilBC.writeLocalDateTime(header.creation));
            GameProfile author = header.author;
            if (author == null || !author.isComplete()) {
                BCLog.logger.warn("Unknown author! (" + header + ")");
                return null;
            }
            nbt.setTag("author", NBTUtil.writeGameProfile(new NBTTagCompound(), header.author));
            nbt.setString("title", header.name);

            if (ItemWrittenBook.validBookTagContents(nbt)) {
                return newStack;
            }
        }
        return null;
    }
}

package buildcraft.lib.library;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

public interface ILibraryStackHandler {
    /** @param stack The stack to read from. You should not modify this, as this may or may not be the actual stack.
     * @return The read entry, or null if you could not read an entry from the stack. */
    @Nullable
    LibraryEntry readEntryFromStack(@Nonnull ItemStack stack);

    /** @param from The stack to use up as a thing to write to.
     * @param hader
     * @param data
     * @return The newly written item, or null if you could not write teh given data to the given item. */
    @Nullable
    ItemStack writeEntryToStack(@Nonnull ItemStack from, LibraryEntryHeader hader, ILibraryEntryData data);
}

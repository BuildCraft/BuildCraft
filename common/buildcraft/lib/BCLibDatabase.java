package buildcraft.lib;

import static java.time.temporal.ChronoField.*;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.library.*;
import buildcraft.lib.library.book.LibraryEntryBook;
import buildcraft.lib.library.book.LibraryStackHandlerBook;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.WorkerThreadUtil;

public class BCLibDatabase {
    public static final DateTimeFormatter DATE_TIME_FORMATTER;

    public static final Map<String, ILibraryEntryType> FILE_HANDLERS = new HashMap<>();
    public static final List<ILibraryStackHandler> STACK_HANDLERS = new ArrayList<>();
    public static final LocalLibraryDatabase LOCAL_DB = new LocalLibraryDatabase();
    public static RemoteLibraryDatabase remoteDB = null;

    /** A list of all the current entries, at this time. This should be used by GUI's on the client to determine what
     * entries are available. */
    public static final List<LibraryEntryHeader> allEntries = new ArrayList<>();

    static {
        DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()//
                .append(DateTimeFormatter.ISO_DATE)//
                .appendLiteral(" - ")//
                .appendValue(HOUR_OF_DAY, 2)//
                .appendLiteral(':')//
                .appendValue(MINUTE_OF_HOUR, 2)//
                .appendLiteral(':')//
                .appendValue(SECOND_OF_MINUTE, 2)//
                .toFormatter();

        FILE_HANDLERS.put(LibraryEntryBook.KIND, LibraryEntryBook::new);
        STACK_HANDLERS.add(LibraryStackHandlerBook.INSTANCE);
    }

    public static void fmlInit() {
        WorkerThreadUtil.executeDependantTask(LOCAL_DB::readAll);
    }

    public static void onServerStarted() {
        LOCAL_DB.onServerStarted();
        fillEntries();
    }

    @SideOnly(Side.CLIENT)
    public static void connectToServer() {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
            remoteDB = null;
            fillEntries();
            return;
        }
        remoteDB = new RemoteLibraryDatabase();
        MessageUtil.doDelayed(10, () -> RemoteLibraryDatabase.requestIndex());
    }

    /** Re-populates the {@link #allEntries} list from the available databases. Call this after changing one of them. */
    public static void fillEntries() {
        allEntries.clear();
        allEntries.addAll(BCLibDatabase.LOCAL_DB.getAllHeaders());
        if (BCLibDatabase.remoteDB != null) {
            for (LibraryEntryHeader header : BCLibDatabase.remoteDB.getAllHeaders()) {
                if (!allEntries.contains(header)) {
                    allEntries.add(header);
                }
            }
        }
        allEntries.sort(Comparator.naturalOrder());
    }

    public static EntryStatus getStatus(LibraryEntryHeader header) {
        boolean local = LOCAL_DB.getAllHeaders().contains(header);
        boolean remote = true;
        if (remoteDB != null) {
            remote = remoteDB.getAllHeaders().contains(header);
        }
        if (local) {
            if (remote) return EntryStatus.BOTH;
            else return EntryStatus.LOCAL;
        } else {
            if (remote) return EntryStatus.REMOTE;
            else return EntryStatus.NEITHER;
        }
    }

    // ILibraryStackHandler quick-read

    @Nullable
    public static LibraryEntry readEntryFromStack(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        for (ILibraryStackHandler handler : STACK_HANDLERS) {
            LibraryEntry entry = handler.readEntryFromStack(stack);
            if (entry != null) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    public static ItemStack writeEntryToStack(ItemStack from, LibraryEntryHeader hader, ILibraryEntryData data) {
        if (from == null) {
            return null;
        }
        for (ILibraryStackHandler handler : STACK_HANDLERS) {
            ItemStack to = handler.writeEntryToStack(from, hader, data);
            if (to != null) {
                return to;
            }
        }
        return null;
    }

    /** All of the possible states of an entry, in terms of local and remote databases */
    public enum EntryStatus {
        NEITHER,
        REMOTE,
        LOCAL,
        BOTH;
    }
}

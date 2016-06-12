package buildcraft.lib.library.book;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import net.minecraftforge.common.util.Constants;

import buildcraft.lib.library.LibraryEntryData;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.data.ZipFileHelper;
import buildcraft.lib.permission.PlayerOwner;

public class LibraryEntryBook implements LibraryEntryData {
    public static final String KIND = "book";
    private final LibraryEntryHeader header;
    private final List<String> pages = new ArrayList<>();

    public LibraryEntryBook(ZipFileHelper helper) throws IOException {
        LibraryEntryHeader h = null;
        for (String key : helper.getKeys()) {
            if (key.equals("header.nbt")) {
                h = new LibraryEntryHeader(helper.getNbtEntry(key), KIND);
            }
            if (key.startsWith("page/") && key.endsWith(".txt")) {
                String pageIndex = key.substring("page/".length(), key.length() - ".txt".length());
                int index = parseIndex(pageIndex);
                while (pages.size() < index) {
                    pages.add("");
                }
                pages.set(index - 1, helper.getTextEntry(key));
            }
        }
        header = h;
        if (header == null) {
            throw new IOException("Did not find a header!");
        }
    }

    public LibraryEntryBook(LibraryEntryHeader header, List<String> pages) {
        this.header = header;
        this.pages.addAll(pages);
    }

    public static LibraryEntryBook create(ItemStack stack) {
        if (stack.getItem() == Items.WRITTEN_BOOK || stack.getItem() == Items.WRITABLE_BOOK) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null) {
                return null;
            }

            if (!ItemWritableBook.isNBTValid(nbt)) {
                return null;
            }

            PlayerOwner author = PlayerOwner.lookup(nbt.getString("author"));
            LocalDateTime time;
            if (nbt.hasKey("creation")) {
                time = NBTUtils.readLocalDateTime(nbt.getCompoundTag("creation"));
            } else {
                time = LocalDateTime.now();
            }

            String name = nbt.getString("title");

            if (StringUtils.isNullOrEmpty(name)) {
                name = "Unknown";
            }
            LibraryEntryHeader header = new LibraryEntryHeader(name, KIND, time, author);

            NBTTagList pagesNbt = nbt.getTagList("pages", Constants.NBT.TAG_STRING);
            List<String> pages = new ArrayList<>();
            for (int i = 0; i < pagesNbt.tagCount(); i++) {
                pages.add(prettifyPage(pagesNbt.getStringTagAt(i)));
            }

            return new LibraryEntryBook(header, pages);
        } else {
            return null;
        }
    }

    private static String prettifyPage(String json) {
        ITextComponent component;
        try {
            component = ITextComponent.Serializer.fromJsonLenient(json);
        } catch (Exception ex) {
            component = new TextComponentString(json);
        }

        String pretty = component.getFormattedText();
        pretty = pretty.replaceAll("\\R", "\n");

        if (pretty.endsWith("§r")) {
            pretty = pretty.substring(0, pretty.length() - 2);
        }

        return pretty;
    }

    private static int parseIndex(String pageIndex) throws IOException {
        try {
            return Integer.parseInt(pageIndex);
        } catch (NumberFormatException nfe) {
            throw new IOException("Invalid integer", nfe);
        }
    }

    public LibraryEntryHeader getHeader() {
        return header;
    }

    @Override
    public LibraryEntryHeader write(ZipFileHelper helper) {
        if (!pages.isEmpty()) {
            int pageNum = 1;
            for (String page : pages) {
                helper.addTextEntry("page/" + pad(pageNum, ((pages.size()) / 10) + 1) + ".txt", "", page);
                pageNum++;
            }
        }
        return header;
    }

    private static String pad(int pageNum, int i) {
        String text = Integer.toString(pageNum);
        while (text.length() < i) {
            text = "0" + text;
        }
        return text;
    }
}

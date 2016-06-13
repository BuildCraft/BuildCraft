package buildcraft.lib.library.book;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import net.minecraftforge.common.util.Constants;

import buildcraft.lib.library.LibraryEntryData;
import buildcraft.lib.misc.data.ZipFileHelper;

public class LibraryEntryBook implements LibraryEntryData {
    public static final String KIND = "book";
    private final List<String> pages = new ArrayList<>();

    public LibraryEntryBook(ZipFileHelper helper) throws IOException {
        for (String key : helper.getKeys()) {
            if (key.startsWith("page/") && key.endsWith(".txt")) {
                String pageIndex = key.substring("page/".length(), key.length() - ".txt".length());
                int index = parseIndex(pageIndex);
                while (pages.size() < index) {
                    pages.add("");
                }
                pages.set(index - 1, helper.getTextEntry(key));
            }
        }
    }

    public LibraryEntryBook(List<String> pages) {
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

            NBTTagList pagesNbt = nbt.getTagList("pages", Constants.NBT.TAG_STRING);
            List<String> pages = new ArrayList<>();
            for (int i = 0; i < pagesNbt.tagCount(); i++) {
                pages.add(prettifyPage(pagesNbt.getStringTagAt(i)));
            }

            return new LibraryEntryBook(pages);
        } else {
            return null;
        }
    }

    private static String prettifyPage(String json) {
        ITextComponent component;
        try {
            component = ITextComponent.Serializer.fromJsonLenient(json);
        } catch (Exception ignored) {
            component = new TextComponentString(json);
        }

        String pretty = component.getFormattedText();
        pretty = pretty.replaceAll("\\R", "\n");

        // TODO: Replace every duplicate "§*§*" with "§*", where * is the same char.

        // Remove the last "reset" that is added
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

    @Override
    public void write(ZipFileHelper helper) {
        if (!pages.isEmpty()) {
            int pageNum = 1;
            for (String page : pages) {
                helper.addTextEntry("page/" + pad(pageNum, ((pages.size()) / 10) + 1) + ".txt", "", page);
                pageNum++;
            }
        }
    }

    private static String pad(int pageNum, int i) {
        String text = Integer.toString(pageNum);
        while (text.length() < i) {
            text = "0" + text;
        }
        return text;
    }
}

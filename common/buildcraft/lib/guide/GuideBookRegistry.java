package buildcraft.lib.guide;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.script.ScriptableRegistry;

public class GuideBookRegistry extends ScriptableRegistry<GuideBook> {

    public static final GuideBookRegistry INSTANCE = new GuideBookRegistry();

    private GuideBookRegistry() {
        super(PackType.DATA_PACK, "buildcraft/book");
        addCustomType("", GuideBook.DESERIALISER);
    }

    @Nullable
    public GuideBook getBook(String bookName) {
        ResourceLocation loc = new ResourceLocation(bookName);
        GuideBook guideBook = getReloadableEntryMap().get(loc);
        if (guideBook != null) {
            return guideBook;
        }
        for (GuideBook book : getPermanent()) {
            if (book.name.toString().equals(bookName)) {
                return book;
            }
        }
        return null;
    }
}

package buildcraft.lib.client.guide;

import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.client.guide.entry.IEntryIterable;
import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.entry.PageEntryExternal;
import buildcraft.lib.client.guide.entry.PageEntryItemStack;
import buildcraft.lib.client.guide.entry.PageEntryStatement;
import buildcraft.lib.script.ScriptableRegistry;

public class GuidePageRegistry extends ScriptableRegistry<PageEntry> {

    public static final GuidePageRegistry INSTANCE = new GuidePageRegistry();

    public static final List<IEntryIterable> ENTRY_ITERABLES = new ArrayList<>();

    static {
        ENTRY_ITERABLES.add(PageEntryExternal.ITERABLE);
        ENTRY_ITERABLES.add(PageEntryItemStack.ITERABLE);
        ENTRY_ITERABLES.add(PageEntryStatement.ITERABLE);
    }

    private GuidePageRegistry() {
        super(PackType.RESOURCE_PACK, "buildcraft/guide", PageEntry.class);
        addCustomType("external", PageEntryExternal.DESERIALISER);
        addCustomType("item_stack", PageEntryItemStack.DESERIALISER);
        addCustomType("statement", PageEntryStatement.DESERIALISER);
    }
}

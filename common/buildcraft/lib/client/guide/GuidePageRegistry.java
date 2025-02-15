package buildcraft.lib.client.guide;

import buildcraft.lib.client.guide.entry.*;
import buildcraft.lib.script.ScriptableRegistry;

import java.util.HashMap;
import java.util.Map;

public class GuidePageRegistry extends ScriptableRegistry<PageEntry<?>> {

    public static final GuidePageRegistry INSTANCE = new GuidePageRegistry();

    public final Map<String, PageValueType<?>> types = new HashMap<>();

    private GuidePageRegistry() {
        super(PackType.RESOURCE_PACK, "buildcraft/guide");
        addType("item_stack", PageEntryItemStack.INSTANCE);
        addType("external", PageEntryExternal.INSTANCE);
        addType("statement", PageEntryStatement.INSTANCE);
    }

    public <T> void addType(String name, PageValueType<T> type) {
        types.put(name, type);
        addCustomType(name, (id, json, ctx) ->
        {
            OptionallyDisabled<PageEntry<T>> o1 = type.deserialize(id, json, ctx);
            // While we can cast PageEntry<T> to PageEntry<?>
            // we can't cast from OpDis<PageEntry<T>> to OpDis<PageEntry<?>>
            // so this is a little hoop jumping to allow it to compile safely.
            if (o1.isPresent()) {
                return new OptionallyDisabled<>(o1.get());
            } else {
                return new OptionallyDisabled<>(o1.getDisabledReason());
            }
        });
    }
}

package buildcraft.lib.client.guide.loader.entry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import buildcraft.lib.gui.ISimpleDrawable;

/** @param <T> The type. This should either override {@link #equals(Object)} and {@link #hashCode()} unless the object
 *            can be compared for equivalence with an identity check. */
public abstract class PageEntryType<T> {

    public static final Map<String, PageEntryType<?>> REGISTRY = new HashMap<>();

    static {
        // Item stack's are the default if no type is specified.
        register("", EntryTypeItem.INSTANCE);
        register(EntryTypeItem.ID, EntryTypeItem.INSTANCE);
        register(EntryTypeStatement.ID, EntryTypeStatement.INSTANCE);
    }

    public static void register(String id, PageEntryType<?> type) {
        REGISTRY.put(id, type);
    }

    @Nullable
    public abstract T deserialise(String source);

    /** @param target A value that has been returned by {@link #deserialise(String)}
     * @param value An unknown value.
     * @return */
    public boolean matches(T target, Object value) {
        return Objects.equals(target, value);
    }

    public abstract List<String> getTooltip(T value);

    @Nullable
    public abstract ISimpleDrawable createDrawable(T value);
}

package buildcraft.lib.client.guide.entry;

import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class PageValue<T> {

    public final PageValueType<T> type;
    public final Component title;
    public final String titleKey;
    public final T value;

    public PageValue(PageValueType<T> type, T value) {
        this.type = type;
        this.title = type.getTitle(value);
        this.titleKey = type.getTitleKey(value);
        this.value = value;
    }

    public static String getTitle(JsonObject json) {
        Component component = JsonUtil.getTextComponent(json, "title", "buildcraft.guide.page.");
        return component.getString();
    }

    /** @param test An unknown object.
     * @return True if it matches {@link #value} */
    public boolean matches(Object test) {
        return type.matches(value, test);
    }

    @Nullable
    public ISimpleDrawable createDrawable() {
        return type.createDrawable(value);
    }

    /** @return A value to be added to {@link GuideManager#objectsAdded} so that
     *         {@link IEntryIterable#iterateAllDefault(IEntryLinkConsumer)} can ignore similar entries. */
    public Object getBasicValue() {
        return type.getBasicValue(value);
    }

    public List<Component> getTooltip() {
        return type.getTooltip(value);
    }

    public PageValue<T> copyToValue() {
        return new PageValue<>(type, value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) {
            return false;
        }
        PageValue<?> other = (PageValue<?>) obj;
        return Objects.equals(value, other.value);
    }
}

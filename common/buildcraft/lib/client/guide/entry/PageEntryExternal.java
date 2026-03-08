package buildcraft.lib.client.guide.entry;

import buildcraft.api.registry.IScriptableRegistry.OptionallyDisabled;
import buildcraft.lib.gui.ISimpleDrawable;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collections;
import java.util.List;

/** An external page type that doesn't correspond to any object in the game. Used for detailing configs, resource packs,
 * model files, etc. */
public class PageEntryExternal extends PageValueType<String> {

    public static final PageEntryExternal INSTANCE = new PageEntryExternal();

    @Override
    public OptionallyDisabled<PageEntry<String>> deserialize(ResourceLocation name, JsonObject json, JsonDeserializationContext ctx) {
        String value = PageValue.getTitle(json);
        return new OptionallyDisabled<>(new PageEntry<>(this, name, json, value));
    }

    @Override
    public Class<String> getEntryClass() {
        return String.class;
    }

    @Override
    public List<Component> getTooltip(String value) {
        return Collections.singletonList(new TextComponent(value));
    }

    @Override
    public ISimpleDrawable createDrawable(String value) {
        return null;
    }

    @Override
    public Component getTitle(String value) {
//        return new TextComponent(value);
        return new TranslatableComponent(value);
    }

    // Calen

    @Override
    public String getTitleKey(String value) {
        return value;
    }

    @Override
    public void iterateAllDefault(IEntryLinkConsumer consumer, ProfilerFiller prof) {
        // NO-OP: everything is provided as-is.
    }
}

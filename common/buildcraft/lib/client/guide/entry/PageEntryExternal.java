package buildcraft.lib.client.guide.entry;

import java.util.Collections;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import buildcraft.api.registry.IScriptableRegistry.ISimpleEntryDeserializer;

import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.gui.ISimpleDrawable;

/** An external page type that doesn't correspond to any object in the game. Used for detailing configs, resource packs,
 * model files, etc. */
public class PageEntryExternal extends PageEntry<String> {

    public static final IEntryIterable ITERABLE = consumer -> {
        // Nothing to iterate though, as everything is defined through json.
    };

    public static final ISimpleEntryDeserializer<PageEntryExternal> DESERIALISER = (name, jObject, ctx) -> {
        String value = JsonUtils.getString(jObject, "title");
        return new PageEntryExternal(name, jObject, value, ctx);
    };

    public PageEntryExternal(ResourceLocation name, JsonObject json, String value, JsonDeserializationContext ctx)
        throws JsonParseException {
        super(name, json, value, ctx);
    }

    public PageEntryExternal(JsonTypeTags typeTags, ResourceLocation book, ITextComponent title, String value) {
        super(typeTags, book, title, value);
    }

    @Override
    public List<String> getTooltip() {
        return Collections.singletonList(title.getFormattedText());
    }

    @Override
    public ISimpleDrawable createDrawable() {
        return null;
    }

    @Override
    public boolean matches(Object test) {
        return value.equals(test);
    }

    @Override
    public Object getBasicValue() {
        return value;
    }
}

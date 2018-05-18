package buildcraft.lib.client.guide.loader.entry;

import java.util.Collections;
import java.util.List;

import buildcraft.lib.gui.ISimpleDrawable;

/** A separate page type that doesn't correspond to any object in the game. Used for detailing configs, resource packs,
 * model files, etc. */
public class EntryTypeSeperate extends PageEntryType<String> {

    public static final String ID = "seperate";
    public static final EntryTypeSeperate INSTANCE = new EntryTypeSeperate();

    @Override
    public String deserialise(String source) {
        return source;
    }

    @Override
    public List<String> getTooltip(String value) {
        return Collections.singletonList(value);
    }

    @Override
    public ISimpleDrawable createDrawable(String value) {
        return null;
    }
}

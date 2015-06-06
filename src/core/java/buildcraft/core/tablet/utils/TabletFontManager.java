package buildcraft.core.tablet.utils;

import java.io.InputStream;
import java.util.HashMap;

public class TabletFontManager {
    public static final TabletFontManager INSTANCE = new TabletFontManager();

    public HashMap<String, TabletFont> fonts = new HashMap<String, TabletFont>();

    public TabletFont register(String name, InputStream stream) {
        try {
            fonts.put(name, new TabletFont(stream));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return get(name);
    }

    public TabletFont get(String font) {
        return fonts.get(font);
    }
}

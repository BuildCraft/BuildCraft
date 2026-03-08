/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.font;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public enum FontManager {
    INSTANCE;

    private static final boolean ENABLE_SMOOTH_FONT = Boolean.getBoolean("buildcraft.guide.smooth_font");

    private final Map<String, IFontRenderer> fonts = new HashMap<>();

    public IFontRenderer getOrLoadFont(String name, int size) {
        // if (fonts.containsKey(name)) {
        // return fonts.get(name);
        // }

        // Try to get a Font object

        // Font font = new Font(name, Font.PLAIN, size);
        // GuideFont value = new GuideFont(font);
        // fonts.put(name, value);
        // return value;
        if (ENABLE_SMOOTH_FONT) {
            return new GuideFont(new Font(name, 0, size));
        } else {
            return MinecraftFont.INSTANCE;
        }
    }

    public void registerFont(String name, IFontRenderer font) {
        if (font == null) throw new NullPointerException("font");
        if (fonts.containsKey(name)) {
            throw new IllegalStateException("Cannot register the font \"" + name + "\" twice!");
        }
        fonts.put(name, font);
    }
}

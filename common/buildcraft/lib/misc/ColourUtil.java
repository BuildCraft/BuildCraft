/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): SpecialColourFontRenderer / Forge colour utils deferred
package buildcraft.lib.misc;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;

public class ColourUtil {
    public static final char MINECRAFT_FORMAT_CHAR = '§';
    public static final String COLOUR_SPECIAL_START = "" + MINECRAFT_FORMAT_CHAR;

    public static final Function<Formatting, Formatting> getTextFormatForBlack = f -> f;

    public static String makeReadable(DyeColor colour) {
        if (colour == null) return "none";
        String name = colour.getName();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).replace('_', ' ');
    }

    @Nullable
    public static DyeColor getDyeFromName(String name) {
        for (DyeColor c : DyeColor.values()) {
            if (c.getName().equalsIgnoreCase(name)) return c;
        }
        return null;
    }

    public static Formatting getTextFormatForBlack(Formatting f) {
        return f;
    }

    /** STUB(R.Chen): Forge had a dedicated bright-colour table; approximate with DyeColor's sign colour. */
    public static int getLightHex(DyeColor colour) {
        if (colour == null) return 0xFFFFFF;
        return colour.getSignColor() & 0xFFFFFF;
    }

    @Nullable
    public static DyeColor parseColourOrNull(String name) {
        return getDyeFromName(name);
    }

    public static int swapArgbToAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }
}

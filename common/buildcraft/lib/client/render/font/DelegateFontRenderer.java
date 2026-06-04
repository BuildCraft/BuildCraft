/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): TextRenderer subclassing not supported in Fabric 1.20.1 — delegate via IFontRenderer
package buildcraft.lib.client.render.font;

import java.util.Collections;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.font.TextRenderer;

import buildcraft.lib.client.guide.font.IFontRenderer;

@Environment(EnvType.CLIENT)
public class DelegateFontRenderer implements IFontRenderer {

    public final TextRenderer delegate;

    public DelegateFontRenderer(TextRenderer delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getStringWidth(String text) {
        return delegate.getWidth(text);
    }

    @Override
    public int getFontHeight(String text) {
        return 9;
    }

    @Override
    public int getMaxFontHeight() {
        return 9;
    }

    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        // STUB
        return (int) x;
    }

    @Override
    public int drawString(String text, int x, int y, int colour, boolean shadow, boolean centered, float scale) {
        return drawString(text, (float) x, (float) y, colour, shadow);
    }

    @Override
    public List<String> wrapString(String text, int maxWidth, boolean shadow, float scale) {
        return Collections.emptyList();
    }
}

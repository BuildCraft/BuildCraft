/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): TextRenderer subclassing not supported in Fabric 1.20.1 — colour-code rendering deferred
package buildcraft.lib.client.render.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

@Environment(EnvType.CLIENT)
public class SpecialColourFontRenderer {

    public static final SpecialColourFontRenderer INSTANCE = new SpecialColourFontRenderer();

    private SpecialColourFontRenderer() {}

    public TextRenderer getRealRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        // STUB
        return 0;
    }
}

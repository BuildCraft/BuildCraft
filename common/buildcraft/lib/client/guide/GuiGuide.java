/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): GuiScreen → Screen migration + guide rendering deferred
package buildcraft.lib.client.guide;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.MousePosition;

@Environment(EnvType.CLIENT)
public class GuiGuide extends Screen {

    public static final Identifier ICONS_1 = new Identifier("minecraft", "textures/gui/icons.png");
    public static final Identifier ICONS_2 = new Identifier("buildcraftlib:textures/gui/guide/icons.png");
    public static final Identifier COVER = new Identifier("buildcraftlib:textures/gui/guide/cover.png");
    public static final Identifier LEFT_PAGE = new Identifier("buildcraftlib:textures/gui/guide/left_page.png");
    public static final Identifier RIGHT_PAGE = new Identifier("buildcraftlib:textures/gui/guide/right_page.png");
    public static final Identifier LEFT_PAGE_BACK = new Identifier("buildcraftlib:textures/gui/guide/left_page_back.png");

    public final MinecraftClient mc = MinecraftClient.getInstance();
    public int guiLeft = 0, guiTop = 0, xSize = 0, ySize = 0;

    public GuiGuide() {
        super(Text.literal("BuildCraft Guide"));
    }

    public IFontRenderer getCurrentFont() {
        return null;
    }

    public IGuiArea getBookArea() {
        return null;
    }

    public MousePosition getMousePosition() {
        return null;
    }

    public void navigateTo(GuidePageBase page) {
        // STUB
    }

    public void navigateTo(GuideChapter chapter) {
        // STUB
    }
}

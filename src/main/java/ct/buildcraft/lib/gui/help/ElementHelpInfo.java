/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.help;

import java.util.List;

import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.IGuiElement;
import ct.buildcraft.lib.gui.elem.GuiElementContainerHelp;
import ct.buildcraft.lib.gui.elem.GuiElementText;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.misc.LocaleUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Defines some information used when displaying help text about a specific {@link IGuiElement}. If you want to display
 * help at a particular position, but the target is not an {@link IGuiElement} then you should use
 * {@link DummyHelpElement}. */
public class ElementHelpInfo {
    private static final int HELP_WRAP_WIDTH = 150;

    public final String title;
    public final int colour;
    public final String[] localeKeys;
    public final boolean preTranslated;

    public ElementHelpInfo(String title, int colour, String... localeKeys) {
        this(title, colour, false, localeKeys);
    }

    public ElementHelpInfo(String title, int colour, boolean preTranslated, String... localeKeys) {
        this.title = title;
        this.colour = colour;
        this.localeKeys = localeKeys;
        this.preTranslated = preTranslated;
    }

    public static ElementHelpInfo preTranslated(String title, int colour, String... lines) {
        return new ElementHelpInfo(title, colour, true, lines);
    }

    @OnlyIn(Dist.CLIENT)
    public final HelpPosition target(IGuiArea target) {
        return new HelpPosition(this, target);
    }

    @OnlyIn(Dist.CLIENT)
    public Component getLocalizedTitle() {
        return Component.literal(resolve(title));
    }

    @OnlyIn(Dist.CLIENT)
    public void addGuiElements(GuiElementContainerHelp container) {
        BuildCraftGui gui = container.gui;
        Font font = Minecraft.getInstance().font;
        int y = 20;
        for (String key : localeKeys) {
            if (key == null) {
                y += font.lineHeight + 5;
                continue;
            }

            String localized = resolve(key);
            for (String rawLine : localized.split("\\n", -1)) {
                if (rawLine.isEmpty()) {
                    y += font.lineHeight + 5;
                    continue;
                }
                for (String line : wrap(rawLine, font)) {
                    GuiElementText elemText = new GuiElementText(gui, container.offset(0, y), Component.literal(line), 0);
                    container.add(elemText);
                    y += elemText.getHeight() + 5;
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private String resolve(String keyOrText) {
        if (keyOrText == null) {
            return "";
        }
        if (preTranslated) {
            return keyOrText;
        }
        String localized = LocaleUtil.localize(keyOrText);
        if (!localized.equals(keyOrText)) {
            return localized;
        }
        // Plain English/Russian strings are valid help text too. Only prettify obvious missing translation keys.
        if (looksLikeTranslationKey(keyOrText)) {
            return humanizeMissingKey(keyOrText);
        }
        return keyOrText;
    }

    private static boolean looksLikeTranslationKey(String value) {
        return value.indexOf('.') >= 0 && value.indexOf(' ') < 0;
    }

    private static String humanizeMissingKey(String key) {
        int idx = key.lastIndexOf('.');
        String tail = idx >= 0 ? key.substring(idx + 1) : key;
        if (tail.isEmpty()) {
            return key;
        }
        tail = tail.replace('_', ' ').replace('-', ' ');
        return Character.toUpperCase(tail.charAt(0)) + tail.substring(1);
    }

    private static List<String> wrap(String line, Font font) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        String remaining = line;
        while (font.width(remaining) > HELP_WRAP_WIDTH) {
            String prefix = font.plainSubstrByWidth(remaining, HELP_WRAP_WIDTH);
            int split = prefix.lastIndexOf(' ');
            if (split > 0) {
                prefix = prefix.substring(0, split);
            }
            if (prefix.isEmpty()) {
                break;
            }
            out.add(prefix);
            remaining = remaining.substring(prefix.length()).stripLeading();
        }
        out.add(remaining);
        return out;
    }

    /** Stores an {@link ElementHelpInfo} information, as well as the target area which the help element relates to. */
    @OnlyIn(Dist.CLIENT)
    public static final class HelpPosition {
        public final ElementHelpInfo info;
        public final IGuiArea target;

        private HelpPosition(ElementHelpInfo info, IGuiArea target) {
            this.info = info;
            this.target = target;
        }
    }
}

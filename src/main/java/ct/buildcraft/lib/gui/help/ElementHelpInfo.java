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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Defines some information used when displaying help text about a specific {@link IGuiElement}. If you want to display
 * help at a particular position, but the target is not an {@link IGuiElement} then you should use
 * {@link DummyHelpElement}. */
public class ElementHelpInfo {
    public final String title;
    public final int colour;
    public final String[] localeKeys;

    public ElementHelpInfo(String title, int colour, String... localeKeys) {
        this.title = title;
        this.colour = colour;
        this.localeKeys = localeKeys;
    }

    @OnlyIn(Dist.CLIENT)
    public final HelpPosition target(IGuiArea target) {
        return new HelpPosition(this, target);
    }

    @OnlyIn(Dist.CLIENT)
    public void addGuiElements(GuiElementContainerHelp container) {
        BuildCraftGui gui = container.gui;
        int y = 20;
        for (String key : localeKeys) {
            if (key == null) {
                y += Minecraft.getInstance().font.lineHeight + 5;
                continue;
            }
            Component localized = Component.translatable(key);//LocaleUtil.localize(key);
            List<Component> lines = localized.toFlatList();

            for (Component line : lines) {
                GuiElementText elemText = new GuiElementText(gui, container.offset(0, y), line, 0);
                container.add(elemText);
                y += elemText.getHeight() + 5;
            }
        }
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

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.help;

import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/** Small helpers for adding BuildCraft help-ledger hitboxes to hand-written GUIs. */
public final class GuiHelpUtil {
    private GuiHelpUtil() {}

    public static void add(BuildCraftGui gui, IGuiArea area, String titleKey, int colour, String... textKeys) {
        gui.shownElements.add(new DummyHelpElement(area, new ElementHelpInfo(titleKey, colour, textKeys)));
    }

    public static void addRoot(BuildCraftGui gui, int x, int y, int width, int height, String titleKey, int colour,
        String... textKeys) {
        add(gui, new GuiRectangle(x, y, width, height).offset(gui.rootElement), titleKey, colour, textKeys);
    }

    public static void addSlot(BuildCraftGui gui, int x, int y, String titleKey, int colour, String... textKeys) {
        addRoot(gui, x - 1, y - 1, 18, 18, titleKey, colour, textKeys);
    }

    public static void addSlots(BuildCraftGui gui, int x, int y, int columns, int rows, String titleKey, int colour,
        String... textKeys) {
        addRoot(gui, x - 1, y - 1, columns * 18, rows * 18, titleKey, colour, textKeys);
    }
    public static void addMenuSlots(BuildCraftGui gui, List<? extends Slot> slots, int startInclusive, int endExclusive,
        String titleKey, int colour, String... textKeys) {
        int end = Math.min(endExclusive, slots.size());
        for (int i = Math.max(0, startInclusive); i < end; i++) {
            Slot slot = slots.get(i);
            addSlot(gui, slot.x, slot.y, titleKey, colour, textKeys);
        }
    }

    public static void addMenuSlot(BuildCraftGui gui, List<? extends Slot> slots, int index, String titleKey, int colour,
        String... textKeys) {
        addMenuSlots(gui, slots, index, index + 1, titleKey, colour, textKeys);
    }
}

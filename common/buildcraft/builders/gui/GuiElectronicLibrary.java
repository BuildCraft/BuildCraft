/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.gui;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.GuiButtonDrawable;
import buildcraft.lib.gui.button.IButtonClickEventTrigger;
import buildcraft.lib.gui.button.StandardSpriteButtons;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.builders.container.ContainerElectronicLibrary;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Snapshot.Header;

public class GuiElectronicLibrary extends GuiBC8<ContainerElectronicLibrary> {
    private static final ResourceLocation TEXTURE_BASE =
        new ResourceLocation("buildcraftbuilders:textures/gui/electronic_library.png");
    private static final int SIZE_X = 244, SIZE_Y = 220;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS_DOWN = new GuiIcon(TEXTURE_BASE, 234, 240, 22, 16);
    private static final GuiRectangle RECT_PROGRESS_DOWN = new GuiRectangle(194, 58, 22, 16);
    private static final GuiIcon ICON_PROGRESS_UP = new GuiIcon(TEXTURE_BASE, 234, 224, 22, 16);
    private static final GuiRectangle RECT_PROGRESS_UP = new GuiRectangle(194, 79, 22, 16);

    private final GuiButtonDrawable delButton;

    public GuiElectronicLibrary(ContainerElectronicLibrary container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
        IGuiPosition buttonPos = rootElement.offset(174, 109);
        delButton = new GuiButtonDrawable(this, "del", buttonPos, StandardSpriteButtons.EIGHTH_BUTTON_DRAWABLE);
        delButton.enabled = false;
        delButton.registerListener(this::onDelButtonClick);
        shownElements.add(delButton);
        shownElements.add(delButton.createTextElement(LocaleUtil.localize("gui.del")));
    }

    private void onDelButtonClick(IButtonClickEventTrigger button, int buttonKey) {
        if (container.tile.selected != null) {
            Snapshot snapshot = getSnapshots().getSnapshot(container.tile.selected);
            if (snapshot != null) {
                container.sendSelectedToServer(null);
                getSnapshots().removeSnapshot(snapshot.key);
            }
        }
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
        drawProgress(RECT_PROGRESS_DOWN, ICON_PROGRESS_DOWN, -container.tile.deltaProgressDown.getDynamic(partialTicks), 1);
        drawProgress(RECT_PROGRESS_UP, ICON_PROGRESS_UP, container.tile.deltaProgressUp.getDynamic(partialTicks), 1);
        iterateSnapshots((i, rect, key) -> {
            boolean isSelected = key.equals(container.tile.selected);
            if (isSelected) {
                drawGradientRect(rect, 0xFF_55_55_55, 0xFF_55_55_55);
            }
            int colour = isSelected ? 0xffffa0 : 0xe0e0e0;
            Header header = key.header;
            String text = header == null ? key.toString() : header.name;
            drawString(fontRenderer, text, rect.x, rect.y, colour);
        });
        delButton.enabled = getSnapshots().getSnapshot(container.tile.selected) != null;
    }

    private GlobalSavedDataSnapshots getSnapshots() {
        return GlobalSavedDataSnapshots.get(container.tile.getWorld());
    }

    private void iterateSnapshots(ISnapshotIterator iterator) {
        List<Snapshot.Key> list = getSnapshots().getList();
        GuiRectangle rect = new GuiRectangle(rootElement.getX() + 8, rootElement.getY() + 22, 154, 8);
        for (int i = 0; i < list.size(); i++) {
            iterator.call(i, rect, list.get(i));
            rect = rect.offset(0, 8);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        AtomicBoolean found = new AtomicBoolean(false);
        iterateSnapshots((i, rect, key) -> {
            if (rect.contains(mouse)) {
                container.sendSelectedToServer(key);
                delButton.enabled = true;
                found.set(true);
            }
        });
        if (!found.get()) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @FunctionalInterface
    private interface ISnapshotIterator {
        void call(int snapshotIndex, GuiRectangle rect, Snapshot.Key key);
    }
}

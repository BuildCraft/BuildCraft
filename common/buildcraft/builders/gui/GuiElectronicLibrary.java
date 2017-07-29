/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.gui;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.GuiAbstractButton;
import buildcraft.lib.gui.button.GuiBetterButton;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.builders.container.ContainerElectronicLibrary;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.Snapshot;

public class GuiElectronicLibrary extends GuiBC8<ContainerElectronicLibrary> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftbuilders:textures/gui/electronic_library.png");
    private static final int SIZE_X = 244, SIZE_Y = 220;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS_DOWN = new GuiIcon(TEXTURE_BASE, 234, 240, 22, 16);
    private static final GuiRectangle RECT_PROGRESS_DOWN = new GuiRectangle(194, 58, 22, 16);
    private static final GuiIcon ICON_PROGRESS_UP = new GuiIcon(TEXTURE_BASE, 234, 224, 22, 16);
    private static final GuiRectangle RECT_PROGRESS_UP = new GuiRectangle(194, 79, 22, 16);
    private GuiAbstractButton delButton;

    public GuiElectronicLibrary(ContainerElectronicLibrary container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(
            delButton = new GuiBetterButton(
                this,
                0,
                guiLeft + 174,
                guiTop + 109,
                25,
                LocaleUtil.localize("gui.del")
            )
                .setActive(container.tile.selected != null && getSnapshots().getSnapshot(container.tile.selected) != null)
                .registerListener((button, buttonId, buttonKey) -> {
                    if (container.tile.selected != null) {
                        Snapshot snapshot = getSnapshots().getSnapshot(container.tile.selected);
                        if (snapshot != null) {
                            container.sendSelectedToServer(null);
                            getSnapshots().removeSnapshot(snapshot.key);
                            if (button instanceof GuiAbstractButton) {
                                ((GuiAbstractButton) button).setActive(false);
                            }
                        }
                    }
                })
        );
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
        drawProgress(
            RECT_PROGRESS_DOWN,
            ICON_PROGRESS_DOWN,
            -container.tile.deltaProgressDown.getDynamic(partialTicks),
            1
        );
        drawProgress(
            RECT_PROGRESS_UP,
            ICON_PROGRESS_UP,
            container.tile.deltaProgressUp.getDynamic(partialTicks),
            1
        );
        iterateSnapshots((i, x, y, width, height, key) ->
            drawString(
                fontRenderer,
                Optional.<Object>ofNullable(key.header).orElse(key).toString(),
                x,
                y,
                key.equals(container.tile.selected)
                    ? 0xffffa0
                    : 0xe0e0e0
            )
        );
        delButton.setActive(container.tile.selected != null && getSnapshots().getSnapshot(container.tile.selected) != null);
    }

    private GlobalSavedDataSnapshots getSnapshots() {
        return GlobalSavedDataSnapshots.get(container.tile.getWorld());
    }

    private void iterateSnapshots(ISnapshotIterator iterator) {
        List<Snapshot.Key> list = getSnapshots().getList();
        for (int i = 0; i < list.size(); i++) {
            iterator.call(
                i,
                rootElement.getX() + 8,
                rootElement.getY() + 22 + i * 8,
                154,
                8,
                list.get(i)
            );
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        AtomicBoolean found = new AtomicBoolean(false);
        iterateSnapshots((i, x, y, width, height, key) -> {
            if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
                container.sendSelectedToServer(key);
                delButton.setActive(true);
                found.set(true);
            }
        });
        if (!found.get()) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @FunctionalInterface
    private interface ISnapshotIterator {
        void call(int i, int x, int y, int width, int height, Snapshot.Key key);
    }
}

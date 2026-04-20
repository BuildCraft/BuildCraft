/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.builders.gui;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.builders.menu.ContainerElectronicLibrary;
import ct.buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.builders.snapshot.Snapshot.Header;
import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.button.GuiButtonDrawable;
import ct.buildcraft.lib.gui.button.IButtonClickEventTrigger;
import ct.buildcraft.lib.gui.button.StandardSpriteButtons;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.lib.gui.pos.IGuiPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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

    public GuiElectronicLibrary(ContainerElectronicLibrary container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        IGuiPosition buttonPos = mainGui.rootElement.offset(174, 109);
        delButton = new GuiButtonDrawable(mainGui, "del", buttonPos, StandardSpriteButtons.EIGHTH_BUTTON_DRAWABLE);
        delButton.enabled = false;
        delButton.registerListener(this::onDelButtonClick);
        mainGui.shownElements.add(delButton);
        mainGui.shownElements.add(delButton.createTextElement(Component.translatable("gui.del")));
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
    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
    	RenderSystem._setShaderTexture(0, TEXTURE_BASE);
        ICON_GUI.drawAt(pose, mainGui.rootElement);
        drawProgress(pose, RECT_PROGRESS_DOWN, ICON_PROGRESS_DOWN, -container.tile.deltaProgressDown.getDynamic(partialTicks), 1);
        drawProgress(pose, RECT_PROGRESS_UP, ICON_PROGRESS_UP, container.tile.deltaProgressUp.getDynamic(partialTicks), 1);
        iterateSnapshots((i, rect, key) -> {
            boolean isSelected = key.equals(container.tile.selected);
            if (isSelected) {
                drawGradientRect(pose, rect, 0xFF_55_55_55, 0xFF_55_55_55);
            }
            int colour = isSelected ? 0xffffa0 : 0xe0e0e0;
            Header header = key.header;
            String text = header == null ? key.toString() : header.name;
            drawString(pose, font, text, rect.x, rect.y, colour);
        });
        delButton.enabled = getSnapshots().getSnapshot(container.tile.selected) != null;
    }

    private GlobalSavedDataSnapshots getSnapshots() {
        return GlobalSavedDataSnapshots.get(container.tile.getLevel());
    }

    private void iterateSnapshots(ISnapshotIterator iterator) {
        List<Snapshot.Key> list = getSnapshots().getList();
        GuiRectangle rect = new GuiRectangle(mainGui.rootElement.getX() + 8, mainGui.rootElement.getY() + 22, 154, 8);
        for (int i = 0; i < list.size(); i++) {
            iterator.call(i, rect, list.get(i));
            rect = rect.offset(0, 8);
        }
    }
    

    @Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton){
        AtomicBoolean found = new AtomicBoolean(false);
        iterateSnapshots((i, rect, key) -> {
            if (rect.contains(mainGui.mouse)) {
                container.sendSelectedToServer(key);
                delButton.enabled = true;
                found.set(true);
            }
        });
        if (!found.get()) {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        return true;
    }

    @FunctionalInterface
    private interface ISnapshotIterator {
        void call(int snapshotIndex, GuiRectangle rect, Snapshot.Key key);
    }
}

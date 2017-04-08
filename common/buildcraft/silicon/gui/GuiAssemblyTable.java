/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.silicon.gui;

import java.io.IOException;

import javax.vecmath.Point2i;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.recipes.AssemblyRecipe;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.silicon.EnumAssemblyRecipeState;
import buildcraft.silicon.container.ContainerAssemblyTable;
import buildcraft.transport.gui.LedgerTablePower;

public class GuiAssemblyTable extends GuiBC8<ContainerAssemblyTable> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftsilicon:textures/gui/assembly_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 220;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_SAVED = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 16, 16);
    private static final GuiIcon ICON_SAVED_ENOUGH = new GuiIcon(TEXTURE_BASE, SIZE_X, 16, 16, 16);
    private static final GuiIcon ICON_SAVED_ENOUGH_ACTIVE = new GuiIcon(TEXTURE_BASE, SIZE_X, 32, 16, 16);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 48, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(86, 36, 4, 70);

    public GuiAssemblyTable(ContainerAssemblyTable container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
        ledgersRight.ledgers.add(new LedgerTablePower(ledgersRight, container.tile));
    }

    private Point2i getPos(int index) {
        int posX = index % 3;
        int posY = index / 3;
        return new Point2i(116 + posX * 18, 36 + posY * 18);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);

        long target = container.tile.getTarget();
        if (target != 0) {
            double v = (double) container.tile.power / target;
            ICON_PROGRESS.drawCutInside(new GuiRectangle(RECT_PROGRESS.x, (int) (RECT_PROGRESS.y + RECT_PROGRESS.height * Math.max(1 - v, 0)), RECT_PROGRESS.width, (int) Math.ceil(RECT_PROGRESS.height
                * Math.min(v, 1))).offset(rootElement));
        }
        for (int i = 0; i < container.tile.recipesStates.size(); i++) {
            AssemblyRecipe recipe = container.tile.recipesStates.keySet().toArray(new AssemblyRecipe[0])[i];
            EnumAssemblyRecipeState state = container.tile.recipesStates.values().toArray(new EnumAssemblyRecipeState[0])[i];
            Point2i pos = getPos(i);
            IGuiArea iconArea = rootElement.offset(pos.x, pos.y).resize(16, 16);
            drawItemStackAt(recipe.output, iconArea.getX(), iconArea.getY());
            if (state == EnumAssemblyRecipeState.SAVED) {
                ICON_SAVED.drawAt(iconArea);
            }
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH) {
                ICON_SAVED_ENOUGH.drawAt(iconArea);
            }
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE) {
                ICON_SAVED_ENOUGH_ACTIVE.drawAt(iconArea);
            }
            if (!recipe.output.isEmpty() && iconArea.contains(mouse)) {
                // TODO: Draw stack overlay
            }
        }
    }

    @Override
    protected void drawForegroundLayer() {
        String title = I18n.format("tile.assemblyTableBlock.name");
        fontRenderer.drawString(title, guiLeft + (xSize - fontRenderer.getStringWidth(title)) / 2, guiTop + 15, 0x404040);

        for (int i = 0; i < container.tile.recipesStates.size(); i++) {
            AssemblyRecipe recipe = container.tile.recipesStates.keySet().toArray(new AssemblyRecipe[0])[i];
            EnumAssemblyRecipeState state = container.tile.recipesStates.values().toArray(new EnumAssemblyRecipeState[0])[i];
            Point2i pos = getPos(i);
            IGuiArea iconArea = rootElement.offset(pos.x - 1, pos.y - 1).resize(18, 18);
            if (!recipe.output.isEmpty() && iconArea.contains(mouse)) {
                renderToolTip(recipe.output, mouse.getX(), mouse.getY());
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (int i = 0; i < container.tile.recipesStates.size(); i++) {
            AssemblyRecipe recipe = container.tile.recipesStates.keySet().toArray(new AssemblyRecipe[0])[i];
            EnumAssemblyRecipeState state = container.tile.recipesStates.values().toArray(new EnumAssemblyRecipeState[0])[i];
            Point2i pos = getPos(i);
            int x = rootElement.getX() + pos.getX();
            int y = rootElement.getY() + pos.getY();
            if (mouseX > x && mouseX < x + 16 && mouseY > y && mouseY < y + 16) {
                container.tile.sendRecipeStateToServer(recipe, state == EnumAssemblyRecipeState.POSSIBLE ? EnumAssemblyRecipeState.SAVED : EnumAssemblyRecipeState.POSSIBLE);
            }
        }
    }
}

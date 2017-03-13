/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.gui;

import buildcraft.builders.container.ContainerFillingPlanner;
import buildcraft.builders.filling.Filling;
import buildcraft.builders.filling.IParameter;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.*;
import buildcraft.lib.gui.elem.ToolTip;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class GuiFillingPlanner extends GuiBC8<ContainerFillingPlanner> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftbuilders:textures/gui/filling_planner.png");
    private static final int SIZE_X = 176, SIZE_Y = 42;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    public GuiFillingPlanner(ContainerFillingPlanner container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButtonSmall(this, 0, rootElement.getX() + 7, rootElement.getY() + 38, 100, "Inverted")
                .setToolTip(ToolTip.createLocalized("gui.filling_planner.inverted"))
                .setBehaviour(IButtonBehaviour.TOGGLE)
                .setActive(container.inverted)
                .registerListener((button, buttonId, buttonKey) -> {
                    container.inverted = button.isButtonActive();
                    container.sendDataToServer();
                }));
    }

    private void iterateParameters(IParameterIterator iterator) {
        for (int i = 0; i < container.parameters.size(); i++) {
            IParameter parameter = container.parameters.get(i);
            iterator.call(i, rootElement.getX() + 8 + i * 18, rootElement.getY() + 18, 16, 16, parameter);
        }
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
    }

    @Override
    protected void drawForegroundLayer() {
        iterateParameters((i, x, y, width, height, parameter) -> {
            new GuiIcon(
                    new ResourceLocation(
                            "buildcraftbuilders:textures/filling_planner/" +
                                    parameter.getParameterName() +
                                    "/" +
                                    parameter.getName() +
                                    ".png"),
                    0,
                    0,
                    16,
                    16
            ).drawAt(x, y);
        });
        iterateParameters((i, x, y, width, height, parameter) -> {
            if (mouse.getX() >= x && mouse.getX() < x + width && mouse.getY() >= y && mouse.getY() < y + height) {
                drawHoveringText(Collections.singletonList(parameter.getParameterName() + ": " + parameter.getName()), mouse.getX(), mouse.getY());
            }
        });
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        AtomicBoolean found = new AtomicBoolean(false);
        iterateParameters((i, x, y, width, height, parameter) -> {
            if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
                IParameter[] values = parameter.getClass().getEnumConstants();
                container.parameters.set(i, values[(parameter.getOrdinal() + values.length + (mouseButton == 1 ? -1 : 1)) % values.length]);
                int j = i + 1;
                while (true) {
                    Class<? extends IParameter> nextParameterClass = Filling.INSTANCE.getNextParameterClass(container.parameters.subList(0, Math.min(j, container.parameters.size())));
                    if (j < container.parameters.size()) {
                        if (container.parameters.get(j).getClass().equals(nextParameterClass)) {
                            j++;
                            continue;
                        } else {
                            IntStream.range(j, container.parameters.size()).forEach(k -> container.parameters.remove(container.parameters.size() - 1));
                        }
                    }
                    if (nextParameterClass != null) {
                        container.parameters.add(nextParameterClass.getEnumConstants()[0]);
                        j++;
                    } else {
                        break;
                    }
                }
                container.sendDataToServer();
                found.set(true);
            }
        });
        container.addon.markDirty();
        if (!found.get()) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @FunctionalInterface
    private interface IParameterIterator {
        void call(int i, int x, int y, int width, int height, IParameter parameter);
    }
}

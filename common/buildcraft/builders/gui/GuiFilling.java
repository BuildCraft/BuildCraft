/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.sprite.RawSprite;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.GuiSpriteButton;
import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.elem.ToolTip;

import buildcraft.builders.container.ContainerFiller;
import buildcraft.builders.container.ContainerFillingPlanner;
import buildcraft.builders.container.IContainerFilling;
import buildcraft.builders.filling.Filling;
import buildcraft.builders.filling.IParameter;

public class GuiFilling extends GuiBC8<ContainerBC_Neptune> {
    private static final ResourceLocation FILLING_PLANNER_TEXTURE_BASE =
        new ResourceLocation("buildcraftbuilders:textures/gui/filling_planner.png");
    private static final int FILLING_PLANNER_SIZE_X = 176, FILLING_PLANNER_SIZE_Y = 42;
    private static final GuiIcon FILLING_PLANNER_ICON_GUI = new GuiIcon(
        FILLING_PLANNER_TEXTURE_BASE,
        0,
        0,
        FILLING_PLANNER_SIZE_X,
        FILLING_PLANNER_SIZE_Y
    );
    private static final ResourceLocation FILLER_TEXTURE_BASE =
        new ResourceLocation("buildcraftbuilders:textures/gui/filler.png");
    private static final int FILLER_SIZE_X = 176, FILLER_SIZE_Y = 241;
    private static final GuiIcon FILLER_ICON_GUI = new GuiIcon(
        FILLER_TEXTURE_BASE,
        0,
        0,
        FILLER_SIZE_X,
        FILLER_SIZE_Y
    );
    private final ResourceLocation textureBase;
    private final GuiIcon iconGui;

    public GuiFilling(ContainerBC_Neptune container) {
        super(container);
        if (container instanceof ContainerFillingPlanner) {
            xSize = FILLING_PLANNER_SIZE_X;
            ySize = FILLING_PLANNER_SIZE_Y;
            textureBase = FILLING_PLANNER_TEXTURE_BASE;
            iconGui = FILLING_PLANNER_ICON_GUI;
        } else if (container instanceof ContainerFiller) {
            xSize = FILLER_SIZE_X;
            ySize = FILLER_SIZE_Y;
            textureBase = FILLER_TEXTURE_BASE;
            iconGui = FILLER_ICON_GUI;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        if (container instanceof ContainerFiller) {
            buttonList.add(
                new GuiSpriteButton(
                    this,
                    0,
                    rootElement.getX() + 130,
                    rootElement.getY() + 18,
                    16,
                    16,
                    new GuiIcon(textureBase, 192, 0, 16, 16),
                    new GuiIcon(textureBase, 208, 0, 16, 16),
                    new GuiIcon(textureBase, 192, 16, 16, 16),
                    new GuiIcon(textureBase, 208, 16, 16, 16)
                )
                    .setToolTip(ToolTip.createLocalized("gui.filler.excavate"))
                    .setActive(((IContainerFilling) container).isCanExcavate())
                    .registerListener((button, buttonId, buttonKey) -> {
                        boolean value = !((IContainerFilling) container).isCanExcavate();
                        ((IContainerFilling) container).setCanExcavate(value);
                        ((GuiSpriteButton) button).setActive(value);
                    })
            );
        }
        buttonList.add(
            new GuiSpriteButton(
                this,
                1,
                rootElement.getX() + 152,
                rootElement.getY() + 18,
                16,
                16,
                new GuiIcon(textureBase, 224, 0, 16, 16),
                new GuiIcon(textureBase, 240, 0, 16, 16),
                new GuiIcon(textureBase, 224, 16, 16, 16),
                new GuiIcon(textureBase, 240, 16, 16, 16)
            )
                .setToolTip(ToolTip.createLocalized("gui.filler.inverted"))
                .setActive(((IContainerFilling) container).isInverted())
                .registerListener((button, buttonId, buttonKey) -> {
                    boolean value = !((IContainerFilling) container).isInverted();
                    ((IContainerFilling) container).setInverted(value);
                    ((GuiSpriteButton) button).setActive(value);
                })
        );
    }

    private void iterateParameters(List<IParameter> parameters, IParameterIterator iterator) {
        for (int i = 0; i < parameters.size(); i++) {
            IParameter parameter = parameters.get(i);
            iterator.call(i, rootElement.getX() + 8 + i * 18, rootElement.getY() + 18, 16, 16, parameter);
        }
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        iconGui.drawAt(rootElement);
    }

    @Override
    protected void drawForegroundLayer() {
        GlStateManager.enableAlpha();
        List<IParameter> parameters = ((IContainerFilling) container).getParameters();
        iterateParameters(parameters, (i, x, y, width, height, parameter) ->
            new GuiIcon(
                new RawSprite(
                    new ResourceLocation(
                        "buildcraftbuilders:textures/filling/" +
                            parameter.getParameterName() +
                            "/" +
                            parameter.getName() +
                            ".png"
                    ),
                    0,
                    0,
                    width,
                    height,
                    (width + height) / 2
                ),
                (width + height) / 2
            ).drawAt(x, y)
        );
        iterateParameters(parameters, (i, x, y, width, height, parameter) -> {
            if (mouse.getX() >= x && mouse.getX() < x + width && mouse.getY() >= y && mouse.getY() < y + height) {
                drawHoveringText(
                    Collections.singletonList(parameter.getParameterName() + ": " + parameter.getName()),
                    mouse.getX(),
                    mouse.getY()
                );
            }
        });
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        AtomicBoolean found = new AtomicBoolean(false);
        List<IParameter> parameters = new ArrayList<>(((IContainerFilling) container).getParameters());
        iterateParameters(parameters, (i, x, y, width, height, parameter) -> {
            if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
                IParameter[] values = parameter.getClass().getEnumConstants();
                parameters.set(
                    i,
                    values[(parameter.getOrdinal() + values.length + (mouseButton == 1 ? -1 : 1)) % values.length]
                );
                int j = i + 1;
                while (true) {
                    Class<? extends IParameter> nextParameterClass = Filling.getNextParameterClass(
                        parameters.subList(
                            0,
                            Math.min(j, parameters.size())
                        )
                    );
                    if (j < parameters.size()) {
                        if (parameters.get(j).getClass().equals(nextParameterClass)) {
                            j++;
                            continue;
                        } else {
                            IntStream.range(j, parameters.size())
                                .forEach(k -> parameters.remove(parameters.size() - 1));
                        }
                    }
                    if (nextParameterClass != null) {
                        parameters.add(nextParameterClass.getEnumConstants()[0]);
                        j++;
                    } else {
                        break;
                    }
                }
                ((IContainerFilling) container).setParameters(parameters);
                found.set(true);
            }
        });
        if (!found.get()) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @FunctionalInterface
    private interface IParameterIterator {
        void call(int i, int x, int y, int width, int height, IParameter parameter);
    }
}

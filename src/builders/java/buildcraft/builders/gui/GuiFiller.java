/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.builders.tile.TileFiller;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.lib.gui.AdvancedSlot;
import buildcraft.core.lib.gui.GuiAdvancedInterface;
import buildcraft.core.lib.gui.GuiTools;
import buildcraft.core.lib.gui.StatementParameterSlot;
import buildcraft.core.lib.gui.StatementSlot;
import buildcraft.core.lib.gui.buttons.GuiBetterButton;
import buildcraft.core.lib.gui.buttons.StandardButtonTextureSets;
import buildcraft.core.lib.utils.StringUtils;

public class GuiFiller extends GuiAdvancedInterface {
    class FillerParameterSlot extends StatementParameterSlot {
        public FillerParameterSlot(int x, int y, int slot) {
            super(instance, x, y, slot, fakeStatementSlot);
        }

        @Override
        public IStatementParameter getParameter() {
            if (slot >= instance.filler.patternParameters.length) {
                return null;
            } else {
                return instance.filler.patternParameters[slot];
            }
        }

        @Override
        public void setParameter(IStatementParameter param, boolean notifyServer) {
            // TODO
        }
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftbuilders:textures/gui/filler.png");
    private final IInventory playerInventory;
    private final TileFiller filler;
    private final GuiFiller instance;
    private final StatementSlot fakeStatementSlot;

    public GuiFiller(IInventory playerInventory, TileFiller filler) {
        super(new ContainerFiller(playerInventory, filler), filler, TEXTURE);
        this.playerInventory = playerInventory;
        this.filler = filler;
        this.instance = this;
        this.fakeStatementSlot = new StatementSlot(instance, -1, -1, 0) {
            @Override
            public IStatement getStatement() {
                return instance.filler.currentPattern;
            }
        };
        xSize = 175;
        ySize = 240;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();

        buttonList.add(new GuiBetterButton(0, guiLeft + 38 - 18, guiTop + 30, 10, StandardButtonTextureSets.LEFT_BUTTON, ""));
        buttonList.add(new GuiBetterButton(1, guiLeft + 38 + 16 + 8, guiTop + 30, 10, StandardButtonTextureSets.RIGHT_BUTTON, ""));

        slots.clear();
        for (int i = 0; i < 4; i++) {
            slots.add(new FillerParameterSlot(77 + (i * 18), 30, i));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == 0) {
            filler.currentPattern = (FillerPattern) FillerManager.registry.getPreviousPattern(filler.currentPattern);
        } else if (button.id == 1) {
            filler.currentPattern = (FillerPattern) FillerManager.registry.getNextPattern(filler.currentPattern);
        }

        filler.rpcSetPatternFromString(filler.currentPattern.getUniqueTag());
    }

    @Override
    protected void mouseClicked(int x, int y, int k) throws IOException {
        super.mouseClicked(x, y, k);

        AdvancedSlot slot = getSlotAtLocation(x, y);

        if (slot != null) {
            int i = ((FillerParameterSlot) slot).slot;
            if (i < filler.patternParameters.length) {
                if (filler.patternParameters[i] != null) {
                    filler.patternParameters[i].onClick(filler, filler.currentPattern, mc.thePlayer.inventory.getItemStack(), new StatementMouseClick(
                            k, isShiftKeyDown()));
                } else {
                    filler.patternParameters[i] = filler.currentPattern.createParameter(i);
                }
                filler.rpcSetParameter(i, filler.patternParameters[i]);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mx, int my) {
        super.drawGuiContainerBackgroundLayer(f, mx, my);
        drawBackgroundSlots();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        String title = StringUtils.localize("tile.fillerBlock.name");
        fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
        fontRendererObj.drawString(StringUtils.localize("gui.filling.resources"), 8, 74, 0x404040);
        fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, 142, 0x404040);
        GuiTools.drawCenteredString(fontRendererObj, filler.currentPattern.getDescription(), 56);
    }
}

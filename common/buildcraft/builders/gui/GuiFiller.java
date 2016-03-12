/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import buildcraft.BuildCraftCore;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.api.tiles.IControllable.Mode;
import buildcraft.builders.TileFiller;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.client.CoreIconProvider;
import buildcraft.core.lib.config.DetailedConfigOption;
import buildcraft.core.lib.gui.*;
import buildcraft.core.lib.gui.buttons.ButtonTextureSet;
import buildcraft.core.lib.gui.buttons.GuiBetterButton;
import buildcraft.core.lib.gui.buttons.IButtonTextureSet;
import buildcraft.core.lib.gui.buttons.StandardButtonTextureSets;
import buildcraft.core.lib.gui.tooltips.ToolTip;
import buildcraft.core.lib.gui.tooltips.ToolTipLine;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.BCStringUtils;

import io.netty.buffer.ByteBuf;

public class GuiFiller extends GuiAdvancedInterface {
    class FillerParameterSlot extends StatementParameterSlot {
        public FillerParameterSlot(int x, int y, int slot) {
            super(instance, x, y, slot, fakeStatementSlot);
        }

        @Override
        public IStatementParameter getParameter() {
            if (instance.filler.patternParameters == null || slot >= instance.filler.patternParameters.length) {
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

    /** Option for controlling the order of the mode icons. Default is mode then lock, but changing it to false will
     * make it lock then mode */
    private static final DetailedConfigOption OPTION_EXTRA_MODE_FIRST = new DetailedConfigOption("filler.gui.extra.modefirst", "true");

    // These 2 options should probably be moved to a json file that can be changed with a resource pack
    /** Options for controlling the position of extra icons are laid out */
    private static final DetailedConfigOption OPTION_EXTRA_X_POS = new DetailedConfigOption("filler.gui.extra.x", "38");
    private static final DetailedConfigOption OPTION_EXTRA_Y_POS = new DetailedConfigOption("filler.gui.extra.y", "9");

    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftbuilders:textures/gui/filler.png");
    private static final IButtonTextureSet EXCAVATE_OFF = new ButtonTextureSet(240, -16, 16, 16, TEXTURE);
    private static final IButtonTextureSet EXCAVATE_ON = new ButtonTextureSet(224, -16, 16, 16, TEXTURE);
    private final TileFiller filler;
    private final GuiFiller instance;
    private final StatementSlot fakeStatementSlot;

    public GuiFiller(EntityPlayer player, TileFiller filler) {
        super(new ContainerFiller(player, filler), filler, TEXTURE);
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

    private IButtonTextureSet getExcavateTexture() {
        return filler.isExcavate() ? EXCAVATE_ON : EXCAVATE_OFF;
    }

    private GuiBetterButton getExcavateButton() {
        return new GuiBetterButton(2, guiLeft + 150, guiTop + 30, 16, getExcavateTexture(), "").setToolTip(new ToolTip(500, new ToolTipLine(
                StatCollector.translateToLocal("tip.filler.excavate." + (filler.isExcavate() ? "on" : "off")))));
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();

        buttonList.add(new GuiBetterButton(0, guiLeft + 38 - 18, guiTop + 30, 10, StandardButtonTextureSets.LEFT_BUTTON, ""));
        buttonList.add(new GuiBetterButton(1, guiLeft + 38 + 16 + 8, guiTop + 30, 10, StandardButtonTextureSets.RIGHT_BUTTON, ""));
        buttonList.add(getExcavateButton());

        slots.clear();
        for (int i = 0; i < 4; i++) {
            slots.add(new FillerParameterSlot(77 + (i * 18), 30, i));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == 0 && !filler.isPatternLocked()) {
            filler.currentPattern = (FillerPattern) FillerManager.registry.getPreviousPattern(filler.currentPattern);
        } else if (button.id == 1 && !filler.isPatternLocked()) {
            filler.currentPattern = (FillerPattern) FillerManager.registry.getNextPattern(filler.currentPattern);
        } else if (button.id == 2) {
            filler.setExcavate(!filler.isExcavate());

            buttonList.set(2, getExcavateButton());

            BuildCraftCore.instance.sendToServer(new PacketCommand(filler, "setFlags", new CommandWriter() {
                @Override
                public void write(ByteBuf data) {
                    data.writeBoolean(filler.isExcavate());
                }
            }));
        }

        filler.rpcSetPatternFromString(filler.currentPattern.getUniqueTag());
    }

    @Override
    protected void mouseClicked(int x, int y, int k) throws IOException {
        super.mouseClicked(x, y, k);

        AdvancedSlot slot = getSlotAtLocation(x, y);

        if (slot != null && !filler.isPatternLocked()) {
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
        drawBackgroundSlots(mx, my);

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        CoreIconProvider[] sprites = new CoreIconProvider[2];
        int count = 0;

        CoreIconProvider sprite = CoreIconProvider.getForControlMode(filler.getControlMode());
        if (filler.getControlMode() != Mode.On && sprite != null) sprites[count++] = sprite;
        if (filler.isPatternLocked()) sprites[count++] = CoreIconProvider.LOCK;

        int xPos = OPTION_EXTRA_X_POS.getAsInt();
        int yPos = OPTION_EXTRA_Y_POS.getAsInt();

        if (count == 1) {
            CoreIconProvider first = sprites[0] == null ? sprites[1] : sprites[0];
            drawTexturedModalRect(guiLeft + xPos, guiTop + yPos, first.getSprite(), 16, 16);
        } else if (count == 2) {
            if (!OPTION_EXTRA_MODE_FIRST.getAsBoolean()) {
                sprite = sprites[0];
                sprites[0] = sprites[1];
                sprites[1] = sprite;
            }

            drawTexturedModalRect(guiLeft + xPos - 8, guiTop + yPos, sprites[0].getSprite(), 16, 16);
            drawTexturedModalRect(guiLeft + xPos + 8, guiTop + yPos, sprites[1].getSprite(), 16, 16);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        super.drawGuiContainerForegroundLayer(mx, my);

        String title = BCStringUtils.localize("tile.fillerBlock.name");
        fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
        fontRendererObj.drawString(BCStringUtils.localize("gui.filling.resources"), 8, 74, 0x404040);
        fontRendererObj.drawString(BCStringUtils.localize("gui.inventory"), 8, 142, 0x404040);
        GuiTools.drawCenteredString(fontRendererObj, filler.currentPattern.getDescription(), 56);

        drawTooltipForSlotAt(mx, my);
    }
}

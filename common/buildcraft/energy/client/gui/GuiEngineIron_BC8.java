package buildcraft.energy.client.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.energy.container.ContainerEngineIron_BC8;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.misc.LocaleUtil;

public class GuiEngineIron_BC8 extends GuiBC8<ContainerEngineIron_BC8> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftenergy:textures/gui/combustion_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 177;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE_BASE, 176, 0, 16, 58);

    public GuiEngineIron_BC8(ContainerEngineIron_BC8 container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
        ledgersRight.ledgers.add(new LedgerEngine(ledgersRight, this));
    }

    @Override
    public void initGui() {
        super.initGui();

        guiElements.add(container.widgetTankFuel.createGuiElement(this, rootElement, new GuiRectangle(26, 19, 16, 58), ICON_TANK_OVERLAY));
        guiElements.add(container.widgetTankCoolant.createGuiElement(this, rootElement, new GuiRectangle(80, 19, 16, 58), ICON_TANK_OVERLAY));
        guiElements.add(container.widgetTankResidue.createGuiElement(this, rootElement, new GuiRectangle(134, 19, 16, 58), ICON_TANK_OVERLAY));
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
    }

    @Override
    protected void drawForegroundLayer() {
        String str = LocaleUtil.localize("tile.engineIron.name");
        int strWidth = fontRendererObj.getStringWidth(str);
        fontRendererObj.drawString(str, rootElement.getCenterX() - strWidth / 2, rootElement.getY() + 6, 0x404040);
        fontRendererObj.drawString(LocaleUtil.localize("gui.inventory"), rootElement.getX() + 8, rootElement.getY() + SIZE_Y - 96, 0x404040);
    }
}

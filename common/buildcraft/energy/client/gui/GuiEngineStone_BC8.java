package buildcraft.energy.client.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.pos.IPositionedElement;

public class GuiEngineStone_BC8 extends GuiBC8<ContainerEngineStone_BC8> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftenergy:textures/gui/steam_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_FLAME = new GuiIcon(TEXTURE_BASE, 176, 0, 14, 14);

    private final IPositionedElement flameRect = new GuiRectangle(81, 25, 14, 14).offset(rootElement);

    public GuiEngineStone_BC8(ContainerEngineStone_BC8 container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);

        double amount = container.tile.deltaFuelLeft.getDynamic(partialTicks) / 100;

        if (amount > 0) {
            int flameHeight = (int) (amount * flameRect.getHeight());
            ICON_FLAME.drawCutInside(flameRect.getX(), flameRect.getY() + flameHeight - flameRect.getHeight(), flameRect.getWidth(), flameHeight);
        }
    }
}

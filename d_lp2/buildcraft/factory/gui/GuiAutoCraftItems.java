package buildcraft.factory.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;

public class GuiAutoCraftItems extends GuiBC8<ContainerAutoCraftItems> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftfactory:textures/gui/autobench_item.png");
    private static final int SIZE_X = 176, SIZE_Y = 197;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 23, 10);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(90, 47, 23, 10);

    public GuiAutoCraftItems(ContainerAutoCraftItems container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);

        DeltaInt delta = container.tile.deltaProgress;
        double dynamic = delta.getDynamic(partialTicks);
        double p = dynamic / 100;

        GuiRectangle progress = RECT_PROGRESS.createProgress(p, 1);
        ICON_PROGRESS.drawCutInside(progress.offset(rootElement));

        double dyI = dynamic;
        fontRendererObj.drawString("Start = " + delta.getStatic(true), 10, 10, -1);
        fontRendererObj.drawString("Dyn   = " + dyI, 10, 20, -1);
        fontRendererObj.drawString("End   = " + delta.getStatic(false), 10, 30, -1);
        fontRendererObj.drawString("Count = " + delta.changingEntries.size(), 10, 40, -1);
    }
}

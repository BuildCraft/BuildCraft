package buildcraft.builders.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.builders.container.ContainerBuilder;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;

public class GuiBuilder extends GuiBC8<ContainerBuilder> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftbuilders:textures/gui/builder.png");
    private static final int SIZE_X = 176, SIZE_Y = 222;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    public GuiBuilder(ContainerBuilder container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
    }
}

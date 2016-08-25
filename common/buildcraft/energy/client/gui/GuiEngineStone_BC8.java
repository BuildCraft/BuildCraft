package buildcraft.energy.client.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.lib.gui.GuiBC8;

public class GuiEngineStone_BC8 extends GuiBC8<ContainerEngineStone_BC8> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftenergy:textures/gui/steam_engine_gui.png");

    public GuiEngineStone_BC8(ContainerEngineStone_BC8 container) {
        super(container);
    }

}

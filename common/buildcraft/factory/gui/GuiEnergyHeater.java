package buildcraft.factory.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.lib.gui.GuiAdvancedInterface;
import buildcraft.factory.tile.TileEnergyHeater;

public class GuiEnergyHeater extends GuiAdvancedInterface {
    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftfactory:textures/gui/energy_heater.png");

    private final TileEnergyHeater heater;
    private int inTicks, outTicks, energyTicks, craftTicks;

    public GuiEnergyHeater(EntityPlayer player, TileEnergyHeater heater) {
        super(new ContainerEnergyHeater(player, heater), heater, TEXTURE);
        this.heater = heater;
        xSize = 176;
        ySize = 144;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        inTicks--;
        outTicks--;
        energyTicks--;
        craftTicks--;
        boolean crafted = heater.hasCraftedRecently();
        if (crafted) craftTicks = 20;
        if ((heater.getInputFluid() != null && heater.getInputFluid().amount > 0) || crafted) inTicks = 20;
        if ((heater.getOutputFluid() != null && heater.getOutputFluid().amount > 0) || crafted) outTicks = 20;
        if (heater.hasEnergy()) energyTicks = 20;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        if (heater != null) {

            drawFluid(heater.getInputFluid(), guiLeft + 44, guiTop + 12, 16, 38, 1000);
            drawFluid(heater.getOutputFluid(), guiLeft + 116, guiTop + 12, 16, 38, 1000);

            mc.renderEngine.bindTexture(TEXTURE);

            int state = 0;
            if (craftTicks > 0) {
                state = 0b1000;
            } else {
                if (energyTicks > 0) state |= 0b001;
                if (inTicks > 0) state |= 0b100;
                if (outTicks > 0) state |= 0b010;
            }
            int y = state * 19;

            drawTexturedModalRect(guiLeft + 61, guiTop + 20, 176, y, 54, 19);
        }
    }
}

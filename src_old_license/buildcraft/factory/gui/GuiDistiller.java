package buildcraft.factory.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.lib.gui.GuiAdvancedInterface;
import buildcraft.factory.tile.TileDistiller;

public class GuiDistiller extends GuiAdvancedInterface {

    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftfactory:textures/gui/distiller.png");

    private final TileDistiller distiller;
    private int inTicks, outGasTicks, outLiquidTicks, craftTicks;

    public GuiDistiller(EntityPlayer player, TileDistiller distiller) {
        super(new ContainerDistiller(player, distiller), distiller, TEXTURE);
        this.distiller = distiller;
        xSize = 176;
        ySize = 161;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        inTicks--;
        outGasTicks--;
        outLiquidTicks--;
        craftTicks--;
        boolean crafted = distiller.hasCraftedRecently();
        if (crafted) craftTicks = 20;
        if ((distiller.getInputTank().getFluidAmount() > 0) || crafted) inTicks = 20;
        if ((distiller.getOutputTankGas().getFluidAmount() > 0) || crafted) outGasTicks = 20;
        if ((distiller.getOutputTankLiquid().getFluidAmount() > 0) || crafted) outLiquidTicks = 20;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
        if (distiller != null) {

            mc.renderEngine.bindTexture(TEXTURE);

            if (craftTicks > 0) {
                drawTexturedModalRect(guiLeft + 61, guiTop + 12, 212, 0, 36, 57);
            } else {
                if (inTicks > 0) {
                    drawTexturedModalRect(guiLeft + 61, guiTop + 35, 176, 23, 12, 11);
                }
                if (outGasTicks > 0) {
                    drawTexturedModalRect(guiLeft + 89, guiTop + 13, 204, 1, 8, 11);
                }
                if (outLiquidTicks > 0) {
                    drawTexturedModalRect(guiLeft + 89, guiTop + 57, 204, 45, 8, 11);
                }
            }
        }
    }
}

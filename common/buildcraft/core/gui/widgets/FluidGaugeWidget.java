package buildcraft.core.gui.widgets;


import buildcraft.core.fluids.Tank;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.gui.tooltips.ToolTip;
import buildcraft.core.render.FluidRenderer;
import net.minecraft.util.Icon;
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class FluidGaugeWidget extends Widget {

    public final Tank tank;

    public FluidGaugeWidget(Tank tank, int x, int y, int u, int v, int w, int h) {
        super(x, y, u, v, w, h);
        this.tank = tank;
    }

    @Override
    public ToolTip getToolTip() {
        return tank.getToolTip();
    }

    @Override
    public void draw(GuiBuildCraft gui, int guiX, int guiY, int mouseX, int mouseY) {
        if (tank == null)
            return;
        FluidStack fluidStack = tank.getFluid();
        if (fluidStack == null || fluidStack.amount <= 0 || fluidStack.getFluid() == null)
            return;

        Icon liquidIcon = FluidRenderer.getFluidTexture(fluidStack, false);

        if (liquidIcon == null)
            return;

        float scale = Math.min(fluidStack.amount, tank.getCapacity()) / (float) tank.getCapacity();

        gui.bindTexture(FluidRenderer.getFluidSheet(fluidStack));

        for (int col = 0; col < w / 16; col++) {
            for (int row = 0; row <= h / 16; row++) {
                gui.drawTexturedModelRectFromIcon(guiX + x + col * 16, guiY + y + row * 16 - 1, liquidIcon, 16, 16);
            }
        }

        gui.bindTexture(gui.texture);

        gui.drawTexturedModalRect(guiX + x, guiY + y - 1, x, y - 1, w, h - (int) Math.floor(h * scale) + 1);
        gui.drawTexturedModalRect(guiX + x, guiY + y, u, v, w, h);
    }

}

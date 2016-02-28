/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.gui.widgets;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.fluids.Tank;
import buildcraft.core.lib.gui.GuiBuildCraft;
import buildcraft.core.lib.gui.tooltips.ToolTip;

public class FluidGaugeWidget extends Widget {
    public final Tank tank;
    private boolean overlay;
    private int overlayX, overlayY;

    public FluidGaugeWidget(Tank tank, int x, int y, int w, int h) {
        super(x, y, 0, 0, w, h);
        this.tank = tank;
    }

    public FluidGaugeWidget withOverlay(int x, int y) {
        overlay = true;
        overlayX = x;
        overlayY = y;
        return this;
    }

    @Override
    public ToolTip getToolTip() {
        return tank.getToolTip();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void draw(GuiBuildCraft gui, int guiX, int guiY, int mouseX, int mouseY) {
        if (tank == null) {
            return;
        }
        FluidStack fluidStack = tank.getFluid();
        if (fluidStack != null && fluidStack.amount > 0) {
            gui.drawFluid(fluidStack, guiX + x, guiY + y, w, h, tank.getCapacity());
        }

        gui.bindTexture(gui.texture);

        if (overlay) {
            gui.drawTexturedModalRect(guiX + x, guiY + y, overlayX, overlayY, w, h);
        }
    }
}

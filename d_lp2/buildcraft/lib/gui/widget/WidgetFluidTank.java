package buildcraft.lib.gui.widget;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.fluids.Tank;
import buildcraft.core.lib.gui.tooltips.ToolTip;
import buildcraft.lib.gui.*;

public class WidgetFluidTank extends Widget_BC8<ContainerBC8> {
    private static final byte NET_CLICK = 0;

    private final Tank tank;

    public WidgetFluidTank(ContainerBC8 container, Tank tank) {
        super(container);
        this.tank = tank;
    }

    @Override
    public void handleWidgetDataServer(PacketBuffer buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_CLICK) {
            // TODO: Item interaction
        }
    }

    @SideOnly(Side.CLIENT)
    public IGuiElement createGuiElement(GuiBC8<ContainerBC8> gui, IPositionedElement parent, GuiRectangle position, GuiIcon overlay) {
        return new GuiElementFluidTank(gui, parent, position, overlay);
    }

    private final class GuiElementFluidTank extends GuiElementSimple<GuiBC8<ContainerBC8>, ContainerBC8> {
        private final GuiIcon overlay;

        public GuiElementFluidTank(GuiBC8<ContainerBC8> gui, IPositionedElement parent, GuiRectangle position, GuiIcon overlay) {
            super(gui, parent, position);
            this.overlay = overlay;
        }

        @Override
        public void drawBackground() {
            // TODO!
        }

        @Override
        public void onMouseClicked(int button) {
            if (contains(gui.mouse)) {
                WidgetFluidTank.this.sendWidgetData(buffer -> {
                    buffer.writeByte(NET_CLICK);
                });
            }
        }

        @Override
        public ToolTip getToolTip() {
            if (contains(gui.mouse)) {
                return tank.getToolTip();
            }
            return null;
        }
    }
}

package buildcraft.lib.gui.widget;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.gui.tooltips.ToolTip;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.gui.*;

public class WidgetFluidTank extends Widget_Neptune<ContainerBC_Neptune> {
    private static final byte NET_CLICK = 0;

    private final Tank tank;

    public WidgetFluidTank(ContainerBC_Neptune container, Tank tank) {
        super(container);
        this.tank = tank;
    }

    @Override
    public IMessage handleWidgetDataServer(PacketBuffer buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_CLICK) {
            // TODO: Item interaction
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public IGuiElement createGuiElement(GuiBC8<ContainerBC_Neptune> gui, IPositionedElement parent, GuiRectangle position, GuiIcon overlay) {
        return new GuiElementFluidTank(gui, parent, position, overlay);
    }

    private final class GuiElementFluidTank extends GuiElementSimple<GuiBC8<ContainerBC_Neptune>, ContainerBC_Neptune> {
        private final GuiIcon overlay;

        public GuiElementFluidTank(GuiBC8<ContainerBC_Neptune> gui, IPositionedElement parent, GuiRectangle position, GuiIcon overlay) {
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

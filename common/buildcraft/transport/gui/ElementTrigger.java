package buildcraft.transport.gui;

import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.transport.gate.TriggerWrapper;

public class ElementTrigger extends ElementStatement<TriggerWrapper> {

    public ElementTrigger(GuiGate gui, IGuiPosition parent, GuiRectangle rectangle, TriggerWrapper[] values, int index) {
        super(gui, parent, rectangle, values, index);
    }

    @Override
    protected TriggerWrapper[] getPossible() {
        TriggerWrapper value = values[index];
        return value == null ? null : value.getPossible();
    }
}

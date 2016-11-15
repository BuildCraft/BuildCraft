package buildcraft.transport.gui;

import buildcraft.lib.gui.pos.IPositionedElement;
import buildcraft.lib.misc.data.IReference;
import buildcraft.transport.gate.ActionWrapper;

public class ElementAction extends ElementStatement<ActionWrapper> {

    public ElementAction(GuiGate gui, IPositionedElement element, IReference<ActionWrapper> reference) {
        super(gui, element, reference);
    }

    @Override
    protected ActionWrapper[] getPossible() {
        ActionWrapper value = reference.get();
        if (value == null) return null;
        ActionWrapper[] possible = value.getPossible();
        ActionWrapper[] realPossible = new ActionWrapper[possible.length + 1];
        System.arraycopy(possible, 0, realPossible, 1, possible.length);
        realPossible[0] = null;
        return realPossible;
    }
}

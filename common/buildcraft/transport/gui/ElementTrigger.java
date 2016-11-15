package buildcraft.transport.gui;

import buildcraft.lib.gui.pos.IPositionedElement;
import buildcraft.lib.misc.data.IReference;
import buildcraft.transport.gate.TriggerWrapper;

public class ElementTrigger extends ElementStatement<TriggerWrapper> {

    public ElementTrigger(GuiGate gui, IPositionedElement element, IReference<TriggerWrapper> reference) {
        super(gui, element, reference);
    }

    @Override
    protected TriggerWrapper[] getPossible() {
        TriggerWrapper value = reference.get();
        if (value == null) return null;
        TriggerWrapper[] possible = value.getPossible();
        TriggerWrapper[] realPossible = new TriggerWrapper[possible.length + 1];
        System.arraycopy(possible, 0, realPossible, 1, possible.length);
        realPossible[0] = null;
        return realPossible;
    }
}

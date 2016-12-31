package buildcraft.transport.gui;

import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.data.IReference;
import buildcraft.transport.gate.TriggerWrapper;

public class ElementTrigger extends ElementStatement<TriggerWrapper> {

    public ElementTrigger(GuiGate gui, IGuiArea element, IReference<TriggerWrapper> reference) {
        super(gui, element, reference);
    }

    @Override
    protected TriggerWrapper[] getPossible() {
        TriggerWrapper value = reference.get();
        if (value == null) return null;
        TriggerWrapper[] possible = value.getPossible();
        if (possible == null) return null;

        List<TriggerWrapper> list = new ArrayList<>();
        list.add(null);
        for (TriggerWrapper poss : possible) {
            if (gui.container.possibleTriggers.contains(poss)) {
                list.add(poss);
            }
        }
        return list.toArray(new TriggerWrapper[list.size()]);
    }
}

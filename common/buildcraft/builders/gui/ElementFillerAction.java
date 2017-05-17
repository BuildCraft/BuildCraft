package buildcraft.builders.gui;

import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.statement.ElementStatement;
import buildcraft.lib.misc.data.IReference;

public class ElementFillerAction extends ElementStatement<GuiFiller, FillerWrapper> {

    public ElementFillerAction(GuiFiller gui, IGuiArea element, IReference<FillerWrapper> reference) {
        super(gui, element, reference);
    }

    @Override
    protected FillerWrapper[] getPossible() {
        FillerWrapper value = reference.get();
        if (value == null) {
            return null;
        }
        FillerWrapper[] possible = value.getPossible();
        if (possible == null) {
            return null;
        }
        List<FillerWrapper> list = new ArrayList<>(possible.length);
        list.remove(value.delegate);
        return list.toArray(new FillerWrapper[list.size()]);
    }
}

package buildcraft.lib.gui.elem;

import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.IContainingElement;
import buildcraft.lib.gui.IGuiElement;

public abstract class GuiElementContainer2 implements IContainingElement {

    public final GuiBC8<?> gui;
    private final List<IGuiElement> children = new ArrayList<>();

    public GuiElementContainer2(GuiBC8<?> gui) {
        this.gui = gui;
    }

    @Override
    public List<IGuiElement> getChildElements() {
        return children;
    }
}

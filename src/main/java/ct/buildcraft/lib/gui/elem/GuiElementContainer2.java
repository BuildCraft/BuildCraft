package ct.buildcraft.lib.gui.elem;

import java.util.ArrayList;
import java.util.List;

import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.IContainingElement;
import ct.buildcraft.lib.gui.IGuiElement;

public abstract class GuiElementContainer2 implements IContainingElement {

    public final BuildCraftGui gui;
    private final List<IGuiElement> children = new ArrayList<>();

    public GuiElementContainer2(BuildCraftGui gui) {
        this.gui = gui;
    }

    @Override
    public List<IGuiElement> getChildElements() {
        return children;
    }
}

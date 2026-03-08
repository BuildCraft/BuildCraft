package buildcraft.lib.gui.elem;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.IContainingElement;
import buildcraft.lib.gui.IGuiElement;

import java.util.ArrayList;
import java.util.List;

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

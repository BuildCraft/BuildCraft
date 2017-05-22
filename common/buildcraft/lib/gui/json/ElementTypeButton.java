package buildcraft.lib.gui.json;

import java.util.Map;
import java.util.function.Supplier;

import buildcraft.lib.gui.ContainerBC_Neptune;

public class ElementTypeButton extends ElementType<GuiJson<ContainerBC_Neptune>, ContainerBC_Neptune> {
    public static final String NAME = "buildcraftlib:button";
    public static final ElementTypeButton INSTANCE = new ElementTypeButton();

    // Args:
    // - size[0], size[1]: The physical size of the button
    // - pos[0], pos[1]: The position of the button
    // - modes.enabled[0-1]: The texture u, v of the "Enabled" button state
    // - modes.disabled[0-1]: The texture u, v of the "Disabled" button state

    private ElementTypeButton() {
        super(NAME);
    }

    @Override
    public void addToGui(GuiJson<ContainerBC_Neptune> gui, JsonGuiInfo info, JsonGuiElement json, Map<String, Supplier<String>> guiProps) {
        // TODO: implement this from filler.json, the above (partial) spec, and ElemtTypeSprite
    }
}

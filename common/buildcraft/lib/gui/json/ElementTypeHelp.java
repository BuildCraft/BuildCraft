package buildcraft.lib.gui.json;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

import java.util.ArrayList;
import java.util.List;

public class ElementTypeHelp extends ElementType {
    public static final String NAME = "buildcraftlib:help";
    public static final ElementTypeHelp INSTANCE = new ElementTypeHelp();

    private ElementTypeHelp() {
        super(NAME);
    }

    // Args:
    // - pos[0], pos[1]: the area for help (where it will be drawn, relative to the root of the gui). Defaults
    // to 0,0
    // - size[0], size[1]: the size of the help area
    // - area[0-3]: mapping for pos[0], pos[1], size[0], size[1]
    // - colour: The colour for the help element (overlay)
    // - title: The name of the help element

    @Override
    public IGuiElement deserialize0(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(json);

        String title = json.properties.get("title");

        List<String> text = new ArrayList<>();
        if (json.properties.containsKey("text[0]")) {
            int i = 0;
            while (true) {
                String prop = json.properties.get("text[" + i + "]");
                if (prop == null) {
                    break;
                }
                text.add(prop);
                i++;
            }
        } else {
            text.add(json.properties.getOrDefault("text", "ERROR: Help not given!"));
        }
        int colour = resolveEquationInt(json, "colour", ctx);
        ElementHelpInfo help = new ElementHelpInfo(title, colour, text.toArray(new String[0]));

        inheritProperty(json, "pos[0]", "area[0]");
        inheritProperty(json, "pos[1]", "area[1]");
        inheritProperty(json, "size[0]", "area[2]");
        inheritProperty(json, "size[1]", "area[3]");

        IGuiArea area = resolveArea(json, "area", parent, ctx);
        return new DummyHelpElement(area, help);
    }
}

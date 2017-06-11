package buildcraft.lib.gui.json;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.misc.LocaleUtil;

public class ElementTypeText extends ElementType {
    public static final String NAME = "buildcraftlib:text";
    public static final ElementTypeText INSTANCE = new ElementTypeText();

    private ElementTypeText() {
        super(NAME);
    }

    // pos: the position of the text
    // text: The text to be drawn. Will be localised first, and used as a fallback
    // colour: Default colour to be drawn
    // centered: If true then the text will be centered around pos

    @Override
    public void addToGui(GuiJson<?> gui, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(json);

        int posX = resolveEquationInt(json, "pos[0]", ctx);
        int posY = resolveEquationInt(json, "pos[1]", ctx);

        String text = LocaleUtil.localize(json.properties.get("text"));
        int colour;

        if (json.properties.containsKey("colour")) {
            colour = resolveEquationInt(json, "colour", ctx);
        } else {
            colour = resolveEquationInt(json, "color", ctx);
        }
        GuiElementText element = new GuiElementText(gui, gui.rootElement.offset(posX, posY), text, colour);
        element.setCentered("true".equals(json.properties.get("centered")));
        element.setDropShadow("true".equals(json.properties.get("shadow")));
        gui.guiElements.add(element);
    }
}

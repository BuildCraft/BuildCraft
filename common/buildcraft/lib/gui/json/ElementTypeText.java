package buildcraft.lib.gui.json;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantObject;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.LocaleUtil;
import com.google.gson.JsonSyntaxException;

public class ElementTypeText extends ElementType {
    public static final String NAME = "buildcraftlib:text";
    public static final ElementTypeText INSTANCE = new ElementTypeText();

    private ElementTypeText() {
        super(NAME);
    }

    // pos: the position of the text
    // text: The text to be drawn. Will be localised first, and used as a fallback
    // expression: A replacement for text -- uses an expression rather than as a literal.
    // colour: Default colour to be drawn
    // centered: If true then the text will be centered around pos

    @Override
    public IGuiElement deserialize0(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(json);

        IGuiPosition pos = resolvePosition(json, "pos", parent, ctx);
        INodeObject<String> text;

        String prop;

        if ((prop = json.properties.get("text")) != null) {
            String localized = LocaleUtil.localize(prop);
            text = new NodeConstantObject<>(String.class, localized);
        } else if ((prop = json.properties.get("expression")) != null) {
            try {
                text = GenericExpressionCompiler.compileExpressionString(prop, ctx);
            } catch (InvalidExpressionException e) {
                throw new JsonSyntaxException("Invalid expression for '" + json.name + "'", e);
            }
        } else {
            throw new JsonSyntaxException("Require either 'text' or 'expression'!");
        }

        int colour;
        if (json.properties.containsKey("colour")) {
            colour = resolveEquationInt(json, "colour", ctx);
        } else {
            colour = resolveEquationInt(json, "color", ctx);
        }
        GuiElementText element = new GuiElementText(gui, pos, text, colour);
        element.setCentered("true".equals(json.properties.get("centered")));
        element.setDropShadow("true".equals(json.properties.get("shadow")));
        element.setForeground("true".equals(json.properties.get("foreground")));
        return element;
    }
}

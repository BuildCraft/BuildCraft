package buildcraft.lib.gui.json;

import java.util.function.Supplier;

import com.google.gson.JsonSyntaxException;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.gui.pos.IGuiPosition;
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
    public IGuiElement deserialize0(GuiJson<?> gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(gui, json);

        int posX = resolveEquationInt(json, "pos[0]", ctx);
        int posY = resolveEquationInt(json, "pos[1]", ctx);

        Supplier<String> text;

        String prop;

        if ((prop = json.properties.get("text")) != null) {
            String localized = LocaleUtil.localize(prop);
            text = () -> localized;
        } else if ((prop = json.properties.get("expression")) != null) {
            try {
                IExpressionNode exp = InternalCompiler.compileExpression(prop, ctx);
                text = () -> exp.evaluateAsString();
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
        GuiElementText element = new GuiElementText(gui, parent.offset(posX, posY), text, colour);
        element.setCentered("true".equals(json.properties.get("centered")));
        element.setDropShadow("true".equals(json.properties.get("shadow")));
        return element;
    }
}

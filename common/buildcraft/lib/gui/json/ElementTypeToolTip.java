package buildcraft.lib.gui.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonSyntaxException;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.gui.GuiElementToolTip;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.ITooltipElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;

public class ElementTypeToolTip extends ElementType {
    public static final String NAME = "buildcraftlib:tooltip";
    public static final ElementTypeToolTip INSTANCE = new ElementTypeToolTip();

    private ElementTypeToolTip() {
        super(NAME);
    }

    // Args:
    // - pos[0], pos[1]: the area for help (where it will be drawn, relative to the root of the gui). Defaults
    // to 0,0
    // - size[0], size[1]: the size of the help area
    // - area[0-3]: mapping for pos[0], pos[1], size[0], size[1]
    // - text: The text to display in the tooltip
    // - expression: The expression to display in the tooltip

    @Override
    public IGuiElement deserialize0(GuiJson<?> gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(json);

        List<String> text = new ArrayList<>();
        String key = "text";
        boolean isExpression = false;
        if (json.properties.containsKey("expression") || json.properties.containsKey("expression[0]")) {
            key = "expression";
            isExpression = true;
        }
        if (json.properties.containsKey(key + "[0]")) {
            int i = 0;
            while (true) {
                String prop = json.properties.get(key + "[" + i + "]");
                if (prop == null) {
                    break;
                }
                text.add(prop);
                i++;
            }
        } else {
            text.add(json.properties.getOrDefault(key, "ERROR: Text not given!"));
        }
        INodeBoolean visible = getEquationBool(json, "visible", ctx, true);
        ITooltipElement source;
        if (isExpression) {
            List<INodeObject<String>> nodes = new ArrayList<>(text.size());
            try {
                for (String s : text) {
                    nodes.add(GenericExpressionCompiler.compileExpressionString(s, ctx));
                }
            } catch (InvalidExpressionException e) {
                throw new JsonSyntaxException(e);
            }
            source = (list) -> {
                if (visible.evaluate()) {
                    String[] arr = new String[nodes.size()];
                    for (int i = 0; i < arr.length; i++) {
                        arr[i] = nodes.get(i).evaluate();
                    }
                    list.add(ToolTip.createLocalized(arr));
                }
            };
        } else {
            ToolTip tooltip = ToolTip.createLocalized(text.toArray(new String[0]));
            source = (list) -> {
                if (visible.evaluate()) {
                    list.add(tooltip);
                }
            };
        }

        inheritProperty(json, "area[0]", "pos[0]");
        inheritProperty(json, "area[1]", "pos[1]");
        inheritProperty(json, "area[2]", "size[0]");
        inheritProperty(json, "area[3]", "size[1]");

        int posX = resolveEquationInt(json, "pos[0]", ctx);
        int posY = resolveEquationInt(json, "pos[1]", ctx);
        int sizeX = resolveEquationInt(json, "size[0]", ctx);
        int sizeY = resolveEquationInt(json, "size[1]", ctx);
        GuiRectangle rect = new GuiRectangle(posX, posY, sizeX, sizeY);
        return new GuiElementToolTip(gui, rect.offset(parent), source);
    }
}

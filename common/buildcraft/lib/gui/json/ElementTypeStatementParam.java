package buildcraft.lib.gui.json;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.statement.GuiElementStatementParam;
import buildcraft.lib.statement.FullStatement;
import com.google.gson.JsonSyntaxException;

public class ElementTypeStatementParam extends ElementType {
    public static final String NAME = "buildcraftlib:statement/parameter";
    public static final ElementTypeStatementParam INSTANCE = new ElementTypeStatementParam();

    // - pos[0], pos[1]: the position of the sprite (where it will be drawn, relative to the root of the gui).
    // - size[0], size[1]: the size of the slot area.
    // - area[0-3]: mapping for pos[0], pos[1], size[0], size[1]
    // - source: The FullStatement reference
    // - draw: If false then the element won't be drawn, but will be a surface for placing statements into.
    // - index: The parameter index

    private ElementTypeStatementParam() {
        super(NAME);
    }

    @Override
    public IGuiElement deserialize0(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(json);

        if (!json.properties.containsKey("size[0]")) {
            json.properties.put("size[0]", "18");
        }
        if (!json.properties.containsKey("size[1]")) {
            json.properties.put("size[1]", "18");
        }

        inheritProperty(json, "pos[0]", "area[0]");
        inheritProperty(json, "pos[1]", "area[1]");
        inheritProperty(json, "size[0]", "area[2]");
        inheritProperty(json, "size[1]", "area[3]");

        IGuiArea area = resolveArea(json, "area", parent, ctx);

        String source;
        if (json.properties.containsKey("source")) {
            source = json.properties.get("source");
        } else {
            source = resolveEquation(json, "source_expression", ctx);
            if (source == null) {
                throw new JsonSyntaxException("Expected either 'source' or 'source_expression' for " + NAME);
            }
        }

        boolean draw = !"false".equals(json.properties.get("draw"));

        int index = resolveEquationInt(json, "index", ctx);

        FullStatement<?> fullStatement = gui.properties.get(source, FullStatement.class);
        IStatementContainer statementContainer = gui.properties.get("statement.container", IStatementContainer.class);
        return new GuiElementStatementParam(gui, area, statementContainer, fullStatement, index, draw);
    }
}

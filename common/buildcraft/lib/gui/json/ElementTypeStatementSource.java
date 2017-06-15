package buildcraft.lib.gui.json;

import com.google.gson.JsonSyntaxException;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.statement.GuiElementStatementSource;
import buildcraft.lib.statement.StatementContext;

public class ElementTypeStatementSource extends ElementType {
    public static final String NAME = "buildcraftlib:statement/source";
    public static final ElementTypeStatementSource INSTANCE = new ElementTypeStatementSource();

    // layout: An enum of (flat|ledger).(left|right) with the position of the elements + groupings and display
    // parent: The parent element (NYI)
    // source: A link to a StatementContext element

    private ElementTypeStatementSource() {
        super(NAME);
    }

    @Override
    public void addToGui(GuiJson<?> gui, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(json);

        String parent = json.properties.get("parent");
        String source = json.properties.get("source");

        int posX = resolveEquationInt(json, "pos[0]", ctx);
        int posY = resolveEquationInt(json, "pos[1]", ctx);

        StatementContext<?> ctxSource = gui.miscProperties.get(source, StatementContext.class);

        String layout = json.properties.getOrDefault("layout", "flat.left");
        if ("flat.left".equals(layout) || "flat.right".equals(layout)) {
            IGuiPosition pos = gui.rootElement.offset(posX, posY);
            gui.shownElements.add(new GuiElementStatementSource<>(gui, pos, ctxSource));
            // } else if ("ledger.left".equals(layout) || "ledger.right".equals(layout)) {
            // TODO!
        } else {
            throw new JsonSyntaxException("Unknown layout '" + layout + "'");
        }
    }
}

package buildcraft.lib.gui.json;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.statement.GuiElementStatement;
import buildcraft.lib.statement.FullStatement;

public class ElementTypeStatementSlot extends ElementType {
    public static final String NAME = "buildcraftlib:statement/slot";
    public static final ElementTypeStatementSlot INSTANCE = new ElementTypeStatementSlot();

    // - pos[0], pos[1]: the position of the sprite (where it will be drawn, relative to the root of the gui).
    // - size[0], size[1]: the size of the slot area.
    // - area[0-3]: mapping for pos[0], pos[1], size[0], size[1]
    // - source: The FullStatement reference
    // - draw: If false then the element won't be drawn, but will be a surface for placing statements into.

    private ElementTypeStatementSlot() {
        super(NAME);
    }

    @Override
    public IGuiElement deserialize0(GuiJson<?> gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(gui, json);

        inheritProperty(json, "area[0]", "pos[0]");
        inheritProperty(json, "area[1]", "pos[1]");
        inheritProperty(json, "area[2]", "size[0]");
        inheritProperty(json, "area[3]", "size[1]");

        int posX = resolveEquationInt(json, "pos[0]", ctx);
        int posY = resolveEquationInt(json, "pos[1]", ctx);
        int sizeX = resolveEquationInt(json, "size[0]", ctx);
        int sizeY = resolveEquationInt(json, "size[1]", ctx);

        String source = json.properties.get("source");

        boolean draw = !"false".equals(json.properties.get("draw"));

        FullStatement<?> stmnt = gui.properties.get(source, FullStatement.class);
        IGuiArea area = new GuiRectangle(posX, posY, sizeX, sizeY).offset(parent);
        return new GuiElementStatement<>(gui, area, stmnt, draw);
    }
}

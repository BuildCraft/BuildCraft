package buildcraft.lib.gui.json;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.elem.GuiElementSlotMover;
import buildcraft.lib.gui.pos.IGuiPosition;
import com.google.gson.JsonSyntaxException;
import net.minecraft.world.inventory.Slot;

public class ElementTypeSlot extends ElementType {
    public static final String NAME = "buildcraftlib:slot";
    public static final ElementTypeSlot INSTANCE = new ElementTypeSlot();

    public ElementTypeSlot() {
        super(NAME);
    }

    // pos: the position of the slot
    // slot: The slot to be moved
    // index: If the slot was an InventorySlotHolder then this is the index of the slot in the list
    // visible: If false then the slot won't be visible

    @Override
    protected IGuiElement deserialize0(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(json);

        String slotName = json.properties.get("slot");
        IGuiPosition pos = resolvePosition(json, "pos", parent, ctx);
        Slot slot = gui.properties.get(slotName, Slot.class);
        INodeBoolean visible = getEquationBool(json, "visible", ctx, true);

        if (slot != null) {
            return new GuiElementSlotMover(gui, pos, visible, slot);
        }
        InventorySlotHolder holder = gui.properties.get(slotName, InventorySlotHolder.class);
        if (holder == null) {
            throw new JsonSyntaxException("Unknown slot '" + slotName + "'");
        }
        int index = resolveEquationInt(json, "index", ctx);

        if (index < 0 || index >= holder.slots.length) {
            throw new JsonSyntaxException(
                    "Invalid slot index! (" + index + ", min = 0, max = " + (holder.slots.length - 1) + ")");
        }
        return new GuiElementSlotMover(gui, pos, visible, holder.slots[index]);
    }
}

package buildcraft.lib.gui.statement;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.BCLibSprites;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.json.GuiJson;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.statement.FullStatement;

public class GuiElementStatementParam extends GuiElementSimple<GuiJson<?>>
    implements IInteractionElement, IReference<IStatementParameter> {

    private final IStatementContainer container;
    private final FullStatement<?> ref;
    private final int paramIndex;
    private final boolean draw;

    public GuiElementStatementParam(GuiJson<?> gui, IGuiArea element, IStatementContainer container,
        FullStatement<?> ref, int index, boolean draw) {
        super(gui, element);
        this.container = container;
        this.ref = ref;
        this.paramIndex = index;
        this.draw = draw;
    }

    // IReference

    @Override
    public IStatementParameter get() {
        return ref.get(paramIndex);
    }

    @Override
    public void set(IStatementParameter to) {
        ref.set(paramIndex, to);
        ref.postSetFromGui(paramIndex);
    }

    @Override
    public boolean canSet(Object value) {
        return ref.canSet(paramIndex, value);
    }

    // ITooltipElement

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (contains(gui.mouse)) {
            IStatementParameter s = get();
            if (s != null) {
                String desc = s.getDescription();
                if (desc != null) {
                    tooltips.add(new ToolTip(desc));
                }
            }
        }
    }

    // IGuiElement

    @Override
    public void drawBackground(float partialTicks) {
        if (draw) {
            int max = ref.get().maxParameters();
            double x = getX();
            double y = getY();
            if (paramIndex >= max) {
                GuiElementStatement.ICON_SLOT_BLOCKED.drawAt(x, y);
                return;
            }
            IStatementParameter stmnt = get();
            if (stmnt == null) {
                return;
            }
            x += 1;
            y += 1;
            ItemStack stack = stmnt.getItemStack();

            ISprite sprite = stmnt.getSprite();
            if (stack.isEmpty()) {
                if (sprite == null) {
                    GuiElementStatement.ICON_SLOT_NOT_SET.drawAt(x - 1, y - 1);
                } else {
                    GuiIcon.drawAt(sprite, x, y, 16);
                }
            } else if (sprite == null) {
                gui.drawItemStackAt(stack, (int) x, (int) y);
            } else {
                GuiIcon.drawAt(sprite, x, y, 16);
            }
            if (!ref.canInteract) {
                GuiIcon.drawAt(BCLibSprites.LOCK, x + 1, y + 7, 8);
            }
        }
    }

    // IInteractionElement

    @Override
    public void onMouseClicked(int button) {
        if (ref.canInteract && contains(gui.mouse)) {
            IStatementParameter param = get();
            if (param == null) {
                return;
            }
            StatementMouseClick clickEvent = new StatementMouseClick(0, false);
            IStatementParameter pNew = param.onClick(container, ref.get(), ItemStack.EMPTY, clickEvent);
            if (pNew != null) {
                set(pNew);
            } else {
                IStatementParameter[] possible = param.getPossible(container);
                if (!param.isPossibleOrdered()) {
                    List<IStatementParameter> list = new ArrayList<>();
                    list.add(null);
                    for (IStatementParameter p2 : possible) {
                        if (p2 != null) {
                            list.add(p2);
                        }
                    }
                    possible = list.toArray(new IStatementParameter[0]);
                }
                gui.currentMenu = GuiElementStatementVariant.create(this, this, possible);
            }
        }
        // TODO!
    }

    @Override
    public void onMouseReleased(int button) {
        // TODO!
    }
}

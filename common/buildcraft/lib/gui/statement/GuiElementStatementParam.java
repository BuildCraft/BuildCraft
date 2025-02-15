package buildcraft.lib.gui.statement;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.statement.FullStatement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GuiElementStatementParam extends GuiElementSimple
        implements IInteractionElement, IReference<IStatementParameter> {

    private final IStatementContainer container;
    private final FullStatement<?> ref;
    private final int paramIndex;
    private final boolean draw;

    public GuiElementStatementParam(BuildCraftGui gui, IGuiArea element, IStatementContainer container, FullStatement<?> ref, int index, boolean draw) {
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
    public boolean canSet(IStatementParameter value) {
        return ref.canSet(paramIndex, value);
    }

    @Override
    public Class<IStatementParameter> getHeldType() {
        return IStatementParameter.class;
    }

    // ITooltipElement

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (contains(gui.mouse)) {
            IStatementParameter s = get();
            if (s != null) {
                tooltips.add(new ToolTip(s.getTooltip()));
            }
        }
    }

    // IGuiElement

    @Override
    public void drawBackground(float partialTicks, GuiGraphics guiGraphics) {
        if (draw) {
            IStatement slot = ref.get();
            int max = slot == null ? 0 : slot.maxParameters();
            double x = getX();
            double y = getY();
            if (paramIndex >= max) {
                GuiElementStatement.SLOT_COLOUR.drawAt(guiGraphics, x, y);
                GuiElementStatement.ICON_SLOT_BLOCKED.drawAt(guiGraphics, x, y);
                return;
            }
            IStatementParameter statementParameter = get();
            GuiElementStatementSource.drawGuiSlot(statementParameter, guiGraphics, x, y);
        }
    }

    // IInteractionElement

    @Override
    public void onMouseClicked(int button) {
        if (ref.canInteract && contains(gui.mouse) && button == 0) {
            IStatementParameter param = get();
            if (param == null) {
                return;
            }
            StatementMouseClick clickEvent = new StatementMouseClick(0, false);

            final ItemStack heldStack;
            Player currentPlayer = Minecraft.getInstance().player;
            if (currentPlayer == null) {
                heldStack = ItemStack.EMPTY;
            } else {
//                heldStack = currentPlayer.inventory.getItemStack();
                heldStack = currentPlayer.inventoryMenu.getCarried();
            }

            IStatementParameter pNew = param.onClick(container, ref.get(), heldStack, clickEvent);
            if (pNew != null) {
                set(pNew);
            } else {
                IStatementParameter[] possible = param.getPossible(container);
                if (!param.isPossibleOrdered()) {
                    List<IStatementParameter> list = new ArrayList<>();
                    for (IStatementParameter p2 : possible) {
                        if (p2 != null) {
                            list.add(p2);
                        }
                    }
                    possible = list.toArray(new IStatementParameter[0]);
                }
                gui.currentMenu = GuiElementStatementVariant.create(gui, this, this, possible);
            }
        }
    }
}

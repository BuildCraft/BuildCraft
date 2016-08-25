package buildcraft.lib.gui.ledger;

import java.util.ArrayList;
import java.util.List;

import buildcraft.core.lib.gui.tooltips.ToolTip;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.ITooltipElement;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.GuiUtil;

public class LedgerManager_Neptune implements ITooltipElement {
    public final GuiBC8<?> gui;
    public final IGuiPosition position;
    public final boolean expandPositive;
    public final List<Ledger_Neptune> ledgers = new ArrayList<>();

    public LedgerManager_Neptune(GuiBC8<?> gui, IGuiPosition position, boolean expendPositive) {
        this.gui = gui;
        this.position = position;
        this.expandPositive = expendPositive;
    }

    public void update() {
        for (Ledger_Neptune ledger : ledgers) {
            ledger.update();
        }
    }

    public void drawBackground(float partialTicks) {
        GuiUtil.drawVerticallyAppending(position, ledgers, (ledger, x, y) -> {
            ledger.drawBackground(x, y, partialTicks);
            return ledger.getHeight(partialTicks) + 5;
        });
    }

    public void drawForeground(float partialTicks) {
        GuiUtil.drawVerticallyAppending(position, ledgers, (ledger, x, y) -> {
            ledger.drawForeground(x, y, partialTicks);
            return ledger.getHeight(partialTicks) + 5;
        });
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        for (Ledger_Neptune ledger : ledgers) {
            ledger.addToolTips(tooltips);
        }
    }

    public void onMouseClicked(int button) {
        for (Ledger_Neptune ledger : ledgers) {
            ledger.onMouseClicked(button);
        }
    }

    public void onMouseDragged(int button, long ticksSinceClick) {
        for (Ledger_Neptune ledger : ledgers) {
            ledger.onMouseDragged(button, ticksSinceClick);
        }
    }

    public void onMouseReleased(int button) {
        for (Ledger_Neptune ledger : ledgers) {
            ledger.onMouseReleased(button);
        }
    }
}

package buildcraft.lib.gui.statement;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;

import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.json.GuiJson;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.data.IReference;

public class GuiElementStatement<S extends IStatement> extends GuiElementSimple<GuiJson<?>>
    implements IInteractionElement, IReference<S> {

    private final IReference<S> ref;
    private final boolean draw;

    public GuiElementStatement(GuiJson<?> gui, IGuiArea element, IReference<S> ref, boolean draw) {
        super(gui, element);
        this.ref = ref;
        this.draw = draw;
    }

    // IReference

    @Override
    public S get() {
        return ref.get();
    }

    @Override
    public void set(S to) {
        ref.set(to);
    }

    @Override
    public boolean canSet(Object value) {
        return ref.canSet(value);
    }

    // IGuiElement

    @Override
    public void drawBackground(float partialTicks) {
        if (draw) {
            S stmnt = ref.get();
            if (stmnt == null) return;
            ISprite sprite = stmnt.getSprite();
            if (sprite == null) return;
            GuiIcon.drawAt(sprite, getX() + 1, getY() + 1, 16);
        }
    }

    @Override
    public void onMouseClicked(int button) {
        // TODO!
    }

    @Override
    public void onMouseReleased(int button) {
        // TODO!
    }
}

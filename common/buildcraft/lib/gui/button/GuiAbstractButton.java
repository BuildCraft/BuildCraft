package buildcraft.lib.gui.button;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

/**
 * If this only has 1 subclass (GuiSpriteButton), then why no merge them?
 */
public abstract class GuiAbstractButton<G extends GuiBC8<?>> extends GuiElementSimple<G> implements IButtonClickEventTrigger {
    private final List<IButtonClickEventListener> listeners = new ArrayList<>();

    public final String id;
    public boolean active, enabled = true, visible = true;
    private IButtonBehaviour behaviour = IButtonBehaviour.DEFAULT;
    private ToolTip toolTip;

    public GuiAbstractButton(G gui, String id, IGuiPosition parent, GuiRectangle rect) {
        super(gui, parent, rect);
        this.id = id;
    }

    public GuiAbstractButton(G gui, String id, IGuiArea area) {
        super(gui, area);
        this.id = id;
    }

    public GuiElementText createTextElement(String text) {
        int width = gui.getFontRenderer().getStringWidth(text);
        int height = gui.getFontRenderer().FONT_HEIGHT;
        IGuiPosition pos = getCenter().offset(-width / 2, -height / 2);
        return new GuiElementText(gui, pos, () -> text, this::getColourForText);
    }

    public GuiElementText createTextElement(Supplier<String> text) {
        IntSupplier x = () -> -gui.getFontRenderer().getStringWidth(text.get()) / 2;
        IntSupplier y = () -> -gui.getFontRenderer().FONT_HEIGHT / 2;
        IGuiPosition pos = getCenter().offset(x, y);
        return new GuiElementText(gui, pos, text, this::getColourForText);
    }

    public int getColourForText() {
        if (!enabled) {
            return 0xa0_a0_a0;
        } else if (isMouseOver()) {
            return 0xff_ff_a0;
        } else {
            return 0xe0_e0_e0;
        }
    }

    // Properties

    public boolean isActive() {
        return active;
    }

    public final void activate() {
        setActive(true);
    }

    public final void deActivate() {
        setActive(false);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isMouseOver() {
        return contains(gui.mouse);
    }

    protected int getHoverState(boolean mouseOver) {
        if (!enabled) {
            return 0;
        }

        return mouseOver ? (active ? 2 : 4) : (active ? 1 : 3);
    }

    // Behaviour

    public void setBehaviour(IButtonBehaviour behaviour) {
        this.behaviour = behaviour;
    }

    // Tooltips

    public void setToolTip(ToolTip tips) {
        this.toolTip = tips;
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (visible && toolTip != null && isMouseOver()) {
            tooltips.add(toolTip);
        }
    }

    // Click Notification

    @Override
    public void notifyButtonClicked(int bkey) {
        for (IButtonClickEventListener listener : listeners) {
            listener.handleButtonClick(this, bkey);
        }
    }

    @Override
    public void registerListener(IButtonClickEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(IButtonClickEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isButtonActive() {
        return this.active;
    }

    // IGuiElement

    @Override
    public void onMouseClicked(int button) {
        if (contains(gui.mouse)) {
            behaviour.mousePressed(this, button);
        }
    }

    @Override
    public void onMouseReleased(int button) {
        behaviour.mouseReleased(this, button);
    }
}

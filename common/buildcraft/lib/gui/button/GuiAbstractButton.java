package buildcraft.lib.gui.button;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.ITooltipElement;
import buildcraft.lib.gui.elem.ToolTip;

public abstract class GuiAbstractButton extends GuiButton implements ITooltipElement, IButtonClickEventTrigger {
    public final GuiBC8<?> gui;
    private final List<IButtonClickEventListener> listeners = new ArrayList<>();

    public boolean active = false;
    private IButtonBehaviour behaviour = IButtonBehaviour.DEFAULT;
    private ToolTip toolTip;

    public GuiAbstractButton(GuiBC8<?> gui, int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
        this.gui = gui;
    }

    public GuiAbstractButton(GuiBC8<?> gui, int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.gui = gui;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        active = true;
    }

    public void deActivate() {
        active = false;
    }

    @Override
    public boolean isMouseOver() {
        return getGuiRectangle().contains(gui.mouse);
    }

    @Override
    protected int getHoverState(boolean mouseOver) {
        if (enabled) {
            if (mouseOver) {
                return 2;
            } else if (active) {
                return 3;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    // Behaviour

    public GuiAbstractButton setBehaviour(IButtonBehaviour behaviour) {
        this.behaviour = behaviour;
        return this;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            behaviour.mousePressed(this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        super.mouseReleased(mouseX, mouseY);
        if (isMouseOver()) {
            behaviour.mouseReleased(this);
        }
    }

    // Tooltips

    public GuiAbstractButton setToolTip(ToolTip tips) {
        this.toolTip = tips;
        return this;
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (visible && toolTip != null && isMouseOver()) {
            tooltips.add(toolTip);
        }
    }

    // Click Notification

    @Override
    public void notifyButtonStateChange() {
        for (IButtonClickEventListener listener : listeners) {
            listener.handleButtonClick(this, id);
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

    // Misc

    public GuiRectangle getGuiRectangle() {
        return new GuiRectangle(this.xPosition, this.yPosition, this.width, this.height);
    }
}

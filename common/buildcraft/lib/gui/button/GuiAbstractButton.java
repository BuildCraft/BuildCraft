package buildcraft.lib.gui.button;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;

public abstract class GuiAbstractButton extends GuiButton implements IGuiElement, IButtonClickEventTrigger {
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

    // Properties

    public boolean isActive() {
        return active;
    }

    public void activate() {
        active = true;
    }

    public void deActivate() {
        active = false;
    }

    public GuiAbstractButton setActive(boolean active) {
        this.active = active;
        return this;
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
            onMouseClicked(0);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        super.mouseReleased(mouseX, mouseY);
        onMouseReleased(0);
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
    public void notifyButtonClicked(int bkey) {
        for (IButtonClickEventListener listener : listeners) {
            listener.handleButtonClick(this, id, bkey);
        }
    }

    @Override
    public GuiAbstractButton registerListener(IButtonClickEventListener listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public GuiAbstractButton removeListener(IButtonClickEventListener listener) {
        listeners.remove(listener);
        return this;
    }

    @Override
    public boolean isButtonActive() {
        return this.active;
    }

    // IGuiArea

    @Override
    public int getX() {
        return xPosition;
    }

    @Override
    public int getY() {
        return yPosition;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public GuiRectangle getGuiRectangle() {
        return asImmutable();
    }

    // IGuiElement

    @Override
    public final void drawBackground(float partialTicks) {
        drawButton(gui.mc, gui.mouse.getX(), gui.mouse.getY());
    }

    @Override
    public final void drawForeground(float partialTicks) {
        drawButtonForegroundLayer(gui.mouse.getX(), gui.mouse.getY());
    }

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

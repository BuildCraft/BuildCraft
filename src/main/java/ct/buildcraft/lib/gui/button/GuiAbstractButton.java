/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.button;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.GuiElementSimple;
import ct.buildcraft.lib.gui.IInteractionElement;
import ct.buildcraft.lib.gui.elem.GuiElementText;
import ct.buildcraft.lib.gui.elem.ToolTip;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.gui.pos.IGuiPosition;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/** If this only has 1 subclass (GuiButtonDrawable), then why not merge them? */
public abstract class GuiAbstractButton extends GuiElementSimple
    implements IButtonClickEventTrigger, IInteractionElement {
    private final List<IButtonClickEventListener> listeners = new ArrayList<>();

    public final String id;
    public boolean active, enabled = true, visible = true;
    private IButtonBehaviour behaviour = IButtonBehaviour.DEFAULT;
    private ToolTip toolTip;

    public GuiAbstractButton(BuildCraftGui gui, String id, IGuiArea area) {
        super(gui, area);
        this.id = id;
    }

    public GuiElementText createTextElement(Component text) {
        return createTextElement(() -> text);
    }

    public GuiElementText createTextElement(Supplier<Component> text) {
        Font fr = gui.mc.font;
        DoubleSupplier x = () -> -fr.width(text.get()) / 2;
        DoubleSupplier y = () -> -fr.lineHeight / 2;
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

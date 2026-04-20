package ct.buildcraft.lib.gui.statement;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.statements.IGuiSlot;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.IGuiElement;
import ct.buildcraft.lib.gui.IMenuElement;
import ct.buildcraft.lib.misc.data.IReference;
import ct.buildcraft.lib.statement.StatementWrapper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class GuiElementStatementDrag implements IMenuElement {

    public final BuildCraftGui gui;

    private boolean isDragging;

    @Nullable
    private IGuiSlot dragging;

    public GuiElementStatementDrag(BuildCraftGui gui) {
        this.gui = gui;
    }

    // Dragging

    public void startDragging(IGuiSlot slot) {
        isDragging = true;
        dragging = slot;
        gui.currentMenu = this;
    }

    // IGuiElement

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public void drawForeground(PoseStack pose, float partialTicks) {
        if (isDragging) {
            boolean canPlace = false;
            for (IGuiElement element : gui.getElementsAt(gui.mouse.getX(), gui.mouse.getY())) {
                if (element instanceof IReference<?>) {
                    if (checkCanSet((IReference<?>) element, dragging)) {
                        canPlace = true;
                        break;
                    }
                }
            }
            if (!canPlace) {
                RenderSystem.setShaderColor(1f, 0.7f, 0.7f, 1f);
            }
            double x = gui.mouse.getX() - 9;
            double y = gui.mouse.getY() - 9;
            if (dragging instanceof IStatementParameter) {
                ParameterRenderer.draw(pose, (IStatementParameter) dragging, x, y);
            } else {
                GuiIcon background = GuiElementStatement.SLOT_COLOUR;
                if (dragging instanceof StatementWrapper) {
                    EnumPipePart part = ((StatementWrapper) dragging).sourcePart;
                    if (part != EnumPipePart.CENTER) {
                        background = background.offset(0, (1 + part.getIndex()) * 18);
                    }
                }
                background.drawAt(pose, x, y);
                if (dragging != null) {
                    ISprite sprite = dragging.getSprite();
                    if (sprite != null) {
                        GuiIcon.drawAt(pose, sprite, x + 1, y + 1, 16);
                    }
                }
            }
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }

    private static <T> boolean checkCanSet(IReference<T> ref, Object value) {
        if (value == null) {
            return ref.canSet(null);
        }
        T obj = ref.convertToType(value);
        return obj != null && ref.canSet(obj);
    }

    // IInteractableElement

    @Override
    public void onMouseClicked(int button) {
        if (button != 1) {
            return;
        }
        for (IGuiElement element : gui.getElementsAt(gui.mouse.getX(), gui.mouse.getY())) {
            if (element instanceof IReference<?>) {
                IReference<?> ref = (IReference<?>) element;
                Object obj = ref.get();
                if (obj == null || obj instanceof IGuiSlot) {
                    startDragging((IGuiSlot) obj);
                    break;
                }
            }
        }
    }

    @Override
    public void onMouseReleased(int button) {
        if (!isDragging) {
            return;
        }
        for (IGuiElement element : gui.getElementsAt(gui.mouse.getX(), gui.mouse.getY())) {
            if (element instanceof IReference<?>) {
                IReference<?> ref = (IReference<?>) element;
                ref.setIfCan(dragging);
            }
        }
        isDragging = false;
        dragging = null;
        if (gui.currentMenu == this) {
            gui.currentMenu = null;
        }
    }

    // IMenuElement

    @Override
    public boolean shouldFullyOverride() {
        return false;
    }
}

package buildcraft.lib.gui.statement;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IGuiSlot;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.IMenuElement;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.statement.StatementWrapper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

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
    public void drawForeground(GuiGraphics guiGraphics, float partialTicks) {
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
//            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);
            if (!canPlace) {
//                GlStateManager.color(1.0f, 0.7f, 0.7f);
                RenderUtil.color(1.0f, 0.7f, 0.7f);
            }
            double x = gui.mouse.getX() - 9;
            double y = gui.mouse.getY() - 9;
            if (dragging instanceof IStatementParameter) {
                ParameterRenderer.draw((IStatementParameter) dragging, guiGraphics, x, y);
            } else {
                GuiIcon background = GuiElementStatement.SLOT_COLOUR;
                if (dragging instanceof StatementWrapper) {
                    EnumPipePart part = ((StatementWrapper) dragging).sourcePart;
                    if (part != EnumPipePart.CENTER) {
                        background = background.offset(0, (1 + part.getIndex()) * 18);
                    }
                }
                background.drawAt(guiGraphics, x, y);
                if (dragging != null) {
                    ISprite sprite = dragging.getSprite();
                    if (sprite != null) {
                        GuiIcon.drawAt(sprite, guiGraphics, x + 1, y + 1, 16);
                    }
                }
            }
//            GlStateManager.color(1, 1, 1);
            RenderUtil.color(1, 1, 1);
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

package buildcraft.lib.gui;

import buildcraft.lib.BCLibSprites;
import buildcraft.lib.expression.api.IVariableNode.IVariableNodeBoolean;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.MousePosition;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A gui element that allows for easy implementation of an actual {@link Screen} class.
 * <p>
 * This isn't final, although you should generally only subclass this for additional library functionality, not to
 * render out a particular gui.
 * <p>
 * Classes extending {@link Screen} (either directly or indirectly) need to call the following methods:
 * <ul>
 * <li>{@link #tick()} once per tick (usually in {@link Screen#tick()}</li>
 * <li>{@link #drawBackgroundLayer(GuiGraphics, float, int, int, Runnable)} before drawing anything else, except for your own
 * backgrounds</li>
 * <li>{@link #drawElementBackgrounds(GuiGraphics)} after {@link #drawBackgroundLayer(GuiGraphics, float, int, int, Runnable)},but before
 * sub-display backgrounds</li>
 * <li>{@link #drawElementForegrounds(Runnable, GuiGraphics)} after drawing everything else.</li>
 * <li>{@link #preDrawForeground(PoseStack)} if your base gui class offsets the call to drawing the foreground by the gui's
 * position, for example, {@link AbstractContainerScreen}.</li>
 * <li>{@link #postDrawForeground(PoseStack)} after {@link #preDrawForeground(PoseStack)} (and the same rules apply). These two calls
 * should wrap around and calls to this that occur while the gl state is translated.
 * <li>{@link #onMouseClicked(double, double, int)} whenever the mouse is clicked. If this returns true you shouldn't do any
 * other mouse click handling.</li>
 * <li>{@link #onMouseReleased(double, double, int)} whenever the mouse is released.</li>
 * <li>{@link #onMouseDragged(double, double, int)} whenever the mouse is dragged.</li>
 * </ul>
 * For both {@link #drawBackgroundLayer(GuiGraphics, float, int, int, Runnable)} and {@link #drawElementForegrounds(Runnable, GuiGraphics)} the
 * {@link Runnable} passed will only be called once, and it's call time will differ based on the
 * {@link #currentMenu}.
 */
public class BuildCraftGui {

    /** Used to control if this gui should show debugging lines, and other oddities that help development. */
    public static final IVariableNodeBoolean isDebuggingEnabled;

    /** If true then the debug icon will be shown. */
    public static final IVariableNodeBoolean isDebuggingShown;

    static {
        ResourceLocation debugDef = new ResourceLocation("buildcraftlib", "base");
        isDebuggingShown = GuiConfigManager.getOrAddBoolean(debugDef, "debugging_is_shown", false);
        isDebuggingEnabled = GuiConfigManager.getOrAddBoolean(debugDef, "debugging_is_enabled", false);
    }

    public static final GuiSpriteScaled SPRITE_DEBUG = new GuiSpriteScaled(BCLibSprites.DEBUG, 16, 16);

    public final Minecraft mc = Minecraft.getInstance();
    public final Screen gui;
    public final MousePosition mouse = new MousePosition();

    /** The area that encompasses the entire screen. */
    public final IGuiArea screenElement;

    /**
     * The area that most of the GUI elements should be in. For most container-based gui's this will be a rectangle
     * smaller than the entire screen. For gui's that display outside of a world this will probably be the entire
     * screen, and then this will equal the {@link #screenElement}.
     */
    public final IGuiArea rootElement;

    /** All of the {@link IGuiElement} which will be drawn by this gui. */
    public final List<IGuiElement> shownElements = new ArrayList<>();
    public IMenuElement currentMenu;

    /** Ledger-style elements. */
    public IGuiPosition lowerLeftLedgerPos, lowerRightLedgerPos;
    private float lastPartialTicks;

    public BuildCraftGui(Screen gui, IGuiArea rootElement) {
        this.gui = gui;
        this.screenElement = GuiUtil.AREA_WHOLE_SCREEN;
        this.rootElement = rootElement;

        lowerLeftLedgerPos = rootElement.offset(0, 5);
        lowerRightLedgerPos = rootElement.getPosition(1, -1).offset(0, 5);
    }

    /**
     * Creates a new {@link BuildCraftGui} that uses the entire screen for display. Ledgers are displayed on the
     * opposite side (so that they expand properly).
     */
    public BuildCraftGui(Screen gui) {
        this.gui = gui;
        this.screenElement = GuiUtil.AREA_WHOLE_SCREEN;
        this.rootElement = screenElement;

        lowerLeftLedgerPos = screenElement.getPosition(1, -1).offset(-5, 5);
        lowerRightLedgerPos = screenElement.offset(5, 5);
    }

    /**
     * Creates a new {@link BuildCraftGui} that takes it's {@link #rootElement} from the {@link AbstractContainerMenu}'s
     * size.
     */
//    public static IGuiArea createWindowedArea(GuiContainer gui)
    public static IGuiArea createWindowedArea(AbstractContainerScreen<?> gui) {
        return IGuiArea.create(gui::getGuiLeft, gui::getGuiTop, gui::getXSize, gui::getYSize);
    }

    /** @return The current partial ticks value. */
    public final float getLastPartialTicks() {
        return lastPartialTicks;
    }

    public void tick() {
        if (currentMenu != null) {
            currentMenu.tick();
        }
        for (IGuiElement element : shownElements) {
            element.tick();
        }
    }

    public List<IGuiElement> getElementsAt(double x, double y) {
        List<IGuiElement> elements = new ArrayList<>();
        IMenuElement m = currentMenu;
        if (m != null) {
            elements.addAll(m.getThisAndChildrenAt(x, y));
            if (m.shouldFullyOverride()) {
                return elements;
            }
        }
        for (IGuiElement elem : shownElements) {
            elements.addAll(elem.getThisAndChildrenAt(x, y));
        }
        return elements;
    }

    private List<ToolTip> getAllTooltips() {
        List<ToolTip> tooltips = new ArrayList<>();

        IMenuElement m = currentMenu;
        if (m != null) {
            m.addToolTips(tooltips);
            if (m.shouldFullyOverride()) {
                return tooltips;
            }
        }

        if (gui instanceof ITooltipElement) {
            ((ITooltipElement) gui).addToolTips(tooltips);
        }
        for (IGuiElement elem : shownElements) {
            elem.addToolTips(tooltips);
        }
        return tooltips;
    }

    // private int drawTooltip(ToolTip tooltip, double x, double y)
    private int drawTooltip(ToolTip tooltip, GuiGraphics guiGraphics, double x, double y) {
        int _x = (int) Math.round(x);
        int _y = (int) Math.round(y);
        int _w = (int) Math.round(screenElement.getWidth());
        int _h = (int) Math.round(screenElement.getHeight());
        return 4 + GuiUtil.drawHoveringText(guiGraphics, tooltip, _x, _y, _w, _h, -1, mc.font);
    }

    public void drawBackgroundLayer(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, Runnable menuBackgroundRenderer) {
        // Calen: smoother than the in-para partialTicks
        // FIX FOR MC-121719 // https://bugs.mojang.com/browse/MC-121719
//        partialTicks = mc.getRenderPartialTicks();
        partialTicks = mc.getFrameTime();
        // END FIX

//        RenderHelper.disableStandardItemLighting();
        RenderUtil.disableStandardItemLighting();
        this.lastPartialTicks = partialTicks;
        mouse.setMousePosition(mouseX, mouseY);
        if (currentMenu == null || !currentMenu.shouldFullyOverride()) {
            menuBackgroundRenderer.run();
        }

//        GlStateManager.color(1, 1, 1, 1);
        RenderUtil.color(1, 1, 1, 1);
        if (isDebuggingShown.evaluate()) {
            SPRITE_DEBUG.drawAt(guiGraphics, 0, 0);
            if (isDebuggingEnabled.evaluate()) {
                guiGraphics.fill(0, 0, 16, 16, 0x33_FF_FF_FF);

                if (rootElement != screenElement) {
                    // draw the outer resizing edges
                    int w = 320;
                    int h = 240;

                    int sx = (int) ((rootElement.getWidth() - w) / 2);
                    int sy = (int) ((rootElement.getHeight() - h) / 2);
                    int ex = sx + w + 1;
                    int ey = sy + h + 1;
                    sx--;
                    sy--;

                    guiGraphics.fill(sx, sy, ex + 1, sy + 1, -1);
                    guiGraphics.fill(sx, ey, ex + 1, ey + 1, -1);

                    guiGraphics.fill(sx, sy, sx + 1, ey + 1, -1);
                    guiGraphics.fill(ex, sy, ex + 1, ey + 1, -1);
                }
            }
        }
    }

    public void drawElementBackgrounds(GuiGraphics guiGraphics) {
        for (IGuiElement element : shownElements) {
            if (element != currentMenu) {
                element.drawBackground(lastPartialTicks, guiGraphics);
            }
        }
    }

    // public void preDrawForeground()
    public void preDrawForeground(PoseStack poseStack) {
//        GlStateManager.pushMatrix();
        poseStack.pushPose();
//        GlStateManager.translate(-rootElement.getX(), -rootElement.getY(), 0);
        poseStack.translate(-rootElement.getX(), -rootElement.getY(), 0);
    }

    public void postDrawForeground(PoseStack poseStack) {
//        GlStateManager.popMatrix();
        poseStack.popPose();
    }

    /**
     * @param menuBackgroundRenderer Will be called to draw the background if the current menu returns true from
     * {@link IMenuElement#shouldFullyOverride()}. This will draw above all of the normal elements.
     * {@link GL11#GL_DEPTH_TEST} will have been disabled for this.
     */
    public void drawElementForegrounds(Runnable menuBackgroundRenderer, GuiGraphics guiGraphics) {
        // Calen: if disableDepth, tooltip will be under currentMenu
        RenderUtil.enableDepth();

        for (IGuiElement element : shownElements) {
            if (element != currentMenu) {
                element.drawForeground(guiGraphics, lastPartialTicks);
            }
        }

        IMenuElement m = currentMenu;
        if (m != null) {
            if (m.shouldFullyOverride() && menuBackgroundRenderer != null) {
//                GlStateManager.disableDepth();
                menuBackgroundRenderer.run();
//                GlStateManager.enableDepth();
            }
            m.drawBackground(lastPartialTicks, guiGraphics);
            m.drawForeground(guiGraphics, lastPartialTicks);
        }

        GuiUtil.drawVerticallyAppending(mouse, getAllTooltips(), this::drawTooltip, guiGraphics);

        if (isDebuggingEnabled.evaluate()) {
            int x = 6;
            int y = 18;
            List<String> info = new ArrayList<>();
            IntOpenHashSet xAxisFilled = new IntOpenHashSet();
            Font fr = mc.font;
            for (IGuiElement elem : this.getElementsAt(mouse.getX(), mouse.getY())) {
                String name = elem.getDebugInfo(info);
                int sx = (int) elem.getX();
                int sy = (int) elem.getY();
                int ex = sx + (int) elem.getWidth() + 1;
                int ey = sy + (int) elem.getHeight() + 1;
                sx--;
                sy--;

                int colour = (name.hashCode() | 0xFF_00_00_00);
                float[] hsb = Color.RGBtoHSB(colour & 0xFF, (colour >> 8) & 0xFF, (colour >> 16) & 0xFF, null);
                int colourDark = Color.HSBtoRGB(hsb[0], hsb[1], Math.max(hsb[2] - 0.25f, 0)) | 0xFF_00_00_00;

                guiGraphics.fill(sx, sy, ex + 1, sy + 1, colour);
                guiGraphics.fill(sx, ey, ex + 1, ey + 1, colour);

                guiGraphics.fill(sx, sy, sx + 1, ey + 1, colour);
                guiGraphics.fill(ex, sy, ex + 1, ey + 1, colour);

                guiGraphics.fill(sx - 1, sy - 1, ex + 2, sy, colourDark);
                guiGraphics.fill(sx - 1, ey + 1, ex + 2, ey + 2, colourDark);

                guiGraphics.fill(sx - 1, sy - 1, sx, ey + 2, colourDark);
                guiGraphics.fill(ex + 1, sy - 1, ex + 2, ey + 2, colourDark);

//                fr.drawStringWithShadow(name, x, y, -1);
                guiGraphics.drawString(fr, name, x, y, -1, true);

                int w = fr.width(name) + 3;

                int mx = ((sx + 3) >> 2) << 2;
                for (int x2 = mx; x2 < ex; x2 += 4) {
                    if (xAxisFilled.add(x2)) {
                        mx = x2;
                        break;
                    }
                }

                GuiUtil.drawHorizontalLine(guiGraphics, x + w, mx, y + 4, colour);
                GuiUtil.drawVerticalLine(guiGraphics, mx, y + 4, sy, colour);
                y += fr.lineHeight + 2;

                for (String line : info) {
//                    fr.drawStringWithShadow(line, x + 7, y, -1);
                    guiGraphics.drawString(fr, line, x + 7, y, -1, true);
                    y += fr.lineHeight + 2;
                }
                info.clear();
            }
        }
    }

    /**
     * @return True if the {@link #currentMenu} {@link IMenuElement#shouldFullyOverride() fully overrides} other mouse
     * clicks, false otherwise.
     */
//    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton)
    public boolean onMouseClicked(double mouseX, double mouseY, int mouseButton) {
        mouse.setMousePosition(mouseX, mouseY);

        if (isDebuggingShown.evaluate()) {
            GuiRectangle debugRect = new GuiRectangle(0, 0, 16, 16);
            if (debugRect.contains(mouse)) {
                isDebuggingEnabled.set(!isDebuggingEnabled.evaluate());
            }
        }

        IMenuElement m = currentMenu;
        if (m != null) {
            m.onMouseClicked(mouseButton);
            if (m.shouldFullyOverride()) {
                return true;
            }
        }

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement interactionElement) {
                interactionElement.onMouseClicked(mouseButton);
            }
        }
        return false;
    }

    // public void onMouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    public void onMouseDragged(double mouseX, double mouseY, int clickedMouseButton) {
        mouse.setMousePosition(mouseX, mouseY);

        IMenuElement m = currentMenu;
        if (m != null) {
//            m.onMouseDragged(clickedMouseButton, timeSinceLastClick);
            m.onMouseDragged(clickedMouseButton);
            if (m.shouldFullyOverride()) {
                return;
            }
        }

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
//                ((IInteractionElement) element).onMouseDragged(clickedMouseButton, timeSinceLastClick);
                ((IInteractionElement) element).onMouseDragged(clickedMouseButton);
            }
        }
    }

    // public void onMouseReleased(int mouseX, int mouseY, int state)
    public void onMouseReleased(double mouseX, double mouseY, int state) {
        mouse.setMousePosition(mouseX, mouseY);

        IMenuElement m = currentMenu;
        if (m != null) {
            m.onMouseReleased(state);
            if (m.shouldFullyOverride()) {
                return;
            }
        }

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
                ((IInteractionElement) element).onMouseReleased(state);
            }
        }
    }

    // public boolean onKeyTyped(char typedChar, int keyCode)
    public boolean onKeyTyped(int typedChar, int keyCode, int modifiers) {
        boolean action = false;
        IMenuElement m = currentMenu;
        if (m != null) {
//            action = m.onKeyPress(typedChar, keyCode);
            action = m.onKeyPress(typedChar, keyCode, modifiers);
            if (action && m.shouldFullyOverride()) {
                return true;
            }
        }

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
                action |= ((IInteractionElement) element).onKeyPress(typedChar, keyCode, modifiers);
            }
        }
        return action;
    }

    public boolean charTyped(char typedChar, int keyCode) {
        boolean action = false;
        IMenuElement m = currentMenu;
        if (m != null) {
//            action = m.onKeyPress(typedChar, keyCode);
            action = m.charTyped(typedChar, keyCode);
            if (action && m.shouldFullyOverride()) {
                return true;
            }
        }

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
                action |= ((IInteractionElement) element).charTyped(typedChar, keyCode);
            }
        }
        return action;
    }
}

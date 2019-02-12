package buildcraft.lib.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import gnu.trove.set.hash.TIntHashSet;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.BCLibSprites;
import buildcraft.lib.expression.api.IVariableNode.IVariableNodeBoolean;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.MousePosition;
import buildcraft.lib.misc.GuiUtil;

/** A gui element that allows for easy implementation of an actual {@link GuiScreen} class.
 * <p>
 * This isn't final, although you should generally only subclass this for additional library functionality, not to
 * render out a particular gui.
 * <p>
 * Classes extending {@link GuiScreen} (either directly or indirectly) need to call the following methods:
 * <ul>
 * <li>{@link #tick()} once per tick (usually in {@link GuiScreen#updateScreen()}</li>
 * <li>{@link #drawBackgroundLayer(float, int, int, Runnable)} before drawing anything else, except for your own
 * backgrounds</li>
 * <li>{@link #drawElementBackgrounds()} after {@link #drawBackgroundLayer(float, int, int, Runnable)},but before
 * sub-display backgrounds</li>
 * <li>{@link #drawElementForegrounds(Runnable)} after drawing everything else.</li>
 * <li>{@link #preDrawForeground()} if your base gui class offsets the call to drawing the foreground by the gui's
 * position, for example, {@link GuiContainer}.</li>
 * <li>{@link #postDrawForeground()} after {@link #preDrawForeground()} (and the same rules apply). These two calls
 * should wrap around and calls to this that occur while the gl state is translated.
 * <li>{@link #onMouseClicked(int, int, int)} whenever the mouse is clicked. If this returns true you shouldn't do any
 * other mouse click handling.</li>
 * <li>{@link #onMouseReleased(int, int, int)} whenever the mouse is released.</li>
 * <li>{@link #onMouseDragged(int, int, int, long)} whenever the mouse is dragged.</li>
 * </ul>
 * For both {@link #drawBackgroundLayer(float, int, int, Runnable)} and {@link #drawElementForegrounds(Runnable)} the
 * {@link Runnable} passed will only be called once, and it's call time will differ based on the
 * {@link #currentMenu}. */
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

    public final Minecraft mc = Minecraft.getMinecraft();
    public final GuiScreen gui;
    public final MousePosition mouse = new MousePosition();

    /** The area that encompasses the entire screen. */
    public final IGuiArea screenElement;

    /** The area that most of the GUI elements should be in. For most container-based gui's this will be a rectangle
     * smaller than the entire screen. For gui's that display outside of a world this will probably be the entire
     * screen, and then this will equal the {@link #screenElement}. */
    public final IGuiArea rootElement;

    /** All of the {@link IGuiElement} which will be drawn by this gui. */
    public final List<IGuiElement> shownElements = new ArrayList<>();
    public IMenuElement currentMenu;

    /** Ledger-style elements. */
    public IGuiPosition lowerLeftLedgerPos, lowerRightLedgerPos;
    private float lastPartialTicks;

    public BuildCraftGui(GuiScreen gui, IGuiArea rootElement) {
        this.gui = gui;
        this.screenElement = GuiUtil.AREA_WHOLE_SCREEN;
        this.rootElement = rootElement;

        lowerLeftLedgerPos = rootElement.offset(0, 5);
        lowerRightLedgerPos = rootElement.getPosition(1, -1).offset(0, 5);
    }

    /** Creates a new {@link BuildCraftGui} that uses the entire screen for display. Ledgers are displayed on the
     * opposite side (so that they expand properly). */
    public BuildCraftGui(GuiScreen gui) {
        this.gui = gui;
        this.screenElement = GuiUtil.AREA_WHOLE_SCREEN;
        this.rootElement = screenElement;

        lowerLeftLedgerPos = screenElement.getPosition(1, -1).offset(-5, 5);
        lowerRightLedgerPos = screenElement.offset(5, 5);
    }

    /** Creates a new {@link BuildCraftGui} that takes it's {@link #rootElement} from the {@link GuiContainer}'s
     * size. */
    public static IGuiArea createWindowedArea(GuiContainer gui) {
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

    private int drawTooltip(ToolTip tooltip, double x, double y) {
        int _x = (int) Math.round(x);
        int _y = (int) Math.round(y);
        int _w = (int) Math.round(screenElement.getWidth());
        int _h = (int) Math.round(screenElement.getHeight());
        return 4 + GuiUtil.drawHoveringText(tooltip, _x, _y, _w, _h, -1, mc.fontRenderer);
    }

    public void drawBackgroundLayer(float partialTicks, int mouseX, int mouseY, Runnable menuBackgroundRenderer) {
        // FIX FOR MC-121719 // https://bugs.mojang.com/browse/MC-121719
        partialTicks = mc.getRenderPartialTicks();
        // END FIX

        RenderHelper.disableStandardItemLighting();
        this.lastPartialTicks = partialTicks;
        mouse.setMousePosition(mouseX, mouseY);
        if (currentMenu == null || !currentMenu.shouldFullyOverride()) {
            menuBackgroundRenderer.run();
        }

        GlStateManager.color(1, 1, 1, 1);
        if (isDebuggingShown.evaluate()) {
            SPRITE_DEBUG.drawAt(0, 0);
            if (isDebuggingEnabled.evaluate()) {
                Gui.drawRect(0, 0, 16, 16, 0x33_FF_FF_FF);

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

                    Gui.drawRect(sx, sy, ex + 1, sy + 1, -1);
                    Gui.drawRect(sx, ey, ex + 1, ey + 1, -1);

                    Gui.drawRect(sx, sy, sx + 1, ey + 1, -1);
                    Gui.drawRect(ex, sy, ex + 1, ey + 1, -1);
                }
            }
        }
    }

    public void drawElementBackgrounds() {
        for (IGuiElement element : shownElements) {
            if (element != currentMenu) {
                element.drawBackground(lastPartialTicks);
            }
        }
    }

    public void preDrawForeground() {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-rootElement.getX(), -rootElement.getY(), 0);
    }

    public void postDrawForeground() {
        GlStateManager.popMatrix();
    }

    /** @param menuBackgroundRenderer Will be called to draw the background if the current menu returns true from
     *            {@link IMenuElement#shouldFullyOverride()}. This will draw above all of the normal elements.
     *            {@link GL11#GL_DEPTH_TEST} will have been disabled for this. */
    public void drawElementForegrounds(Runnable menuBackgroundRenderer) {

        for (IGuiElement element : shownElements) {
            if (element != currentMenu) {
                element.drawForeground(lastPartialTicks);
            }
        }

        IMenuElement m = currentMenu;
        if (m != null) {
            if (m.shouldFullyOverride() && menuBackgroundRenderer != null) {
                GlStateManager.disableDepth();
                menuBackgroundRenderer.run();
                GlStateManager.enableDepth();
            }
            m.drawBackground(lastPartialTicks);
            m.drawForeground(lastPartialTicks);
        }

        GuiUtil.drawVerticallyAppending(mouse, getAllTooltips(), this::drawTooltip);

        if (isDebuggingEnabled.evaluate()) {
            int x = 6;
            int y = 18;
            List<String> info = new ArrayList<>();
            TIntHashSet xAxisFilled = new TIntHashSet();
            FontRenderer fr = mc.fontRenderer;
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

                Gui.drawRect(sx, sy, ex + 1, sy + 1, colour);
                Gui.drawRect(sx, ey, ex + 1, ey + 1, colour);

                Gui.drawRect(sx, sy, sx + 1, ey + 1, colour);
                Gui.drawRect(ex, sy, ex + 1, ey + 1, colour);

                Gui.drawRect(sx - 1, sy - 1, ex + 2, sy, colourDark);
                Gui.drawRect(sx - 1, ey + 1, ex + 2, ey + 2, colourDark);

                Gui.drawRect(sx - 1, sy - 1, sx, ey + 2, colourDark);
                Gui.drawRect(ex + 1, sy - 1, ex + 2, ey + 2, colourDark);

                fr.drawStringWithShadow(name, x, y, -1);

                int w = fr.getStringWidth(name) + 3;

                int mx = ((sx + 3) >> 2) << 2;
                for (int x2 = mx; x2 < ex; x2 += 4) {
                    if (xAxisFilled.add(x2)) {
                        mx = x2;
                        break;
                    }
                }

                GuiUtil.drawHorizontalLine(x + w, mx, y + 4, colour);
                GuiUtil.drawVerticalLine(mx, y + 4, sy, colour);
                y += fr.FONT_HEIGHT + 2;

                for (String line : info) {
                    fr.drawStringWithShadow(line, x + 7, y, -1);
                    y += fr.FONT_HEIGHT + 2;
                }
                info.clear();
            }
        }
    }

    /** @return True if the {@link #currentMenu} {@link IMenuElement#shouldFullyOverride() fully overrides} other mouse
     *         clicks, false otherwise. */
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
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
            if (element instanceof IInteractionElement) {
                ((IInteractionElement) element).onMouseClicked(mouseButton);
            }
        }
        return false;
    }

    public void onMouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        mouse.setMousePosition(mouseX, mouseY);

        IMenuElement m = currentMenu;
        if (m != null) {
            m.onMouseDragged(clickedMouseButton, timeSinceLastClick);
            if (m.shouldFullyOverride()) {
                return;
            }
        }

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
                ((IInteractionElement) element).onMouseDragged(clickedMouseButton, timeSinceLastClick);
            }
        }
    }

    public void onMouseReleased(int mouseX, int mouseY, int state) {
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

    public boolean onKeyTyped(char typedChar, int keyCode) {
        boolean action = false;
        IMenuElement m = currentMenu;
        if (m != null) {
            action = m.onKeyPress(typedChar, keyCode);
            if (action && m.shouldFullyOverride()) {
                return true;
            }
        }

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
                action |= ((IInteractionElement) element).onKeyPress(typedChar, keyCode);
            }
        }
        return action;
    }
}

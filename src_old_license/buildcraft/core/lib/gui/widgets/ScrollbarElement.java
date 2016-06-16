package buildcraft.core.lib.gui.widgets;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.utils.MathUtils;
import buildcraft.lib.gui.*;

@SideOnly(Side.CLIENT)
public class ScrollbarElement<G extends GuiBC8<C>, C extends ContainerBC_Neptune> extends GuiElementSimple<G, C> {
    private static final int HEIGHT = 14;
    private final GuiIcon background, scroller;
    private int pos, len;
    private boolean isClicking;

    public ScrollbarElement(G gui, IPositionedElement parent, int height, GuiIcon background, GuiIcon scroller) {
        super(gui, parent, new GuiRectangle(0, 0, 6, height));
        this.background = background;
        this.scroller = scroller;
    }

    @Override
    public void drawBackground() {
        if (len > 0) {
            background.drawAt(this);
            scroller.drawAt(this.offset(0, pos * (getHeight() - HEIGHT + 2) / len));
        }
    }

    private void updatePositionFromMouse() {
        int h = getHeight();
        setPosition(((gui.mouse.getY() - getY()) * len + (h / 2)) / h);
    }

    /** This is called EVEN IF the mouse is not inside your width and height! */
    @Override
    public void onMouseClicked(int button) {
        if (contains(gui.mouse)) {
            if (button == 0) {
                isClicking = true;
                updatePositionFromMouse();
            }
        }
    }

    /** This is called EVEN IF the mouse is not inside your width and height! */
    @Override
    public void onMouseDragged(int button, long ticksSinceClick) {
        if (isClicking && button == 0) {
            updatePositionFromMouse();
        }
    }

    /** This is called EVEN IF the mouse is not inside your width and height! */
    @Override
    public void onMouseReleased(int button) {
        if (isClicking && button == 0) {
            updatePositionFromMouse();
            isClicking = false;
        }
    }

    public int getPosition() {
        return pos;
    }

    public void setPosition(int pos) {
        this.pos = MathUtils.clamp(pos, 0, len);
    }

    public void setLength(int len) {
        this.len = len;
        setPosition(this.pos);
    }
}

/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */

package buildcraft.core.lib.gui.buttons;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiImageButton extends GuiButton implements IButtonClickEventTrigger {

    public enum ButtonImage {
        BLANK(1, 19),
        WHITE_LIST(19, 19),
        BLACK_LIST(37, 19),
        ROUND_ROBIN(55, 19);

        private final int u, v;

        ButtonImage(int u, int v) {
            this.u = u;
            this.v = v;
        }

        public int getU() {
            return u;
        }

        public int getV() {
            return v;
        }
    }

    public static final ResourceLocation ICON_BUTTON_TEXTURES = new ResourceLocation("buildcraftcore:textures/gui/icon_button.png");

    public static final int SIZE = 18;

    private ArrayList<IButtonClickEventListener> listeners = new ArrayList<IButtonClickEventListener>();
    private ButtonImage image = ButtonImage.BLANK;
    private boolean active = false;

    public GuiImageButton(int id, int x, int y, ButtonImage image) {
        super(id, x, y, SIZE, SIZE, "");

        this.image = image;
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
    public void drawButton(Minecraft minecraft, int x, int y) {

        if (!visible) {
            return;
        }

        minecraft.renderEngine.bindTexture(ICON_BUTTON_TEXTURES);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int buttonState = getButtonState(x, y);

        drawTexturedModalRect(xPosition, yPosition, buttonState * SIZE, 0, SIZE, SIZE);

        drawTexturedModalRect(xPosition + 1, yPosition + 1, image.getU(), image.getV(), SIZE - 2, SIZE - 2);

        mouseDragged(minecraft, x, y);
    }

    @Override
    public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
        boolean pressed = super.mousePressed(par1Minecraft, par2, par3);

        if (pressed) {
            active = !active;
            notifyAllListeners();
        }

        return pressed;
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
    public void notifyAllListeners() {
        for (IButtonClickEventListener listener : listeners) {
            listener.handleButtonClick(this, this.id);
        }
    }

    private int getButtonState(int mouseX, int mouseY) {
        if (!this.enabled) {
            return 0;
        }

        if (isMouseOverButton(mouseX, mouseY)) {
            if (!this.active) {
                return 2;
            } else {
                return 4;
            }
        }

        if (!this.active) {
            return 1;
        } else {
            return 3;
        }
    }

    private boolean isMouseOverButton(int mouseX, int mouseY) {
        return mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + SIZE && mouseY < yPosition + SIZE;
    }
}

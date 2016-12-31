package buildcraft.lib.gui.elem;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;

public class GuiElementText extends GuiElementSimple<GuiBC8<?>> {
    public boolean dropShadow = false;
    public boolean foreground = true;

    private final Supplier<String> text;
    private final IntSupplier colour;

    public GuiElementText(GuiBC8<?> gui, IGuiPosition parent, Supplier<String> text, IntSupplier colour) {
        super(gui, parent, GuiRectangle.ZERO);
        this.text = text;
        this.colour = colour;
    }

    public GuiElementText(GuiBC8<?> gui, IGuiPosition parent, String text, int colour) {
        this(gui, parent, () -> text, () -> colour);
    }

    public GuiElementText setDropShadow(boolean value) {
        dropShadow = value;
        return this;
    }

    public GuiElementText setForeground(boolean value) {
        foreground = value;
        return this;
    }

    @Override
    public int getWidth() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        return fr.getStringWidth(text.get());
    }

    @Override
    public int getHeight() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        return fr.FONT_HEIGHT;
    }

    @Override
    public void drawBackground(float partialTicks) {
        if (!foreground) {
            draw();
        }
    }

    @Override
    public void drawForeground(float partialTicks) {
        if (foreground) {
            draw();
        }
    }

    private void draw() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString(text.get(), getX(), getY(), colour.getAsInt(), dropShadow);
    }
}

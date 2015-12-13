/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import buildcraft.core.client.CoreIconProvider;

public abstract class AdvancedSlot {

    public int x, y;
    public GuiAdvancedInterface gui;
    public boolean drawBackround = false;

    public AdvancedSlot(GuiAdvancedInterface gui, int x, int y) {
        this.x = x;
        this.y = y;
        this.gui = gui;
    }

    public String getDescription() {
        return null;
    }

    public final void drawTooltip(GuiAdvancedInterface gui, int x, int y) {
        String s = StatCollector.translateToLocal(getDescription());

        if (s != null) {
            gui.drawTooltip(s, x, y);
        } else {
            ItemStack stack = getItemStack();

            if (stack != null) {
                int cornerX = (gui.width - gui.getXSize()) / 2;
                int cornerY = (gui.height - gui.getYSize()) / 2;

                int xS = x - cornerX;
                int yS = y - cornerY;

                gui.renderToolTip(stack, xS, yS);
            }
        }
    }

    public TextureAtlasSprite getIcon() {
        return null;
    }

    public ResourceLocation getTexture() {
        return TextureMap.locationBlocksTexture;
    }

    public ItemStack getItemStack() {
        return null;
    }

    public boolean isDefined() {
        return true;
    }

    public void selected() {}

    public void drawSprite(int cornerX, int cornerY) {
        Minecraft mc = Minecraft.getMinecraft();

        if (drawBackround) {
            mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            gui.drawTexturedModalRect(cornerX + x - 12, cornerY + y - 12, CoreIconProvider.SLOT.getSprite(), 32, 32);
            // gui.drawTexturedModalRect(cornerX + x - 1, cornerY + y - 1, 0, 0, 18, 18);
        }

        if (!isDefined()) {
            return;
        }

        if (getItemStack() != null) {
            GlStateManager.color(1, 1, 1, 1);
            drawStack(getItemStack());
            GL11.glDisable(GL11.GL_LIGHTING); // Make sure that render states are reset, an ItemStack can derp them up.
            GlStateManager.disableLighting();
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        } else if (getIcon() != null) {
            mc.renderEngine.bindTexture(getTexture());
            // System.out.printf("Drawing advanced sprite %s (%d,%d) at %d %d\n", getIcon().getIconName(),
            // getIcon().getOriginX(),getIcon().getOriginY(),cornerX + x, cornerY + y);

            GL11.glPushAttrib(GL11.GL_LIGHTING_BIT | GL11.GL_COLOR_BUFFER_BIT);

            gui.drawTexturedModalRect(cornerX + x, cornerY + y, getIcon(), 16, 16);

            GL11.glPopAttrib();
        }

    }

    public void drawStack(ItemStack item) {
        int cornerX = (gui.width - gui.getXSize()) / 2;
        int cornerY = (gui.height - gui.getYSize()) / 2;

        RenderHelper.enableGUIStandardItemLighting();

        gui.drawStack(item, cornerX + x, cornerY + y);
        GlStateManager.color(1, 1, 1, 1);
        System.out.println("hi");
    }

    public boolean shouldDrawHighlight() {
        return true;
    }
}

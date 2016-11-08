/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.gui;

import java.io.IOException;
import java.util.Collection;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.core.lib.client.render.FluidRenderer;
import buildcraft.core.lib.client.render.FluidRenderer.FluidType;
import buildcraft.core.lib.gui.tooltips.IToolTipProvider;
import buildcraft.core.lib.gui.tooltips.ToolTipLine;
import buildcraft.core.lib.gui.widgets.Widget;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.Widget_Neptune;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.slot.IPhantomSlot;
import buildcraft.lib.misc.RenderUtil;

// TODO: Rewrite all methods from this into GuiBC8
@Deprecated
public abstract class GuiBuildCraft extends GuiContainer {

    public static final ResourceLocation LEDGER_TEXTURE = new ResourceLocation("buildcraftcore:textures/gui/ledger.png");
    public final LedgerManager ledgerManager = new LedgerManager(this);
    public final TileEntity tile;
    public final ContainerBC_Neptune container;
    public ResourceLocation texture;

    public GuiBuildCraft(ContainerBC_Neptune container, IInventory inventory, ResourceLocation texture) {
        super(container);
        this.container = container;

        this.texture = texture;

        if (inventory instanceof TileEntity) {
            tile = (TileEntity) inventory;
        } else {
            tile = null;
        }

        initLedgers(inventory);
    }

    public FontRenderer getFontRenderer() {
        return fontRendererObj;
    }

    protected void initLedgers(IInventory inventory) {}

    // Protected/private methods made public
    public int xSize() {
        return xSize;
    }

    public int ySize() {
        return ySize;
    }

    @Override
    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        super.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    @Override
    public void drawTexturedModalRect(int xCoord, int yCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn) {
        super.drawTexturedModalRect(xCoord, yCoord, textureSprite != null ? textureSprite : Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite(), widthIn, heightIn);
    }

    /** Draws the screen and all the components in it. */
    @Override
    public void drawScreen(int mouseX, int mouseY, float par3) {
        super.drawScreen(mouseX, mouseY, par3);
        int left = this.guiLeft;
        int top = this.guiTop;

        GlStateManager.disableDepth();
        GL11.glPushMatrix();
        GL11.glTranslatef(left, top, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();

        InventoryPlayer playerInv = this.mc.thePlayer.inventory;

        if (playerInv.getItemStack() == null) {
            drawToolTips(container.getWidgets(), mouseX - left, mouseY - top, left, top);
            drawToolTips(buttonList, mouseX, mouseY, 0, 0);
            drawToolTips(inventorySlots.inventorySlots, mouseX, mouseY, 0, 0);
        }

        GL11.glPopMatrix();
        GlStateManager.enableDepth();
    }

    private void drawToolTips(Collection<?> objects, int mouseX, int mouseY, int offsetX, int offsetY) {
        for (Object obj : objects) {
            if (!(obj instanceof IToolTipProvider)) {
                continue;
            }
            IToolTipProvider provider = (IToolTipProvider) obj;
            if (!provider.isToolTipVisible()) {
                continue;
            }
            ToolTip tips = provider.getToolTip();
            if (tips == null) {
                continue;
            }
            boolean mouseOver = provider.isMouseOver(mouseX, mouseY);
            tips.onTick(mouseOver);
            if (mouseOver && tips.isReady()) {
                tips.refresh();
                drawToolTips(tips, mouseX + offsetX, mouseY + offsetY);
            }
        }
    }

    /** Draws a fluid into the gui. This will automatically cut and/or repeat the fluid sprite to ensure that it can
     * fill up any sized tank while keeping the sprite the correct size and scale. */
    public void drawFluid(FluidStack fluid, int x, int y, int width, int height, int maxCapacity) {
        if (fluid == null || fluid.getFluid() == null) {
            return;
        }

        TextureAtlasSprite sprite = FluidRenderer.getFluidTexture(fluid.getFluid(), FluidType.STILL);

        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderUtil.setGLColorFromInt(fluid.getFluid().getColor(fluid));
        int fullX = width / 16;
        int fullY = height / 16;
        int lastX = width - fullX * 16;
        int lastY = height - fullY * 16;
        int level = fluid.amount * height / maxCapacity;
        int fullLvl = (height - level) / 16;
        int lastLvl = (height - level) - fullLvl * 16;
        for (int i = 0; i < fullX; i++) {
            for (int j = 0; j < fullY; j++) {
                if (j >= fullLvl) {
                    drawCutIcon(sprite, x + i * 16, y + j * 16, 16, 16, j == fullLvl ? lastLvl : 0);
                }
            }
        }
        for (int i = 0; i < fullX; i++) {
            drawCutIcon(sprite, x + i * 16, y + fullY * 16, 16, lastY, fullLvl == fullY ? lastLvl : 0);
        }
        for (int i = 0; i < fullY; i++) {
            if (i >= fullLvl) {
                drawCutIcon(sprite, x + fullX * 16, y + i * 16, lastX, 16, i == fullLvl ? lastLvl : 0);
            }
        }
        drawCutIcon(sprite, x + fullX * 16, y + fullY * 16, lastX, lastY, fullLvl == fullY ? lastLvl : 0);
        GlStateManager.color(1, 1, 1, 1);
    }

    // The magic is here
    private void drawCutIcon(TextureAtlasSprite icon, int x, int y, int width, int height, int cut) {
        Tessellator tess = Tessellator.getInstance();
        VertexBuffer vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vertexUV(vb, x, y + height, zLevel, icon.getMinU(), icon.getInterpolatedV(height));
        vertexUV(vb, x + width, y + height, zLevel, icon.getInterpolatedU(width), icon.getInterpolatedV(height));
        vertexUV(vb, x + width, y + cut, zLevel, icon.getInterpolatedU(width), icon.getInterpolatedV(cut));
        vertexUV(vb, x, y + cut, zLevel, icon.getMinU(), icon.getInterpolatedV(cut));
        tess.draw();
    }

    private static void vertexUV(VertexBuffer vb, double x, double y, double z, double u, double v) {
        vb.pos(x, y, z).tex(u, v).endVertex();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int mX = mouseX - guiLeft;
        int mY = mouseY - guiTop;

        drawWidgets(mX, mY);
    }

    protected void drawWidgets(int mX, int mY) {
        for (Widget_Neptune widget : container.getWidgets()) {
            if (widget.hidden) {
                continue;
            }
            bindTexture(texture);
            widget.draw(this, guiLeft, guiTop, mX, mY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        drawLedgers(par1, par2);
    }

    protected void drawLedgers(int x, int y) {
        ledgerManager.drawLedgers(x, y);
    }

    public void drawCenteredString(String string, int xCenter, int yCenter, int textColor) {
        int x = xCenter - fontRendererObj.getStringWidth(string) / 2;
        int y = yCenter - fontRendererObj.FONT_HEIGHT / 2;
        fontRendererObj.drawString(string, x, y, textColor);
        // Reset the colour afterwards as drawString leaves it at the last colour drawn
        GlStateManager.color(1, 1, 1, 1);
    }

    protected int getCenteredOffset(String string) {
        return getCenteredOffset(string, xSize);
    }

    protected int getCenteredOffset(String string, int xWidth) {
        return (xWidth - fontRendererObj.getStringWidth(string)) / 2;
    }

    /** Returns if the passed mouse position is over the specified slot. */
    private boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
        int left = this.guiLeft;
        int top = this.guiTop;
        int realMouseX = mouseX - left;
        int realMouseY = mouseY - top;
        return realMouseX >= slot.xDisplayPosition - 1 && realMouseX < slot.xDisplayPosition + 16 + 1 && realMouseY >= slot.yDisplayPosition - 1 && realMouseY < slot.yDisplayPosition + 16 + 1;
    }

    // / MOUSE CLICKS
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int mX = mouseX - guiLeft;
        int mY = mouseY - guiTop;

        for (Widget widget : container.getWidgets()) {
            if (widget.hidden) {
                continue;
            } else if (!widget.isMouseOver(mX, mY)) {
                continue;
            } else if (widget.handleMouseClick(mX, mY, mouseButton)) {
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // / Handle ledger clicks
        ledgerManager.handleMouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long time) {
        int mX = mouseX - guiLeft;
        int mY = mouseY - guiTop;
        for (Widget widget : container.getWidgets()) {
            if (widget.hidden) {
                continue;
            }
            widget.handleMouseMove(mX, mY, mouseButton, time);
        }

        Slot slot = getSlotAtPosition(mouseX, mouseY);
        if (mouseButton == 1 && slot instanceof IPhantomSlot) {
            return;
        }
        super.mouseClickMove(mouseX, mouseY, mouseButton, time);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int eventType) {
        super.mouseReleased(mouseX, mouseY, eventType);

        int mX = mouseX - guiLeft;
        int mY = mouseY - guiTop;
        for (Widget widget : container.getWidgets()) {
            if (widget.hidden) {
                continue;
            }
            widget.handleMouseRelease(mX, mY, eventType);
        }
    }

    public Slot getSlotAtPosition(int x, int y) {
        for (int slotIndex = 0; slotIndex < this.inventorySlots.inventorySlots.size(); ++slotIndex) {
            Slot slot = (Slot) this.inventorySlots.inventorySlots.get(slotIndex);
            if (isMouseOverSlot(slot, x, y)) {
                return slot;
            }
        }
        return null;
    }

    public static void bindTexture(ResourceLocation texture) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
    }

    private void drawToolTips(ToolTip toolTips, int mouseX, int mouseY) {
        if (toolTips.size() > 0) {
            int left = this.guiLeft;
            int top = this.guiTop;
            int length = 0;
            int x;
            int y;

            for (ToolTipLine tip : toolTips) {
                y = this.fontRendererObj.getStringWidth(tip.text);

                if (y > length) {
                    length = y;
                }
            }

            x = mouseX - left + 12;
            y = mouseY - top - 12;
            int var14 = 8;

            if (toolTips.size() > 1) {
                var14 += 2 + (toolTips.size() - 1) * 10;
            }
            this.zLevel = 300.0F;
            itemRender.zLevel = 300.0F;
            int var15 = -267386864;
            this.drawGradientRect(x - 3, y - 4, x + length + 3, y - 3, var15, var15);
            this.drawGradientRect(x - 3, y + var14 + 3, x + length + 3, y + var14 + 4, var15, var15);
            this.drawGradientRect(x - 3, y - 3, x + length + 3, y + var14 + 3, var15, var15);
            this.drawGradientRect(x - 4, y - 3, x - 3, y + var14 + 3, var15, var15);
            this.drawGradientRect(x + length + 3, y - 3, x + length + 4, y + var14 + 3, var15, var15);
            int var16 = 1347420415;
            int var17 = (var16 & 16711422) >> 1 | var16 & -16777216;
            this.drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + var14 + 3 - 1, var16, var17);
            this.drawGradientRect(x + length + 2, y - 3 + 1, x + length + 3, y + var14 + 3 - 1, var16, var17);
            this.drawGradientRect(x - 3, y - 3, x + length + 3, y - 3 + 1, var16, var16);
            this.drawGradientRect(x - 3, y + var14 + 2, x + length + 3, y + var14 + 3, var17, var17);

            for (ToolTipLine tip : toolTips) {
                String line = tip.text;

                if (tip.color == -1) {
                    line = "\u00a77" + line;
                } else {
                    line = "\u00a7" + Integer.toHexString(tip.color) + line;
                }

                this.fontRendererObj.drawStringWithShadow(line, x, y, -1);

                y += 10 + tip.getSpacing();
            }

            this.zLevel = 0.0F;
            itemRender.zLevel = 0.0F;
        }
    }

    public ContainerBC_Neptune getContainer() {
        return container;
    }
}

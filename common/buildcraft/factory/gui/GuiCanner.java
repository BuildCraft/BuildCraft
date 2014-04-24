package buildcraft.factory.gui;

import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.render.RenderUtils;
import buildcraft.factory.TileCanner;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class GuiCanner extends GuiBuildCraft {

    TileCanner canner;
    private static final ResourceLocation texture = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/FluidicCompressorGUI.png");
    private static final ResourceLocation BLOCK_TEXTURE = TextureMap.locationBlocksTexture;

    public GuiCanner(InventoryPlayer inventoryplayer, TileCanner canner) {
        super(new ContainerCanner(inventoryplayer, canner), canner, texture);
        this.canner = canner;
    }
    
    @Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton){
		int mX = mouseX - guiLeft;
		int mY = mouseY - guiTop;
		if (mX >= 20 && mX <= 39 && mY >= 25 && mY <= 41 && canner.fill){
			canner.fill = false;
		}
		if (mX >= 20 && mX <= 39 && mY >= 45 && mY <= 61 && !canner.fill){
			canner.fill = true;
		}
		canner.sendModeUpdatePacket();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        int j = (width - xSize) / 2;
        int k = (height - ySize) / 2;
        drawFluid(canner.getFluid(), canner.getScaledLiquid(52), j+52, k+16, 16, 52);
        mc.renderEngine.bindTexture(texture);
        drawTexturedModalRect(j + 52, k + 21, 176, 21, 16, 58);
        
        if (canner.fill){
        	drawTexturedModalRect(j+20, k+25, 195, 83, 19, 16);
        	drawTexturedModalRect(j+20, k+45, 176, 99, 19, 16);
        } else {
        	drawTexturedModalRect(j+20, k+45, 195, 99, 19, 16);
        	drawTexturedModalRect(j+20, k+25, 176, 83, 19, 16);
        	
        }
        drawTexturedModalRect(j+89, k+53, 176, 3, canner.getProgress(), 4);
        
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);
    }

    private void drawFluid(FluidStack fluid, int level, int x, int y, int width, int height){
        if(fluid == null || fluid.getFluid() == null) {
            return;
        }
        IIcon icon = fluid.getFluid().getIcon(fluid);
        mc.renderEngine.bindTexture(BLOCK_TEXTURE);
        RenderUtils.setGLColorFromInt(fluid.getFluid().getColor(fluid));
        int fullX = width / 16;
        int fullY = height / 16;
        int lastX = width - fullX * 16;
        int lastY = height - fullY * 16;
        int fullLvl = (height - level) / 16;
        int lastLvl = (height - level) - fullLvl * 16;
        for(int i = 0; i < fullX; i++) {
            for(int j = 0; j < fullY; j++) {
                if(j >= fullLvl) {
                    drawCutIcon(icon, x + i * 16, y + j * 16, 16, 16, j == fullLvl ? lastLvl : 0);
                }
            }
        }
        for(int i = 0; i < fullX; i++) {
            drawCutIcon(icon, x + i * 16, y + fullY * 16, 16, lastY, fullLvl == fullY ? lastLvl : 0);
        }
        for(int i = 0; i < fullY; i++) {
            if(i >= fullLvl) {
                drawCutIcon(icon, x + fullX * 16, y + i * 16, lastX, 16, i == fullLvl ? lastLvl : 0);
            }
        }
        drawCutIcon(icon, x + fullX * 16, y + fullY * 16, lastX, lastY, fullLvl == fullY ? lastLvl : 0);
    }

    private void drawCutIcon(IIcon icon, int x, int y, int width, int height, int cut){
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x, y + height, zLevel, icon.getMinU(), icon.getInterpolatedV(height));
        tess.addVertexWithUV(x + width, y + height, zLevel, icon.getInterpolatedU(width), icon.getInterpolatedV(height));
        tess.addVertexWithUV(x + width, y + cut, zLevel, icon.getInterpolatedU(width), icon.getInterpolatedV(cut));
        tess.addVertexWithUV(x, y + cut, zLevel, icon.getMinU(), icon.getInterpolatedV(cut));
        tess.draw();
    }
}

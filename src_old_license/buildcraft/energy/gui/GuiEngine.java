/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftCore;
import buildcraft.core.client.CoreIconProvider;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.Ledger;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.misc.StringUtilBC;

public abstract class GuiEngine extends GuiBC8<ContainerEngine> {

    private static final ResourceLocation TEXTURES = TextureMap.LOCATION_BLOCKS_TEXTURE;

    protected class EngineLedger extends Ledger {

        TileEngineBase engine;
        int headerColour = 0xe1c92f;
        int subheaderColour = 0xaaafb8;
        int textColour = 0x000000;

        public EngineLedger(TileEngineBase engine) {
            super(GuiEngine.this);
            this.engine = engine;
            maxHeight = 94;
            overlayColor = 0xd46c1f;
        }

        @Override
        public void draw(int x, int y) {

            // Draw background
            drawBackground(x, y);

            // Draw icon
            Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURES);
            drawIcon(CoreIconProvider.ENERGY.getSprite(), x + 3, y + 4);

            if (!isFullyOpened()) {
                return;
            }

            fontRendererObj.drawStringWithShadow(StringUtilBC.localize("gui.energy"), x + 22, y + 8, headerColour);
            fontRendererObj.drawStringWithShadow(StringUtilBC.localize("gui.currentOutput") + ":", x + 22, y + 20, subheaderColour);
            fontRendererObj.drawString(String.format("%d RF/t", engine.currentOutput), x + 22, y + 32, textColour);
            fontRendererObj.drawStringWithShadow(StringUtilBC.localize("gui.stored") + ":", x + 22, y + 44, subheaderColour);
            fontRendererObj.drawString(String.format("%d RF", engine.getEnergyStored()), x + 22, y + 56, textColour);
            fontRendererObj.drawStringWithShadow(StringUtilBC.localize("gui.heat") + ":", x + 22, y + 68, subheaderColour);
            fontRendererObj.drawString(String.format("%.2f \u00B0C", engine.getCurrentHeatValue()), x + 22, y + 80, textColour);

        }

        @Override
        public String getTooltip() {
            return String.format("%d RF/t", engine.currentOutput);
        }
    }

    public GuiEngine(ContainerEngine container) {
        super(container);
    }

    @Override
    protected void initLedgers(IInventory inventory) {
        super.initLedgers(inventory);
        if (!BuildCraftCore.hidePowerNumbers) {
            ledgerManager.add(new EngineLedger((TileEngineBase) tile));
        }
    }
}

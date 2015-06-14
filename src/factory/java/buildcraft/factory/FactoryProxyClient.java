/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import java.lang.reflect.Method;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.EntityBlock;
import buildcraft.factory.gui.GuiAutoCrafting;
import buildcraft.factory.render.RenderHopper;
import buildcraft.factory.render.RenderRefinery;
import buildcraft.factory.render.RenderTank;
import buildcraft.factory.tile.TileHopper;
import buildcraft.factory.tile.TileRefinery;
import buildcraft.factory.tile.TileTank;

public class FactoryProxyClient extends FactoryProxy {
    public static TextureAtlasSprite pumpTexture;

    @Override
    public void initializeTileEntities() {
        super.initializeTileEntities();

        if (BuildCraftFactory.tankBlock != null) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new RenderTank());
        }

        if (BuildCraftFactory.refineryBlock != null) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, new RenderRefinery());
            RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftFactory.refineryBlock, 0), new RenderRefinery());
        }

        if (BuildCraftFactory.hopperBlock != null) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileHopper.class, new RenderHopper());
            RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftFactory.hopperBlock, 0), new RenderHopper());
        }
    }

    @Override
    public void initializeEntityRenders() {}

    @Override
    public void initializeNEIIntegration() {
        try {
            Class<?> neiRenderer = Class.forName("codechicken.nei.DefaultOverlayRenderer");
            Method method = neiRenderer.getMethod("registerGuiOverlay", Class.class, String.class, int.class, int.class);
            method.invoke(null, GuiAutoCrafting.class, "crafting", 5, 11);
            BCLog.logger.debug("NEI detected, adding NEI overlay");
        } catch (Exception e) {
            BCLog.logger.debug("NEI not detected.");
        }
    }

    @Override
    public EntityBlock newPumpTube(World w) {
        EntityBlock eb = super.newPumpTube(w);
        eb.texture = pumpTexture;
        return eb;
    }
}

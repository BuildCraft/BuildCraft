/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import buildcraft.BuildCraftFactory;
import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.factory.render.RenderRefinery;
import buildcraft.factory.render.RenderTank;

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
        }
    }

    @Override
    public void initializeEntityRenders() {
        ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(BuildCraftFactory.refineryBlock), 0, TileRefinery.class);
    }

    @Override
    public EntityResizableCuboid newPumpTube(World w) {
        EntityResizableCuboid eb = super.newPumpTube(w);
        eb.texture = pumpTexture;
        return eb;
    }
}

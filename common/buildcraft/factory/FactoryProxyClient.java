/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import buildcraft.BuildCraftFactory;
import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.factory.render.RenderRefinery;
import buildcraft.factory.render.RenderTank;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class FactoryProxyClient extends FactoryProxy {
    public static TextureAtlasSprite pumpTexture;

    @Override
    public void preInit() {
        super.preInit();

        if (BuildCraftFactory.refineryBlock != null) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, new RenderRefinery());
            ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(BuildCraftFactory.refineryBlock), 0, TileRefinery.class);
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BuildCraftFactory.refineryBlock), 0, new ModelResourceLocation("buildcraftfactory:refineryBlock", "inventory"));
        }
    }

    @Override
    public void fmlInit() {
        super.fmlInit();

        if (BuildCraftFactory.tankBlock != null) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new RenderTank());
        }

        if (BuildCraftFactory.refineryBlock != null) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, new RenderRefinery());
         }

        OBJLoader.instance.addDomain("buildcraftfactory");
    }

    @Override
    public EntityResizableCuboid newPumpTube(World w) {
        EntityResizableCuboid eb = super.newPumpTube(w);
        eb.texture = pumpTexture;
        return eb;
    }
}

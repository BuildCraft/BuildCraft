/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.silicon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import buildcraft.BuildCraftSilicon;
import buildcraft.silicon.render.RenderLaserTile;

public class SiliconProxyClient extends SiliconProxy {
    @Override
    public void registerRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, new RenderLaserTile());

        RenderingRegistry.registerEntityRenderingHandler(EntityPackage.class, new RenderSnowball(Minecraft.getMinecraft().getRenderManager(),
                BuildCraftSilicon.packageItem, Minecraft.getMinecraft().getRenderItem()));
    }
}

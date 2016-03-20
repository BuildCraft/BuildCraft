/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.silicon;

import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import buildcraft.silicon.render.RenderLaserTile;

public class SiliconProxyClient extends SiliconProxy {
    @Override
    public void preInit() {
        OBJLoader.instance.addDomain("buildcraftsilicon");
    }

    @Override
    public void registerRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, new RenderLaserTile());
    }
}

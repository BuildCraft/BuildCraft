/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.SidedProxy;

import buildcraft.core.lib.EntityResizableCuboid;

public class BuilderProxy {
    @SidedProxy(clientSide = "buildcraft.builders.BuilderProxyClient", serverSide = "buildcraft.builders.BuilderProxy")
    public static BuilderProxy proxy;

    public void registerClientHook() {

    }

    public void registerBlockRenderers() {

    }

    public EntityResizableCuboid newDrill(World w, double i, double j, double k, double l, double d, double e) {
        return new EntityResizableCuboid(w, i, j, k, l, d, e);
    }

    public EntityResizableCuboid newDrillHead(World w, double i, double j, double k, double l, double d, double e) {
        return new EntityResizableCuboid(w, i, j, k, l, d, e);
    }
}

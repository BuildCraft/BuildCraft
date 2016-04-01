/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.core.CompatHooks;
import buildcraft.transport.pipes.bc8.TilePipe_BC8;
import buildcraft.transport.transformer.TileTransformer;

public class TransportProxy {
    @SidedProxy(clientSide = "buildcraft.transport.TransportProxyClient", serverSide = "buildcraft.transport.TransportProxy")
    public static TransportProxy proxy;
    public static int pipeModel = -1;

    public void registerTileEntities() {
        // The first name here is the current TE name; the remaining names are old names used for backwards
        // compatibility
        GameRegistry.registerTileEntityWithAlternatives(CompatHooks.INSTANCE.getTile(TileGenericPipe.class),
                "net.minecraft.src.buildcraft.transport.GenericPipe", "net.minecraft.src.buildcraft.GenericPipe",
                "net.minecraft.src.buildcraft.transport.TileGenericPipe");
        GameRegistry.registerTileEntity(CompatHooks.INSTANCE.getTile(TileFilteredBuffer.class),
                "net.minecraft.src.buildcraft.transport.TileFilteredBuffer");

        GameRegistry.registerTileEntity(CompatHooks.INSTANCE.getTile(TileTransformer.class), "buildcraft.transport.transformer.TileTransformer");

        GameRegistry.registerTileEntity(TilePipe_BC8.class, "buildcraft.transport.pipes.TilePipe");
    }

    public void registerRenderers() {}

    public void setIconProviderFromPipe(ItemPipe item, Pipe<?> dummyPipe) {}

    public void obsidianPipePickup(World world, EntityItem item, TileEntity tile) {}

    public void clearDisplayList(int displayList) {}

    public void preInit() {
    }
}

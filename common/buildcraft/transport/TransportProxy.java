/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.BCCompatHooks;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.BCLog;
import buildcraft.core.utils.Utils;

public class TransportProxy {

	@SidedProxy(clientSide = "buildcraft.transport.TransportProxyClient", serverSide = "buildcraft.transport.TransportProxy")
	public static TransportProxy proxy;
	public static int pipeModel = -1;

	public void registerTileEntities() {
        // The first name here is the current TE name; the remaining names are old names used for backwards compatibility
		GameRegistry.registerTileEntityWithAlternatives(BCCompatHooks.getPipeTile(), "net.minecraft.src.buildcraft.transport.GenericPipe", "net.minecraft.src.buildcraft.GenericPipe", "net.minecraft.src.buildcraft.transport.TileGenericPipe");
		GameRegistry.registerTileEntity(TileFilteredBuffer.class, "net.minecraft.src.buildcraft.transport.TileFilteredBuffer");
	}

	public void registerRenderers() {
		for (Object o : Item.itemRegistry) {
			if (o instanceof ItemPipe) {
				BCLog.logger.info("Registering model for " + ((Item) o).getUnlocalizedName());
				final ModelResourceLocation loc = new ModelResourceLocation(Utils.getItemName((Item) o), null);
				Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register((Item) o, new ItemMeshDefinition() {
					public ModelResourceLocation getModelLocation(ItemStack stack) {
						return loc;
					}
				});
			}
		}
	}

	public void initIconProviders(BuildCraftTransport instance){
	}

	public void setIconProviderFromPipe(ItemPipe item, Pipe<?> dummyPipe) {
	}
}

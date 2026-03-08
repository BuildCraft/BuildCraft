package buildcraft.factory;

import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.factory.container.ContainerChute;
import buildcraft.factory.container.ContainerTank;
import buildcraft.factory.gui.GuiAutoCraftItems;
import buildcraft.factory.gui.GuiChute;
import buildcraft.factory.gui.GuiTank;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileTank;
import buildcraft.lib.misc.MessageUtil;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class BCFactoryMenuTypes {
    public static final ContainerType<ContainerChute> CHUTE = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileChute) {
                    TileChute tile = (TileChute) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerChute(BCFactoryMenuTypes.CHUTE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerAutoCraftItems> AUTO_WORKBENCH_ITEMS = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileAutoWorkbenchItems) {
                    TileAutoWorkbenchItems tile = (TileAutoWorkbenchItems) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerAutoCraftItems(BCFactoryMenuTypes.AUTO_WORKBENCH_ITEMS, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerTank> TANK = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileTank) {
                    TileTank tile = (TileTank) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerTank(BCFactoryMenuTypes.TANK, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );

    public static void registerAll(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().registerAll(
                CHUTE.setRegistryName("chute"),
                AUTO_WORKBENCH_ITEMS.setRegistryName("auto_workbench_items"),
                TANK.setRegistryName("tank")
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ScreenManager.register(CHUTE, GuiChute::new);
            ScreenManager.register(AUTO_WORKBENCH_ITEMS, GuiAutoCraftItems::new);
            ScreenManager.register(TANK, GuiTank::new);
        }
    }
}

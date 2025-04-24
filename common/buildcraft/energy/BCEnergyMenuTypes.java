package buildcraft.energy;

import buildcraft.energy.client.gui.GuiDynamoMJ;
import buildcraft.energy.client.gui.GuiEngineIron_BC8;
import buildcraft.energy.client.gui.GuiEngineRF;
import buildcraft.energy.client.gui.GuiEngineStone_BC8;
import buildcraft.energy.container.ContainerDynamoMJ;
import buildcraft.energy.container.ContainerEngineIron_BC8;
import buildcraft.energy.container.ContainerEngineRF;
import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.misc.MessageUtil;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class BCEnergyMenuTypes {
    public static final MenuType<ContainerEngineIron_BC8> ENGINE_IRON = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level.getBlockEntity(data.readBlockPos()) instanceof TileEngineIron_BC8 tile) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerEngineIron_BC8(BCEnergyMenuTypes.ENGINE_IRON, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final MenuType<ContainerEngineStone_BC8> ENGINE_STONE = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level.getBlockEntity(data.readBlockPos()) instanceof TileEngineStone_BC8 tile) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerEngineStone_BC8(BCEnergyMenuTypes.ENGINE_STONE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final MenuType<ContainerEngineRF> ENGINE_RF = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level.getBlockEntity(data.readBlockPos()) instanceof TileEngineRF tile) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerEngineRF(BCEnergyMenuTypes.ENGINE_STONE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final MenuType<ContainerDynamoMJ> DYNAMO_MJ = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level.getBlockEntity(data.readBlockPos()) instanceof TileDynamoMJ tile) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerDynamoMJ(BCEnergyMenuTypes.ENGINE_STONE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );

    public static void registerAll(RegistryEvent.Register<MenuType<?>> event) {
        event.getRegistry().registerAll(
                ENGINE_IRON.setRegistryName("engine_iron"),
                ENGINE_STONE.setRegistryName("engine_stone"),
                ENGINE_RF.setRegistryName("engine_rf"),
                DYNAMO_MJ.setRegistryName("dynamo_mj")
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MenuScreens.register(ENGINE_IRON, GuiEngineIron_BC8::new);
            MenuScreens.register(ENGINE_STONE, GuiEngineStone_BC8::new);
            MenuScreens.register(ENGINE_RF, GuiEngineRF::new);
            MenuScreens.register(DYNAMO_MJ, GuiDynamoMJ::new);
        }
    }
}

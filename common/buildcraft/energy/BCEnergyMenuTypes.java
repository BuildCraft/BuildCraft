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
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class BCEnergyMenuTypes {
    public static final ContainerType<ContainerEngineIron_BC8> ENGINE_IRON = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileEngineIron_BC8) {
                    TileEngineIron_BC8 tile = (TileEngineIron_BC8) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerEngineIron_BC8(BCEnergyMenuTypes.ENGINE_IRON, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerEngineStone_BC8> ENGINE_STONE = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileEngineStone_BC8) {
                    TileEngineStone_BC8 tile = (TileEngineStone_BC8) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerEngineStone_BC8(BCEnergyMenuTypes.ENGINE_STONE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerEngineRF> ENGINE_RF = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileEngineRF) {
                    TileEngineRF tile = (TileEngineRF) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerEngineRF(BCEnergyMenuTypes.ENGINE_STONE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerDynamoMJ> DYNAMO_MJ = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileDynamoMJ) {
                    TileDynamoMJ tile = (TileDynamoMJ) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerDynamoMJ(BCEnergyMenuTypes.ENGINE_STONE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );

    public static void registerAll(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().registerAll(
                ENGINE_IRON.setRegistryName("engine_iron"),
                ENGINE_STONE.setRegistryName("engine_stone"),
                ENGINE_RF.setRegistryName("engine_rf"),
                DYNAMO_MJ.setRegistryName("dynamo_mj")
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ScreenManager.register(ENGINE_IRON, GuiEngineIron_BC8::new);
            ScreenManager.register(ENGINE_STONE, GuiEngineStone_BC8::new);
            ScreenManager.register(ENGINE_RF, GuiEngineRF::new);
            ScreenManager.register(DYNAMO_MJ, GuiDynamoMJ::new);
        }
    }
}

package buildcraft.transport;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.transport.container.ContainerDiamondPipe;
import buildcraft.transport.container.ContainerDiamondWoodPipe;
import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.container.ContainerFilteredBuffer_BC8;
import buildcraft.transport.gui.GuiDiamondPipe;
import buildcraft.transport.gui.GuiDiamondWoodPipe;
import buildcraft.transport.gui.GuiEmzuliPipe_BC8;
import buildcraft.transport.gui.GuiFilteredBuffer;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.tile.TileFilteredBuffer;
import buildcraft.transport.tile.TilePipeHolder;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

// Calen: instead of BCTransportProxy in 1.12.2
// For Client
public class BCTransportMenuTypes {
    public static final MenuType<ContainerDiamondPipe> PIPE_DIAMOND = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level().getBlockEntity(data.readBlockPos()) instanceof TilePipeHolder tile && tile.getPipe().behaviour instanceof PipeBehaviourDiamond diamond) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);

                    // Calen 1.18.2: moved from ContainerGate#<init>
                    // Server call in PluggableGate#onPluggableActivate to make MessageUpdateTile
                    tile.onPlayerOpen(inv.player);

                    // Calen: Refresh the PipeBehaviour object
                    diamond = (PipeBehaviourDiamond) tile.getPipe().behaviour;
                    return new ContainerDiamondPipe(BCTransportMenuTypes.PIPE_DIAMOND, windowId, inv.player, diamond);
                } else {
                    return null;
                }
            }
    );
    public static final MenuType<ContainerDiamondWoodPipe> PIPE_DIAMOND_WOOD = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level().getBlockEntity(data.readBlockPos()) instanceof TilePipeHolder tile && tile.getPipe().behaviour instanceof PipeBehaviourWoodDiamond woodDiamond) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);

                    // Calen 1.18.2: moved from ContainerGate#<init>
                    // Server call in PluggableGate#onPluggableActivate to make MessageUpdateTile
                    tile.onPlayerOpen(inv.player);

                    // Calen: Refresh the PipeBehaviour object
                    woodDiamond = (PipeBehaviourWoodDiamond) tile.getPipe().behaviour;
                    return new ContainerDiamondWoodPipe(BCTransportMenuTypes.PIPE_DIAMOND_WOOD, windowId, inv.player, woodDiamond);
                } else {
                    return null;
                }
            }
    );
    public static final MenuType<ContainerEmzuliPipe_BC8> PIPE_EMZULI = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level().getBlockEntity(data.readBlockPos()) instanceof TilePipeHolder tile && tile.getPipe().behaviour instanceof PipeBehaviourEmzuli emzuli) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);

                    // Calen 1.18.2: moved from ContainerGate#<init>
                    // Server call in PluggableGate#onPluggableActivate to make MessageUpdateTile
                    tile.onPlayerOpen(inv.player);

                    // Calen: Refresh the PipeBehaviour object
                    emzuli = (PipeBehaviourEmzuli) tile.getPipe().behaviour;
                    return new ContainerEmzuliPipe_BC8(BCTransportMenuTypes.PIPE_EMZULI, windowId, inv.player, emzuli);
                } else {
                    return null;
                }
            }
    );
    public static final MenuType<ContainerFilteredBuffer_BC8> FILTERED_BUFFER = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level().getBlockEntity(data.readBlockPos()) instanceof TileFilteredBuffer tile) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerFilteredBuffer_BC8(BCTransportMenuTypes.FILTERED_BUFFER, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );

    public static void registerAll() {
        ForgeRegistries.MENU_TYPES.register("pipe_diamond", PIPE_DIAMOND);
        ForgeRegistries.MENU_TYPES.register("pipe_diamond_wood", PIPE_DIAMOND_WOOD);
        ForgeRegistries.MENU_TYPES.register("pipe_emzuli", PIPE_EMZULI);
        ForgeRegistries.MENU_TYPES.register("filtered_buffer", FILTERED_BUFFER);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MenuScreens.register(PIPE_DIAMOND, GuiDiamondPipe::new);
            MenuScreens.register(PIPE_DIAMOND_WOOD, GuiDiamondWoodPipe::new);
            MenuScreens.register(PIPE_EMZULI, GuiEmzuliPipe_BC8::new);
            MenuScreens.register(FILTERED_BUFFER, GuiFilteredBuffer::new);
        }
    }
}

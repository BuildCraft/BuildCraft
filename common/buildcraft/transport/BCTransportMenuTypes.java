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
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

// Calen: instead of BCTransportProxy in 1.12.2
// For Client
public class BCTransportMenuTypes {
    public static final ContainerType<ContainerDiamondPipe> PIPE_DIAMOND = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TilePipeHolder && ((TilePipeHolder) te).getPipe().behaviour instanceof PipeBehaviourDiamond) {
                    TilePipeHolder tile = (TilePipeHolder) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);

                    // Calen 1.18.2: moved from ContainerGate#<init>
                    // Server call in PluggableGate#onPluggableActivate to make MessageUpdateTile
                    tile.onPlayerOpen(inv.player);

                    // Calen: Refresh the PipeBehaviour object
                    PipeBehaviourDiamond diamond = (PipeBehaviourDiamond) tile.getPipe().behaviour;
                    return new ContainerDiamondPipe(BCTransportMenuTypes.PIPE_DIAMOND, windowId, inv.player, diamond);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerDiamondWoodPipe> PIPE_DIAMOND_WOOD = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TilePipeHolder && ((TilePipeHolder) te).getPipe().behaviour instanceof PipeBehaviourWoodDiamond) {
                    TilePipeHolder tile = (TilePipeHolder) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);

                    // Calen 1.18.2: moved from ContainerGate#<init>
                    // Server call in PluggableGate#onPluggableActivate to make MessageUpdateTile
                    tile.onPlayerOpen(inv.player);

                    // Calen: Refresh the PipeBehaviour object
                    PipeBehaviourWoodDiamond woodDiamond = (PipeBehaviourWoodDiamond) tile.getPipe().behaviour;
                    return new ContainerDiamondWoodPipe(BCTransportMenuTypes.PIPE_DIAMOND_WOOD, windowId, inv.player, woodDiamond);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerEmzuliPipe_BC8> PIPE_EMZULI = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TilePipeHolder && ((TilePipeHolder) te).getPipe().behaviour instanceof PipeBehaviourEmzuli) {
                    TilePipeHolder tile = (TilePipeHolder) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);

                    // Calen 1.18.2: moved from ContainerGate#<init>
                    // Server call in PluggableGate#onPluggableActivate to make MessageUpdateTile
                    tile.onPlayerOpen(inv.player);

                    // Calen: Refresh the PipeBehaviour object
                    PipeBehaviourEmzuli emzuli = (PipeBehaviourEmzuli) tile.getPipe().behaviour;
                    return new ContainerEmzuliPipe_BC8(BCTransportMenuTypes.PIPE_EMZULI, windowId, inv.player, emzuli);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerFilteredBuffer_BC8> FILTERED_BUFFER = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileFilteredBuffer) {
                    TileFilteredBuffer tile = (TileFilteredBuffer) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerFilteredBuffer_BC8(BCTransportMenuTypes.FILTERED_BUFFER, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );

    public static void registerAll(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().registerAll(
                PIPE_DIAMOND.setRegistryName("pipe_diamond"),
                PIPE_DIAMOND_WOOD.setRegistryName("pipe_diamond_wood"),
                PIPE_EMZULI.setRegistryName("pipe_emzuli"),
                FILTERED_BUFFER.setRegistryName("filtered_buffer")
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ScreenManager.register(PIPE_DIAMOND, GuiDiamondPipe::new);
            ScreenManager.register(PIPE_DIAMOND_WOOD, GuiDiamondWoodPipe::new);
            ScreenManager.register(PIPE_EMZULI, GuiEmzuliPipe_BC8::new);
            ScreenManager.register(FILTERED_BUFFER, GuiFilteredBuffer::new);
        }
    }
}

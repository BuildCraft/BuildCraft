package buildcraft.builders;

import buildcraft.builders.container.*;
import buildcraft.builders.gui.*;
import buildcraft.builders.tile.*;
import buildcraft.lib.misc.MessageUtil;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class BCBuildersMenuTypes {
    public static final ContainerType<ContainerBuilder> BUILDER = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileBuilder) {
                    TileBuilder tile = (TileBuilder) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerBuilder(BCBuildersMenuTypes.BUILDER, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerArchitectTable> ARCHITECT_TABLE = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileArchitectTable) {
                    TileArchitectTable tile = (TileArchitectTable) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerArchitectTable(BCBuildersMenuTypes.ARCHITECT_TABLE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerElectronicLibrary> ELECTRONIC_LIBRARY = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileElectronicLibrary) {
                    TileElectronicLibrary tile = (TileElectronicLibrary) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerElectronicLibrary(BCBuildersMenuTypes.ELECTRONIC_LIBRARY, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerReplacer> REPLACER = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileReplacer) {
                    TileReplacer tile = (TileReplacer) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerReplacer(BCBuildersMenuTypes.REPLACER, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerFiller> FILLER = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileFiller) {
                    TileFiller tile = (TileFiller) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerFiller(BCBuildersMenuTypes.FILLER, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerFillerPlanner> FILLER_PLANNER = IForgeContainerType.create((windowId, inv, data) ->
            {
                return new ContainerFillerPlanner(BCBuildersMenuTypes.FILLER_PLANNER, windowId, inv.player);
            }
    );

    public static void registerAll(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().registerAll(
                BUILDER.setRegistryName("builder"),
                ARCHITECT_TABLE.setRegistryName("architect_table"),
                ELECTRONIC_LIBRARY.setRegistryName("electronic_library"),
                REPLACER.setRegistryName("replacer"),
                FILLER.setRegistryName("filler"),
                FILLER_PLANNER.setRegistryName("filler_planner")
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ScreenManager.register(BUILDER, GuiBuilder::new);
            ScreenManager.register(ARCHITECT_TABLE, GuiArchitectTable::new);
            ScreenManager.register(ELECTRONIC_LIBRARY, GuiElectronicLibrary::new);
            ScreenManager.register(REPLACER, GuiReplacer::new);
            ScreenManager.register(FILLER, GuiFiller::new);
            ScreenManager.register(FILLER_PLANNER, GuiFillerPlanner::new);
        }
    }
}

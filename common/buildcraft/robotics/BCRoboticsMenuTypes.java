package buildcraft.robotics;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.robotics.container.ContainerRequester;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.gui.GuiRequester;
import buildcraft.robotics.gui.GuiZonePlanner;
import buildcraft.robotics.tile.TileRequester;
import buildcraft.robotics.tile.TileZonePlanner;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class BCRoboticsMenuTypes {
    public static final ContainerType<ContainerZonePlanner> ZONE_PLANNER = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileZonePlanner) {
                    TileZonePlanner tile = (TileZonePlanner) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerZonePlanner(BCRoboticsMenuTypes.ZONE_PLANNER, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final ContainerType<ContainerRequester> REQUESTER = IForgeContainerType.create((windowId, inv, data) ->
            {
                TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
                if (te instanceof TileRequester) {
                    TileRequester tile = (TileRequester) te;
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerRequester(BCRoboticsMenuTypes.REQUESTER, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );

    public static void registerAll(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().registerAll(
                ZONE_PLANNER.setRegistryName("zone_planner"),
                REQUESTER.setRegistryName("requester")
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ScreenManager.register(ZONE_PLANNER, GuiZonePlanner::new);
            ScreenManager.register(REQUESTER, GuiRequester::new);
        }
    }
}

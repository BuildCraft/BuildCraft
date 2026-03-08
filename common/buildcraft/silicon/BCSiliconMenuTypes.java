package buildcraft.silicon;

import buildcraft.api.net.IMessage;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.silicon.container.*;
import buildcraft.silicon.gate.GateLogic;
import buildcraft.silicon.gui.*;
import buildcraft.silicon.plug.PluggableGate;
import buildcraft.silicon.tile.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** The static fields are used to create GUI on client thread. */
public class BCSiliconMenuTypes {
    public static final MenuType<ContainerAssemblyTable> ASSEMBLY_TABLE = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level.getBlockEntity(data.readBlockPos()) instanceof TileAssemblyTable tile) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerAssemblyTable(BCSiliconMenuTypes.ASSEMBLY_TABLE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final MenuType<ContainerIntegrationTable> INTEGRATION_TABLE = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level.getBlockEntity(data.readBlockPos()) instanceof TileIntegrationTable tile) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerIntegrationTable(BCSiliconMenuTypes.INTEGRATION_TABLE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final MenuType<ContainerAdvancedCraftingTable> ADVANCED_CRAFTING_TABLE = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level.getBlockEntity(data.readBlockPos()) instanceof TileAdvancedCraftingTable tile) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerAdvancedCraftingTable(BCSiliconMenuTypes.INTEGRATION_TABLE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final MenuType<ContainerChargingTable> CHARGING_TABLE = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level.getBlockEntity(data.readBlockPos()) instanceof TileChargingTable tile) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerChargingTable(BCSiliconMenuTypes.CHARGING_TABLE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    public static final MenuType<ContainerProgrammingTable_Neptune> PROGRAMMING_TABLE = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.level.getBlockEntity(data.readBlockPos()) instanceof TileProgrammingTable_Neptune tile) {
                    MessageUtil.clientHandleUpdateTileMsgBeforeOpen(tile, data);
                    return new ContainerProgrammingTable_Neptune(BCSiliconMenuTypes.PROGRAMMING_TABLE, windowId, inv.player, tile);
                } else {
                    return null;
                }
            }
    );
    /**
     * {@link IPipeHolder#onPlayerOpen(Player)} is moved from {@link ContainerGate#ContainerGate(MenuType, int, Player, GateLogic)} in 1.12.2
     * to ensure the new gate obj created before GUI opened.
     * <p>
     * {@link IPipeHolder#getPluggable(Direction)} is used to receive the new gate obj.
     * <p>
     * {@link MessageUtil#clientHandleUpdateTileMsgBeforeOpen(TileBC_Neptune, FriendlyByteBuf, Runnable...)}
     * handles the message created in {@link MessageUtil#serverOpenGUIWithMsg(Player, MenuProvider, BlockPos, int, IMessage)}
     * in {@link PluggableGate#onPluggableActivate(Player, HitResult, float, float, float)}
     */
    public static final MenuType<ContainerGate> GATE = IForgeMenuType.create((windowId, inv, data) ->
            {
                BlockPos pos = data.readBlockPos();
                if (inv.player.level.getBlockEntity(pos) instanceof IPipeHolder holder) {
                    int id = data.readInt();
                    Direction direction = Direction.from3DDataValue(id >>> 8);
                    if (holder.getPluggable(direction) instanceof PluggableGate gate) {
                        MessageUtil.clientHandleUpdateTileMsgBeforeOpen((TileBC_Neptune) holder, data);
                        gate.logic.getPipeHolder().onPlayerOpen(inv.player);

                        // Refresh the gate object
                        gate = (PluggableGate) holder.getPluggable(direction);

                        return new ContainerGate(BCSiliconMenuTypes.GATE, windowId, inv.player, gate.logic);
                    }
                }
                return null;
            }
    );

    public static void registerAll(RegistryEvent.Register<MenuType<?>> event) {
        event.getRegistry().registerAll(
                ASSEMBLY_TABLE.setRegistryName("assembly_table"),
                INTEGRATION_TABLE.setRegistryName("integration_table"),
                ADVANCED_CRAFTING_TABLE.setRegistryName("advanced_crafting_table"),
                CHARGING_TABLE.setRegistryName("charging_table"),
                PROGRAMMING_TABLE.setRegistryName("programming_table"),
                GATE.setRegistryName("gate")
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MenuScreens.register(ASSEMBLY_TABLE, GuiAssemblyTable::new);
            MenuScreens.register(INTEGRATION_TABLE, GuiIntegrationTable::new);
            MenuScreens.register(ADVANCED_CRAFTING_TABLE, GuiAdvancedCraftingTable::new);
            MenuScreens.register(CHARGING_TABLE, GuiChargingTable::new);
            MenuScreens.register(PROGRAMMING_TABLE, GuiProgrammingTable_Neptune::new);
            MenuScreens.register(GATE, GuiGate::new);
        }
    }
}

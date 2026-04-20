package ct.buildcraft.factory;

import ct.buildcraft.factory.client.gui.MenuHeatExchange;
import ct.buildcraft.factory.client.gui.ScreenHeatExchange;
import ct.buildcraft.factory.container.ContainerAutoCraftItems;
import ct.buildcraft.factory.container.ContainerChute;
import ct.buildcraft.factory.gui.GuiAutoCraftItems;
import ct.buildcraft.factory.gui.GuiChute;
import ct.buildcraft.lib.gui.BCContainerFactory;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCFactoryGuis {
	
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BCFactory.MODID);
	
    public static final RegistryObject<MenuType<ContainerAutoCraftItems>> MENU_AUTOWORK_BENCH_ITEM = MENUS.register("menu.autoworkbench_item", () -> BCContainerFactory.create(ContainerAutoCraftItems::create));
    public static final RegistryObject<MenuType<MenuHeatExchange>> MENU_HEAT_EXCHANGE = MENUS.register("menu.heat_exchange", () -> new MenuType<>(MenuHeatExchange::new));
    public static final RegistryObject<MenuType<ContainerChute>> MENU_CHUTE = MENUS.register("menu.chute", () -> BCContainerFactory.create(ContainerChute::new));

    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        event.enqueueWork(
                () -> {
                	MenuScreens.register(MENU_AUTOWORK_BENCH_ITEM.get(), GuiAutoCraftItems::new);
                	MenuScreens.register(MENU_HEAT_EXCHANGE.get(), ScreenHeatExchange::new);
                	MenuScreens.register(MENU_CHUTE.get(), GuiChute::new);
                }
        );
    }

    public void openGui(Player player) {
        openGui(player, 0, -1, 0);
    }

    public void openGui(Player player, BlockPos pos) {
        openGui(player, pos.getX(), pos.getY(), pos.getZ());
    }

    public void openGui(Player player, int x, int y, int z) {
//    	player.openMenu(null);
//        player.openGui(BCTransport, ordinal(), player.getLevel(), x, y, z);
    }
    static void registry(IEventBus bus) {
    	bus.addListener(BCFactoryGuis::clientInit);
    	MENUS.register(bus);
    }
}

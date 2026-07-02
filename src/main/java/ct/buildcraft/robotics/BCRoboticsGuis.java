package ct.buildcraft.robotics;

import ct.buildcraft.lib.gui.BCContainerFactory;
import ct.buildcraft.robotics.container.ContainerRequester;
import ct.buildcraft.robotics.container.ContainerZonePlanner;
import ct.buildcraft.robotics.gui.GuiRequester;
import ct.buildcraft.robotics.gui.GuiZonePlanner;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BCRoboticsGuis {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BCRobotics.MODID);

    public static final RegistryObject<MenuType<ContainerZonePlanner>> MENU_ZONE_PLANNER = MENUS.register(
            "menu.zone_planner",
            () -> BCContainerFactory.create(ContainerZonePlanner::new)
    );

    public static final RegistryObject<MenuType<ContainerRequester>> MENU_REQUESTER = MENUS.register(
            "menu.requester",
            () -> BCContainerFactory.create(ContainerRequester::new)
    );

    private BCRoboticsGuis() {
    }

    public static void clientInit(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(MENU_ZONE_PLANNER.get(), GuiZonePlanner::new);
            MenuScreens.register(MENU_REQUESTER.get(), GuiRequester::new);
        });
    }

    public static void registry(IEventBus bus) {
        bus.addListener(BCRoboticsGuis::clientInit);
        MENUS.register(bus);
    }
}

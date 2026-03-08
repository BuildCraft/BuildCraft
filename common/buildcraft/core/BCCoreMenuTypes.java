package buildcraft.core;

import buildcraft.core.list.ContainerList;
import buildcraft.core.list.GuiList;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class BCCoreMenuTypes {
    public static final ContainerType<ContainerList> LIST = IForgeContainerType.create((windowId, inv, data) ->
            {
                return new ContainerList(BCCoreMenuTypes.LIST, windowId, inv.player);
            }
    );

    public static void registerAll(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().registerAll(
                LIST.setRegistryName("list")
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ScreenManager.register(LIST, GuiList::new);
        }
    }
}

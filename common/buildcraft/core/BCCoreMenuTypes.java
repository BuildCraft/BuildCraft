package buildcraft.core;

import buildcraft.core.list.ContainerList;
import buildcraft.core.list.GuiList;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

public class BCCoreMenuTypes {
    public static final MenuType<ContainerList> LIST = IForgeMenuType.create((windowId, inv, data) ->
            {
                return new ContainerList(BCCoreMenuTypes.LIST, windowId, inv.player);
            }
    );

    public static void registerAll() {
        ForgeRegistries.MENU_TYPES.register("list", LIST);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MenuScreens.register(LIST, GuiList::new);
        }
    }
}

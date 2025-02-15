package buildcraft.lib;

import buildcraft.lib.container.ContainerGuide;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

public class BCLibMenuTypes {
    public static final MenuType<ContainerGuide> GUIDE = IForgeMenuType.create((windowId, inv, data) ->
            {
                if (inv.player.getMainHandItem().getItem() == BCLibItems.guide.get() || inv.player.getOffhandItem().getItem() == BCLibItems.guide.get()) {
                    return new ContainerGuide(BCLibMenuTypes.GUIDE, windowId);
                } else {
                    return null;
                }
            }
    );

    public static void registerAll() {
        ForgeRegistries.MENU_TYPES.register("guide", GUIDE);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MenuScreens.register(GUIDE, BCLibScreenConstructors.GUIDE);
        }
//        MenuScreens.register(
//                GUIDE,
//                (container, inv, title) ->
//                {
//                    Player player = inv.player;
//                    ItemStack stack;
//                    if (player.getMainHandItem().getItem() == BCLibItems.guide.get())
//                    {
//                        stack = player.getMainHandItem();
//                    }
//                    else if (player.getOffhandItem().getItem() == BCLibItems.guide.get())
//                    {
//                        stack = player.getOffhandItem();
//                    }
//                    else
//                    {
//                        stack = StackUtil.EMPTY;
//                    }
//                    String name = ItemGuide.getBookName(stack);
//                    if (name == null || name.isEmpty())
//                    {
//                        return new GuiGuide(container, title);
//                    }
//                    else
//                    {
//                        return new GuiGuide(container, name, title);
//                    }
//                }
//        );
    }
}

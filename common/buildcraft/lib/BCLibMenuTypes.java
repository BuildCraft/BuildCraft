package buildcraft.lib;

import buildcraft.lib.container.ContainerGuide;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class BCLibMenuTypes {
    public static final ContainerType<ContainerGuide> GUIDE = IForgeContainerType.create((windowId, inv, data) ->
            {
                if (inv.player.getMainHandItem().getItem() == BCLibItems.guide.get() || inv.player.getOffhandItem().getItem() == BCLibItems.guide.get()) {
                    return new ContainerGuide(BCLibMenuTypes.GUIDE, windowId);
                } else {
                    return null;
                }
            }
    );

    public static void registerAll(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().registerAll(
                GUIDE.setRegistryName("guide")
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ScreenManager.register(GUIDE, BCLibScreenConstructors.GUIDE);
        }
//        ScreenManager.register(
//                GUIDE,
//                (container, inv, title) ->
//                {
//                    PlayerEntity player = inv.player;
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

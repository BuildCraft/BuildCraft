package buildcraft.core.client;

import buildcraft.api.items.IMapLocation;
import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
public class CoreItemModelPredicates {
    public static final ResourceLocation PREDICATE_USED = new ResourceLocation("buildcraft", "used");
    public static final ResourceLocation PREDICATE_MAP_TYPE = new ResourceLocation("buildcraft", "map_type");

    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(
                () ->
                {
                    ItemModelsProperties.register(
                            BCCoreItems.list.get(),
                            PREDICATE_USED,
                            (stack, world, entity) -> ItemList_BC8.isUsed(stack) ? 1 : 0
                    );
                    ItemModelsProperties.register(
                            BCCoreItems.mapLocation.get(),
                            PREDICATE_MAP_TYPE,
                            (stack, world, entity) -> IMapLocation.MapLocationType.getFromStack(StackUtil.asNonNull(stack)).meta
                    );
                }
        );
    }
}

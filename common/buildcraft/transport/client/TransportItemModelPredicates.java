package buildcraft.transport.client;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportItems;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
public class TransportItemModelPredicates {
    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(
                () ->
                {
                    ItemModelsProperties.register(
                            BCTransportItems.wire.get(),
                            new ResourceLocation("buildcraft:colour"),
                            (stack, world, entity) -> ColourUtil.getStackColourIdFromTag(stack)
                    );
                }
        );
    }
}

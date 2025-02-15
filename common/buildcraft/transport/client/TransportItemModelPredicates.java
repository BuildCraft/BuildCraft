package buildcraft.transport.client;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
public class TransportItemModelPredicates {
    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(
                () ->
                {
                    ItemProperties.register(
                            BCTransportItems.wire.get(),
                            new ResourceLocation("buildcraft:colour"),
                            (stack, world, entity, pSeed) -> ColourUtil.getStackColourIdFromTag(stack)
                    );
                }
        );
    }
}

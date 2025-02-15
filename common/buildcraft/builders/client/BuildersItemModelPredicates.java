package buildcraft.builders.client;

import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
public class BuildersItemModelPredicates {
    public static final ResourceLocation PREDICATE_USED = new ResourceLocation("buildcraft", "used");

    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(
                () ->
                {
                    ItemProperties.register(
                            BCBuildersItems.snapshotBLUEPRINT.get(),
                            PREDICATE_USED,
                            (stack, world, entity, pSeed) -> stack.getTagElement(ItemSnapshot.TAG_KEY) != null ? 1 : 0
                    );
                    ItemProperties.register(
                            BCBuildersItems.snapshotTEMPLATE.get(),
                            PREDICATE_USED,
                            (stack, world, entity, pSeed) -> stack.getTagElement(ItemSnapshot.TAG_KEY) != null ? 1 : 0
                    );
                    ItemProperties.register(
                            BCBuildersItems.schematicSingle.get(),
                            PREDICATE_USED,
                            (stack, world, entity, pSeed) ->
                            {
                                if (stack.getDamageValue() == ItemSchematicSingle.DAMAGE_USED && stack.getTagElement(ItemSchematicSingle.NBT_KEY) != null) {
                                    return 1;
                                }
                                if (stack.getDamageValue() == ItemSchematicSingle.DAMAGE_CLEAN || stack.getTagElement(ItemSchematicSingle.NBT_KEY) == null) {
                                    return 0;
                                }
                                throw new RuntimeException("[builders.item] damage not match nbt!");
                            }
                    );
                }
        );
    }
}

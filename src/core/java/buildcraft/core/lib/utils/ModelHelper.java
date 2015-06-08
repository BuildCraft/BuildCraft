package buildcraft.core.lib.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;

public final class ModelHelper {
    private ModelHelper() {

    }

    public static void registerItemModel(Item item, int metadata, String suffix) {
        String type = Utils.getNameForItem(item) + suffix;
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, metadata, new ModelResourceLocation(type, "inventory"));
        ModelBakery.addVariantName(item, type);
    }

    public static ModelResourceLocation getItemResourceLocation(Item item, String suffix) {
        return new ModelResourceLocation(Utils.getNameForItem(item) + suffix, "inventory");
    }

    public static Object getBlockResourceLocation(Block block) {
        return new ModelResourceLocation(Utils.getNameForBlock(block));
    }
}

package buildcraft.core.utils;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import buildcraft.core.ItemBuildCraft;

public final class ModelHelper {
	private ModelHelper() {

	}

	public static void registerItemModel(Item item, int metadata, String suffix) {
		String type = Utils.getItemName(item) + suffix;
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, metadata, new ModelResourceLocation(type, "inventory"));
		ModelBakery.addVariantName(item, type);
	}

	public static ModelResourceLocation getItemResourceLocation(Item item, String suffix) {
		return new ModelResourceLocation(Utils.getItemName(item) + suffix, "inventory");
	}
}

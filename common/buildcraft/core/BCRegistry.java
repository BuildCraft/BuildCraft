package buildcraft.core;

import java.io.File;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import buildcraft.core.lib.items.ItemBlockBuildCraft;
import buildcraft.core.lib.utils.Utils;

public final class BCRegistry {
	public static final BCRegistry INSTANCE = new BCRegistry();
	private Configuration regCfg;

	private BCRegistry() {

	}

	public boolean isEnabled(String category, String name) {
		return regCfg.get(category, name, true).getBoolean();
	}

	public void setRegistryConfig(File f) {
		regCfg = new Configuration(f);
	}

	public boolean registerBlock(Block block, boolean forced) {
		return registerBlock(block, ItemBlockBuildCraft.class, forced);
	}

	public boolean isBlockEnabled(String name) {
		return regCfg.get("blocks", name, true).getBoolean();
	}

	public boolean isBlockEnabled(Block block) {
		String name = block.getUnlocalizedName().replace("tile.", "");
		return isBlockEnabled(name);
	}

	public boolean registerBlock(Block block, Class<? extends ItemBlock> item, boolean forced) {
		String name = block.getUnlocalizedName().replace("tile.", "");
		if (forced || isBlockEnabled(name)) {
			GameRegistry.registerBlock(block, item, name);
			return true;
		}
		return false;
	}

	public boolean isItemEnabled(String name) {
		return regCfg.get("items", name, true).getBoolean();
	}

	public boolean isItemEnabled(Item item) {
		String name = item.getUnlocalizedName().replace("item.", "");
		return isItemEnabled(name);
	}

	public boolean registerItem(Item item, boolean forced) {
		String name = item.getUnlocalizedName().replace("item.", "");
		if (forced || isItemEnabled(name)) {
			GameRegistry.registerItem(item, name);
			return true;
		}
		return false;
	}

	private boolean isInvalidRecipeElement(Object o) {
		if (o == null) {
			return true;
		}
		if (o instanceof Block && !Utils.isRegistered((Block) o)) {
			return true;
		}
		if (o instanceof Item && !Utils.isRegistered((Item) o)) {
			return true;
		}
		if (o instanceof ItemStack && !Utils.isRegistered((ItemStack) o)) {
			return true;
		}

		return false;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void registerTileEntity(Class clas, String ident) {
		GameRegistry.registerTileEntity(CompatHooks.INSTANCE.getTile(clas), ident);
	}

	public void addCraftingRecipe(ItemStack result, Object... recipe) {
		if (isInvalidRecipeElement(result)) {
			return;
		}

		for (Object o : recipe) {
			if (isInvalidRecipeElement(o)) {
				return;
			}
		}
		
		GameRegistry.addRecipe(new ShapedOreRecipe(result, recipe));
	}

	public void addShapelessRecipe(ItemStack result, Object... recipe) {
		if (isInvalidRecipeElement(result)) {
			return;
		}

		for (Object o : recipe) {
			if (isInvalidRecipeElement(o)) {
				return;
			}
		}

		GameRegistry.addRecipe(new ShapelessOreRecipe(result, recipe));
	}

	public void save() {
		regCfg.save();
	}
}

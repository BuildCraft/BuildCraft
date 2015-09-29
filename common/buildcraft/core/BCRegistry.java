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

public class BCRegistry {
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

	public boolean registerBlock(Block block, Class<? extends ItemBlock> item, boolean forced) {
		String name = block.getUnlocalizedName().replace("tile.", "");
		if (forced || regCfg.get("blocks", name, true).getBoolean()) {
			GameRegistry.registerBlock(block, item, name);
			return true;
		}
		return false;
	}

	public boolean registerItem(Item item, boolean forced) {
		String name = item.getUnlocalizedName().replace("item.", "");
		if (forced || regCfg.get("items", name, true).getBoolean()) {
			GameRegistry.registerItem(item, name);
			return true;
		}
		return false;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void registerTileEntity(Class clas, String ident) {
		GameRegistry.registerTileEntity(CompatHooks.INSTANCE.getTile(clas), ident);
	}

	public void addCraftingRecipe(ItemStack result, Object... recipe) {
		for (Object o : recipe) {
			if (o instanceof Block && Block.getIdFromBlock((Block) o) < 0) {
				return;
			}
			if (o instanceof Item && Item.getIdFromItem((Item) o) < 0) {
				return;
			}
			if (o instanceof ItemStack && ((ItemStack) o).getItem() == null) {
				return;
			}
		}
		GameRegistry.addRecipe(new ShapedOreRecipe(result, recipe));
	}

	public void addShapelessRecipe(ItemStack result, Object... recipe) {
		for (Object o : recipe) {
			if (o instanceof Block && Block.getIdFromBlock((Block) o) < 0) {
				return;
			}
			if (o instanceof Item && Item.getIdFromItem((Item) o) < 0) {
				return;
			}
			if (o instanceof ItemStack && ((ItemStack) o).getItem() == null) {
				return;
			}
		}
		GameRegistry.addRecipe(new ShapelessOreRecipe(result, recipe));
	}

	public void save() {
		regCfg.save();
	}
}

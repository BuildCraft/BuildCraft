package buildcraft.core;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Optional;

import buildcraft.BuildCraftCore;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.core.lib.utils.Utils;

public final class CoreSiliconRecipes {
	private CoreSiliconRecipes() {

	}

	@Optional.Method(modid = "BuildCraft|Silicon")
	public static void loadSiliconRecipes() {
		// Lists
		if (Utils.isRegistered(BuildCraftCore.listItem)) {
			BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:list", 20000, new ItemStack(BuildCraftCore.listItem, 1, 1),
					"dyeGreen", "dustRedstone", new ItemStack(Items.paper, 8));
		}
	}
}


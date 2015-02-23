package buildcraft.core.worldgen;

import java.util.Random;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;

import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;

public class VillagerTradeHandler implements IVillageTradeHandler
{

	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random)
	{
		recipeList.clear();
		//Buy
		recipeList.add(new MerchantRecipe(new ItemStack(BuildCraftEnergy.bucketFuel), new ItemStack(Items.emerald, 1 + random.nextInt(2))));
		//recipeList.add(new MerchantRecipe(new ItemStack(BuildCraftTransport.pipeWaterproof, 12 + random.nextInt(8)), new ItemStack(Items.emerald)));
		//recipeList.add(new MerchantRecipe(new ItemStack(BuildCraftFactory.tankBlock, 10 + random.nextInt(6)), new ItemStack(Items.emerald)));
		//Sell
		//recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(BuildCraftTransport.pipeItemsWood), new ItemStack(BuildCraftTransport.pipeFluidsWood, 2)));
		villager.setRecipes(recipeList);
	}

}

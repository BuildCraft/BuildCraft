package buildcraft.transport;

import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.recipe.RecipeBuilderShaped;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class BCTransportRecipes {
    public static void init() {
        if (BCTransportItems.waterproof != null) {
            GameRegistry.addShapelessRecipe(new ItemStack(BCTransportItems.waterproof), new ItemStack(Items.DYE, 1, 2));
        }

        if (Utils.isRegistered(BCTransportBlocks.filteredBuffer)) {
            ItemStack out = new ItemStack(BCTransportBlocks.filteredBuffer);
            RecipeBuilderShaped builder = new RecipeBuilderShaped(out);
            builder.add("w w"); // TODO: diamond pipe in center of this line
            builder.add("wcw");
            builder.add("wpw");
            builder.map('w', "plankWood");
            builder.map('p', Blocks.PISTON);
            builder.map('c', Blocks.CHEST);
            GameRegistry.addRecipe(builder.build());
        }
    }
}

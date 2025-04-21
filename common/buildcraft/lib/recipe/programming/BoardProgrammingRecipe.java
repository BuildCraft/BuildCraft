package buildcraft.lib.recipe.programming;

import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.api.recipes.IngredientStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public class BoardProgrammingRecipe implements IProgrammingRecipe {
    // public static final BoardProgrammingRecipe INSTANCE = new BoardProgrammingRecipe();

    // Calen 1.18.2 moved to ProgrammingRecipeManager
//    private class BoardSorter implements Comparator<ItemStack> {
//        private BoardProgrammingRecipe recipe;
//
//        public BoardSorter(BoardProgrammingRecipe recipe) {
//            this.recipe = recipe;
//        }
//
//        @Override
//        public int compare(ItemStack o1, ItemStack o2) {
//            int i = (recipe.getEnergyCost(o1) - recipe.getEnergyCost(o2)) * 200;
//            return i != 0 ? i : ItemRedstoneBoard.getBoardNBT(o1).getID().compareTo(ItemRedstoneBoard.getBoardNBT(o2).getID());
//        }
//    }

    private final ResourceLocation id;
    private static final IngredientStack INPUT = IngredientStack.of(new ItemStack(RedstoneBoardRegistry.instance.getBoardNBTItemMap().get(RedstoneBoardRegistry.instance.getEmptyRobotBoard()).get()));
    private final ItemStack output;
    private final long energyCost;

    public BoardProgrammingRecipe(ResourceLocation id, ItemStack output, long energyCost) {
        this.id = id;
        this.output = output;
        this.energyCost = energyCost;
    }

    @Override
    public ResourceLocation getId() {
        // return "buildcraft:redstone_board";
        // return new ResourceLocation(BCModules.ROBOTICS.getModId(), "redstone_board");
        return this.id;
    }

    private List<ItemStack> cachedSorted;
    private List<ItemStack> cachedOptions;

//    @Override
//    public List<ItemStack> getOptions(int width, int height) {
//        List<ItemStack> options = new ArrayList<ItemStack>(width * height);
//        for (RedstoneBoardNBT<?> nbt : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
//            ItemStack stack = new ItemStack(BuildCraftRobotics.redstoneBoard);
//            nbt.createBoard(NBTUtils.getItemData(stack));
//            options.add(stack);
//        }
//        Collections.sort(options, new BoardSorter(this));
//        return options;
//    }

    @Override
    // public long getEnergyCost(ItemStack option)
    public long getEnergyCost() {
        // return RedstoneBoardRegistry.instance.getPowerCost(RedstoneBoardRegistry.instance.getRedstoneBoard(option.getOrCreateTag().getString("id")));
        // return RedstoneBoardRegistry.instance.getPowerCost(ItemRedstoneBoard.getBoardNBT(option));
        return this.energyCost;
    }

    @Override
    public boolean canCraft(ItemStack input) {
        // return input.getItem() instanceof ItemRedstoneBoard;
        return INPUT.ingredient.test(input);
    }

    @Override
    // public ItemStack craft(ItemStack input, ItemStack option)
    public ItemStack craft(ItemStack input) {
        // return option.copy();
        return output.copy();
    }

    @Override
    public IngredientStack getInput() {
        return INPUT;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ProgrammingRecipeSerializer.INSTANCE;
    }
}

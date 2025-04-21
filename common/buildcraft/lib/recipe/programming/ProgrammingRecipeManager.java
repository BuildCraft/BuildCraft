package buildcraft.lib.recipe.programming;

import buildcraft.api.core.BCLog;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.api.recipes.IProgrammingRecipeManager;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;

public enum ProgrammingRecipeManager implements IProgrammingRecipeManager {
    // public static final ProgrammingRecipeManager INSTANCE = new ProgrammingRecipeManager();
    INSTANCE;
    // private final HashMap<String, IProgrammingRecipe> recipes = new HashMap<String, IProgrammingRecipe>();
    private final HashMap<ResourceLocation, IProgrammingRecipe> recipes = new HashMap<ResourceLocation, IProgrammingRecipe>();

    @Override
    public void addRecipe(IProgrammingRecipe recipe) {
        if (recipe == null || recipe.getId() == null) {
            return;
        }

        if (!recipes.containsKey(recipe.getId())) {
            recipes.put(recipe.getId(), recipe);
        } else {
            BCLog.logger.warn("Programming Table Recipe '" + recipe.getId() + "' seems to be duplicated! This is a bug!");
        }
    }

    @Override
    // public void removeRecipe(String id)
    public void removeRecipe(ResourceLocation id) {
        recipes.remove(id);
    }

    @Override
    public void removeRecipe(IProgrammingRecipe recipe) {
        if (recipe == null || recipe.getId() == null) {
            return;
        }

        recipes.remove(recipe.getId());
    }

    @Override
    // public Collection<IProgrammingRecipe> getRecipes()
    public Collection<IProgrammingRecipe> getRecipes(Level world) {
        // return Collections.unmodifiableCollection(recipes.values());
        Collection<IProgrammingRecipe> ret = Lists.newArrayList();
        ret.addAll(recipes.values());
        world.getRecipeManager().byType(IProgrammingRecipe.TYPE).values().stream().filter(c -> c instanceof IProgrammingRecipe).forEach(c -> ret.add((IProgrammingRecipe) c));
        return ret;
    }

    @Override
    // public IProgrammingRecipe getRecipe(String id)
    public IProgrammingRecipe getRecipe(Level world, ResourceLocation id) {
        // return recipes.get(id);
        for (IProgrammingRecipe recipe : getRecipes(world)) {
            if (Objects.equals(recipe.getId(), id)) {
                return recipe;
            }
        }
        return null;
    }

    // Calen 1.18.2 from IProgrammingRecipe
    public List<IProgrammingRecipe> getOptions(List<IProgrammingRecipe> recipes, int width, int height) {
        // List<ItemStack> options = new ArrayList<ItemStack>(width * height);
        List<IProgrammingRecipe> options = new ArrayList<IProgrammingRecipe>(width * height);
        // for (RedstoneBoardNBT<?> nbt : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
        //     ItemStack stack = new ItemStack(BuildCraftRobotics.redstoneBoard);
        //     nbt.createBoard(NBTUtils.getItemData(stack));
        //     options.add(stack);
        // }
        for (int i = 0; i < recipes.size(); i++) {
            options.add(i, recipes.get(i));
        }
        // Collections.sort(options, new BoardProgrammingRecipe.BoardSorter(this));
        Collections.sort(options, new Sorter());
        return options;
    }

    // Calen 1.18.2 from BoardProgrammingRecipe
    public static class Sorter implements Comparator<IProgrammingRecipe> {
        @Override
        public int compare(IProgrammingRecipe o1, IProgrammingRecipe o2) {
            long iL = (o1.getEnergyCost() - o2.getEnergyCost()) * 200L;
            int i = (int) (iL / MjAPI.MJ);
            return i != 0 ? i : o1.getId().compareTo(o2.getId());
        }
    }
}

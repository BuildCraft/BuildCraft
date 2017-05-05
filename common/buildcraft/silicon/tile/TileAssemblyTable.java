package buildcraft.silicon.tile;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IAssemblyRecipeProvider;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.silicon.EnumAssemblyRecipeState;

public class TileAssemblyTable extends TileLaserTableBase {
    public static final int NET_RECIPE_STATE = 10;
    public final ItemHandlerSimple inv = itemManager.addInvHandler("", 3 * 4, ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);
    public SortedMap<AssemblyRecipe, EnumAssemblyRecipeState> recipesStates = new TreeMap<>();

    private void updateRecipes() {
        AssemblyRecipeRegistry.INSTANCE.getRecipesFor(inv.stacks).forEach(recipe ->
                recipesStates.putIfAbsent(recipe, EnumAssemblyRecipeState.POSSIBLE));
        boolean findActive = false;
        for (Iterator<Map.Entry<AssemblyRecipe, EnumAssemblyRecipeState>> iterator = recipesStates.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<AssemblyRecipe, EnumAssemblyRecipeState> entry = iterator.next();
            AssemblyRecipe recipe = entry.getKey();
            EnumAssemblyRecipeState state = entry.getValue();
            boolean enough = extract(inv, recipe.requiredStacks, true, false);
            if (state == EnumAssemblyRecipeState.POSSIBLE) {
                if (!enough) {
                    iterator.remove();
                }
            } else {
                if (enough) {
                    if (state == EnumAssemblyRecipeState.SAVED) {
                        state = EnumAssemblyRecipeState.SAVED_ENOUGH;
                    }
                } else {
                    if (state != EnumAssemblyRecipeState.SAVED) {
                        state = EnumAssemblyRecipeState.SAVED;
                    }
                }
            }
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE) {
                findActive = true;
            }
            entry.setValue(state);
        }
        if (!findActive) {
            for (Map.Entry<AssemblyRecipe, EnumAssemblyRecipeState> entry : recipesStates.entrySet()) {
                EnumAssemblyRecipeState state = entry.getValue();
                if (state == EnumAssemblyRecipeState.SAVED_ENOUGH) {
                    state = EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE;
                    entry.setValue(state);
                    break;
                }
            }
        }
    }

    private AssemblyRecipe getActiveRecipe() {
        return recipesStates.entrySet().stream().filter(entry -> entry.getValue() == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private void activateNextRecipe() {
        AssemblyRecipe activeRecipe = getActiveRecipe();
        if (activeRecipe != null) {
            int index = 0;
            int activeIndex = 0;
            boolean isActiveLast = false;
            long enoughCount = recipesStates.values().stream().filter(state -> state == EnumAssemblyRecipeState.SAVED_ENOUGH || state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE).count();
            if (enoughCount <= 1) {
                return;
            }
            for (Map.Entry<AssemblyRecipe, EnumAssemblyRecipeState> entry : recipesStates.entrySet()) {
                EnumAssemblyRecipeState state = entry.getValue();
                if (state == EnumAssemblyRecipeState.SAVED_ENOUGH) {
                    isActiveLast = false;
                }
                if (state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE) {
                    state = EnumAssemblyRecipeState.SAVED_ENOUGH;
                    entry.setValue(state);
                    activeIndex = index;
                    isActiveLast = true;
                }
                index++;
            }
            index = 0;
            for (Map.Entry<AssemblyRecipe, EnumAssemblyRecipeState> entry : recipesStates.entrySet()) {
                AssemblyRecipe recipe = entry.getKey();
                EnumAssemblyRecipeState state = entry.getValue();
                if (state == EnumAssemblyRecipeState.SAVED_ENOUGH && recipe != activeRecipe && (index > activeIndex || isActiveLast)) {
                    state = EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE;
                    entry.setValue(state);
                    break;
                }
                index++;
            }
        }
    }

    public long getTarget() {
        return getActiveRecipe() == null ? 0 : getActiveRecipe().requiredMicroJoules;
    }

    @Override
    public void update() {
        super.update();

        if (world.isRemote) {
            return;
        }

        updateRecipes();

        if (power >= getTarget() && getTarget() != 0) {
            AssemblyRecipe recipe = getActiveRecipe();
            extract(inv, recipe.requiredStacks, false, false);

            InventoryUtil.addToBestAcceptor(getWorld(), getPos(), null, recipe.output.copy());

            power -= getTarget();
            activateNextRecipe();
        }
        sendNetworkGuiUpdate(NET_GUI_DATA);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList recipesStatesTag = new NBTTagList();
        recipesStates.forEach((recipe, state) -> {
            NBTTagCompound entryTag = new NBTTagCompound();
            entryTag.setString("recipe", recipe.name.toString());
            if (recipe.recipeTag != null) entryTag.setTag("recipe_tag", recipe.recipeTag);
            entryTag.setInteger("state", state.ordinal());
            recipesStatesTag.appendTag(entryTag);
        });
        nbt.setTag("recipes_states", recipesStatesTag);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        recipesStates.clear();
        NBTTagList recipesStatesTag = nbt.getTagList("recipes_states", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < recipesStatesTag.tagCount(); i++) {
            NBTTagCompound entryTag = recipesStatesTag.getCompoundTagAt(i);
            String name = entryTag.getString("recipe");
            NBTTagCompound recipeTag = entryTag.hasKey("recipe_tag") ? entryTag.getCompoundTag("recipe_tag") : null;
            recipesStates.put(lookupRecipe(name, recipeTag), EnumAssemblyRecipeState.values()[entryTag.getInteger("state")]);
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);

        if (id == NET_GUI_DATA) {
            buffer.writeInt(recipesStates.size());
            recipesStates.forEach((recipe, state) -> {
                buffer.writeString(recipe.name.toString());
                buffer.writeCompoundTag(recipe.recipeTag);
                buffer.writeInt(state.ordinal());
            });
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);

        if (id == NET_GUI_DATA) {
            recipesStates.clear();
            int count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                AssemblyRecipe recipe = lookupRecipe(buffer.readString(), buffer.readCompoundTag());
                recipesStates.put(recipe, EnumAssemblyRecipeState.values()[buffer.readInt()]);
            }
        }

        if (id == NET_RECIPE_STATE) {
            AssemblyRecipe recipe = lookupRecipe(buffer.readString(), buffer.readCompoundTag());
            EnumAssemblyRecipeState state = EnumAssemblyRecipeState.values()[buffer.readInt()];
            if (recipesStates.containsKey(recipe)) {
                recipesStates.put(recipe, state);
            }
        }
    }

    public void sendRecipeStateToServer(AssemblyRecipe recipe, EnumAssemblyRecipeState state) {
        IMessage message = createMessage(NET_RECIPE_STATE, (buffer) -> {
            buffer.writeString(recipe.name.toString());
            buffer.writeCompoundTag(recipe.recipeTag);
            buffer.writeInt(state.ordinal());
        });
        MessageUtil.getWrapper().sendToServer(message);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("recipes - " + recipesStates.size());
        left.add("target - " + LocaleUtil.localizeMj(getTarget()));
    }

    @Override
    public boolean hasWork() {
        return getActiveRecipe() != null;
    }

    private AssemblyRecipe lookupRecipe(String name, NBTTagCompound recipeTag) {
        return AssemblyRecipeRegistry.INSTANCE.getRecipe(new ResourceLocation(name), recipeTag).orElseThrow(() -> new RuntimeException("Assembly recipe with name " + name + " not found"));
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IngredientStack;

import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.silicon.EnumAssemblyRecipeState;

public class TileAssemblyTable extends TileLaserTableBase {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("assembly_table");
    public static final int NET_RECIPE_STATE = IDS.allocId("RECIPE_STATE");

    public final ItemHandlerSimple inv = itemManager.addInvHandler(
        "inv",
        3 * 4,
        ItemHandlerManager.EnumAccess.BOTH,
        EnumPipePart.VALUES
    );
    public SortedMap<AssemblyInstruction, EnumAssemblyRecipeState> recipesStates = new TreeMap<>();

    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftsilicon:precision_crafting");
    private final SortedMap<AssemblyInstruction, Collection<IngredientStack>> savedTargets = new TreeMap<>();
    private final Set<EntityPlayer> guiPlayers = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final int REFRESH_NONE = 0;
    private static final int REFRESH_TARGETS = 1;
    private static final int REFRESH_RECIPES = 2;
    private int refreshState = REFRESH_RECIPES;

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    private boolean updateRecipes() {
        boolean changed = false;
        for (AssemblyRecipe recipe: AssemblyRecipeRegistry.REGISTRY.values()) {
            Set<ItemStack> outputs = recipe.getOutputs(inv.stacks);
            for (ItemStack out: outputs) {
                boolean found = false;
                for (AssemblyInstruction instruction: recipesStates.keySet()) {
                    if (instruction.recipe == recipe && out == instruction.output) {
                        found = true;
                        break;
                    }
                }
                AssemblyInstruction instruction = new AssemblyInstruction(recipe, out);
                if (!found && !recipesStates.containsKey(instruction)) {
                    recipesStates.put(instruction, EnumAssemblyRecipeState.POSSIBLE);
                    changed = true;
                }
            }
        }

        for (Iterator<Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState>> iterator = recipesStates.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState> entry = iterator.next();
            AssemblyInstruction instruction = entry.getKey();
            if (entry.getValue() == EnumAssemblyRecipeState.POSSIBLE
                && !extract(inv, instruction.recipe.getInputsFor(instruction.output), true, false)) {
                iterator.remove();
                changed = true;
            }
        }

        cacheSavedTargets();
        return updateSavedTargets() || changed;
    }

    private void cacheSavedTargets() {
        savedTargets.clear();
        for (Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState> entry : recipesStates.entrySet()) {
            if (entry.getValue() != EnumAssemblyRecipeState.POSSIBLE) {
                AssemblyInstruction instruction = entry.getKey();
                savedTargets.put(instruction, instruction.recipe.getInputsFor(instruction.output));
            }
        }
    }

    private boolean updateSavedTargets() {
        boolean findActive = false;
        boolean changed = false;
        for (Map.Entry<AssemblyInstruction, Collection<IngredientStack>> entry : savedTargets.entrySet()) {
            AssemblyInstruction instruction = entry.getKey();
            EnumAssemblyRecipeState state = recipesStates.get(instruction);
            if (state == null || state == EnumAssemblyRecipeState.POSSIBLE) {
                continue;
            }

            boolean enough = extract(inv, entry.getValue(), true, false);
            if (enough) {
                if (state == EnumAssemblyRecipeState.SAVED) {
                    state = EnumAssemblyRecipeState.SAVED_ENOUGH;
                    changed = true;
                }
            } else if (state != EnumAssemblyRecipeState.SAVED) {
                state = EnumAssemblyRecipeState.SAVED;
                changed = true;
            }
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE) {
                findActive = true;
            }
            recipesStates.put(instruction, state);
        }
        if (!findActive) {
            for (AssemblyInstruction instruction : savedTargets.keySet()) {
                EnumAssemblyRecipeState state = recipesStates.get(instruction);
                if (state == EnumAssemblyRecipeState.SAVED_ENOUGH) {
                    recipesStates.put(instruction, EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE);
                    changed = true;
                    break;
                }
            }
        }
        return changed;
    }

    private AssemblyInstruction getActiveRecipe() {
        return recipesStates.entrySet().stream().filter(entry -> entry.getValue() == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private void activateNextRecipe() {
        AssemblyInstruction activeRecipe = getActiveRecipe();
        if (activeRecipe != null) {
            int index = 0;
            int activeIndex = 0;
            boolean isActiveLast = false;
            long enoughCount = recipesStates.values().stream().filter(state -> state == EnumAssemblyRecipeState.SAVED_ENOUGH || state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE).count();
            if (enoughCount <= 1) {
                return;
            }
            for (Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState> entry : recipesStates.entrySet()) {
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
            for (Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState> entry : recipesStates.entrySet()) {
                AssemblyRecipe recipe = entry.getKey().recipe;
                EnumAssemblyRecipeState state = entry.getValue();
                if (state == EnumAssemblyRecipeState.SAVED_ENOUGH && recipe != activeRecipe.recipe && (index > activeIndex || isActiveLast)) {
                    state = EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE;
                    entry.setValue(state);
                    break;
                }
                index++;
            }
        }
    }

    @Override
    public long getTarget() {
        return Optional.ofNullable(getActiveRecipe()).map(instruction -> instruction.recipe.getRequiredMicroJoulesFor(instruction.output)).orElse(0L);
    }

    @Override
    public void update() {
        super.update();

        if (world.isRemote) {
            return;
        }

        boolean recipesChanged = false;
        if (refreshState == REFRESH_RECIPES) {
            recipesChanged = updateRecipes();
        } else if (refreshState == REFRESH_TARGETS) {
            recipesChanged = updateSavedTargets();
        }
        refreshState = REFRESH_NONE;

        if (recipesChanged) {
            sendNetworkGuiUpdate(NET_GUI_DATA);
        }

        if (getTarget() > 0) {
            AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT);
            if (power >= getTarget()) {
                AssemblyInstruction instruction = getActiveRecipe();

                // As input can be extracted before recipe completion, we check we have everything
                // before actually completing the recipe and consuming the inputs
                Collection<IngredientStack> inputs = instruction.recipe.getInputsFor(instruction.output);
                if (!extract(inv, inputs, true, false)) {
                    if (updateSavedTargets()) sendNetworkGuiUpdate(NET_GUI_DATA);
                    return;
                }

                extract(inv, inputs, false, false);

                InventoryUtil.addToBestAcceptor(getWorld(), getPos(), null, instruction.output.copy());

                power -= getTarget();
                activateNextRecipe();
                updateSavedTargets();
                sendNetworkGuiUpdate(NET_GUI_DATA);
            }
        }
    }

    @Override
    public void onPlayerOpen(EntityPlayer player) {
        if (!world.isRemote) {
            updateRecipes();
            refreshState = REFRESH_NONE;
        }
        super.onPlayerOpen(player);
        if (!world.isRemote) {
            guiPlayers.add(player);
        }
    }

    @Override
    public void onPlayerClose(EntityPlayer player) {
        if (!world.isRemote) {
            guiPlayers.remove(player);
        }
        super.onPlayerClose(player);
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (handler == inv && refreshState == REFRESH_NONE) {
            if (!guiPlayers.isEmpty()) {
                // If we have someone viewing the GUI, refresh the recipes every time inventory changes,
                // so things stay responsive
                refreshState = REFRESH_RECIPES;
            } else if (getTarget() == 0) {
                // Otherwise, we only need to refresh in the (rare) case we have no active target
                // If we have a target, they will naturally refresh when the it finishes processing
                refreshState = REFRESH_TARGETS;
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList recipesStatesTag = new NBTTagList();
        recipesStates.forEach((instruction, state) -> {
            NBTTagCompound entryTag = new NBTTagCompound();
            entryTag.setString("recipe", instruction.recipe.getRegistryName().toString());
            entryTag.setTag("output", instruction.output.serializeNBT());
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
            if (entryTag.hasKey("output")) {
                AssemblyInstruction instruction = lookupRecipe(name, new ItemStack(entryTag.getCompoundTag("output")));
                if (instruction != null)
                    recipesStates.put(instruction, EnumAssemblyRecipeState.values()[entryTag.getInteger("state")]);
            }
        }
        savedTargets.clear();
        refreshState = REFRESH_RECIPES;
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);

        if (id == NET_GUI_DATA) {
            buffer.writeInt(recipesStates.size());
            recipesStates.forEach((instruction, state) -> {
                buffer.writeString(instruction.recipe.getRegistryName().toString());
                buffer.writeItemStack(instruction.output);
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
                AssemblyInstruction instruction = lookupRecipe(buffer.readString(), buffer.readItemStack());
                recipesStates.put(instruction, EnumAssemblyRecipeState.values()[buffer.readInt()]);
            }
        }

        if (id == NET_RECIPE_STATE) {
            AssemblyInstruction recipe = lookupRecipe(buffer.readString(), buffer.readItemStack());
            EnumAssemblyRecipeState state = EnumAssemblyRecipeState.values()[buffer.readInt()];
            if (recipesStates.containsKey(recipe)) {
                recipesStates.put(recipe, state);
                cacheSavedTargets();
                updateSavedTargets();
                sendNetworkUpdate(NET_GUI_DATA);
            }
        }
    }

    public void sendRecipeStateToServer(AssemblyInstruction instruction, EnumAssemblyRecipeState state) {
        IMessage message = createMessage(NET_RECIPE_STATE, (buffer) -> {
            buffer.writeString(instruction.recipe.getRegistryName().toString());
            buffer.writeItemStack(instruction.output);
            buffer.writeInt(state.ordinal());
        });
        MessageManager.sendToServer(message);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("recipes - " + recipesStates.size());
        left.add("target - " + LocaleUtil.localizeMj(getTarget()));
    }

    @Nullable
    private AssemblyInstruction lookupRecipe(String name, ItemStack output) {
        AssemblyRecipe recipe = AssemblyRecipeRegistry.REGISTRY.get(new ResourceLocation(name));
        return recipe != null ? new AssemblyInstruction(recipe, output) : null;
    }

    public class AssemblyInstruction implements Comparable<AssemblyInstruction> {
        public final AssemblyRecipe recipe;
        public final ItemStack output;

        private AssemblyInstruction(AssemblyRecipe recipe, ItemStack output) {
            this.recipe = recipe;
            this.output = output;
        }

        @Override
        public int compareTo(AssemblyInstruction o) {
            return recipe.compareTo(o.recipe) + output.serializeNBT().toString().compareTo(o.output.serializeNBT().toString());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AssemblyInstruction)) return false;
            AssemblyInstruction instruction = (AssemblyInstruction) obj;
            return recipe.getRegistryName().equals(instruction.recipe.getRegistryName()) && ItemStack.areItemStacksEqual(output, instruction.output);
        }
    }
}

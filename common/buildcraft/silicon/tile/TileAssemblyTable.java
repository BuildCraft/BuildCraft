/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.net.IMessage;
import buildcraft.api.recipes.IAssemblyRecipe;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.recipe.assembly.AssemblyRecipeRegistry;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.craft.IAssemblyCraft;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.EnumAssemblyRecipeState;
import buildcraft.silicon.container.ContainerAssemblyTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class TileAssemblyTable extends TileLaserTableBase implements IAssemblyCraft {
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

    public TileAssemblyTable(BlockPos pos, BlockState blockState) {
        super(BCSiliconBlocks.assemblyTableTile.get(), pos, blockState);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    private void updateRecipes() {
        //TODO: rework this to not iterate over every recipe every tick
        int count = recipesStates.size();
//        for (AssemblyRecipe recipe : AssemblyRecipeRegistry.REGISTRY.values())
        for (IAssemblyRecipe recipe : AssemblyRecipeRegistry.getAll(level)) {
            Set<ItemStack> outputs = recipe.getOutputs(inv.stacks);
            for (ItemStack out : outputs) {
                boolean found = false;
                for (AssemblyInstruction instruction : recipesStates.keySet()) {
                    if (instruction.recipe == recipe && out == instruction.output) {
                        found = true;
                        break;
                    }
                }
                AssemblyInstruction instruction = new AssemblyInstruction(recipe, out);
                if (!found && !recipesStates.containsKey(instruction)) {
                    recipesStates.put(instruction, EnumAssemblyRecipeState.POSSIBLE);
                }
            }
        }

        boolean findActive = false;
        for (Iterator<Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState>> iterator = recipesStates.entrySet().iterator(); iterator.hasNext(); )
        {
            Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState> entry = iterator.next();
            AssemblyInstruction instruction = entry.getKey();
            EnumAssemblyRecipeState state = entry.getValue();
            boolean enough = extract(inv, instruction.recipe.getInputsFor(instruction.output), true, false);
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
            for (Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState> entry : recipesStates.entrySet()) {
                EnumAssemblyRecipeState state = entry.getValue();
                if (state == EnumAssemblyRecipeState.SAVED_ENOUGH) {
                    state = EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE;
                    entry.setValue(state);
                    break;
                }
            }
        }
        if (count != recipesStates.size()) {
            sendNetworkGuiUpdate(NET_GUI_DATA);
        }
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
                IAssemblyRecipe recipe = entry.getKey().recipe;
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

        if (level.isClientSide) {
            return;
        }

        updateRecipes();

        if (getTarget() > 0) {
            AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT);
            if (power >= getTarget()) {
                AssemblyInstruction instruction = getActiveRecipe();
                extract(inv, instruction.recipe.getInputsFor(instruction.output), false, false);

                InventoryUtil.addToBestAcceptor(getLevel(), getBlockPos(), null, instruction.output.copy());

                power -= getTarget();
                activateNextRecipe();
            }
            sendNetworkGuiUpdate(NET_GUI_DATA);
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        ListTag recipesStatesTag = new ListTag();
        recipesStates.forEach((instruction, state) ->
        {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("recipe", instruction.recipe.getId().toString());
            entryTag.put("output", instruction.output.serializeNBT());
            entryTag.putInt("state", state.ordinal());
            recipesStatesTag.add(entryTag);
        });
        nbt.put("recipes_states", recipesStatesTag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        recipesStates.clear();
        ListTag recipesStatesTag = nbt.getList("recipes_states", Tag.TAG_COMPOUND);
        for (int i = 0; i < recipesStatesTag.size(); i++) {
//            CompoundTag entryTag = recipesStatesTag.getCompound(i);
            CompoundTag entryTag = recipesStatesTag.getCompound(i).copy();
            String name = entryTag.getString("recipe");
            if (entryTag.contains("output")) {
                // Calen: Here this.level is null. lookup recipes -> NPE
//                AssemblyInstruction instruction = lookupRecipe(name, ItemStack.of(entryTag.getCompound("output")));
//                if (instruction != null)
//                    recipesStates.put(instruction, EnumAssemblyRecipeState.values()[entryTag.getInt("state")]);
                runWhenWorldNotNull(() ->
                        {
                            AssemblyInstruction instruction = lookupRecipe(name, ItemStack.of(entryTag.getCompound("output")));
                            if (instruction != null)
                                recipesStates.put(instruction, EnumAssemblyRecipeState.values()[entryTag.getInt("state")]);
                        },
                        false);
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);

        if (id == NET_GUI_DATA) {
            buffer.writeInt(recipesStates.size());
            recipesStates.forEach((instruction, state) ->
            {
                buffer.writeUtf(instruction.recipe.getId().toString());
                buffer.writeItemStack(instruction.output, false);
                buffer.writeInt(state.ordinal());
            });
        }
    }

    @Override
//    public void readPayload(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);

        if (id == NET_GUI_DATA) {
            recipesStates.clear();
            int count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                AssemblyInstruction instruction = lookupRecipe(buffer.readString(), buffer.readItem());
                recipesStates.put(instruction, EnumAssemblyRecipeState.values()[buffer.readInt()]);
            }
        }

        if (id == NET_RECIPE_STATE) {
            AssemblyInstruction recipe = lookupRecipe(buffer.readString(), buffer.readItem());
            EnumAssemblyRecipeState state = EnumAssemblyRecipeState.values()[buffer.readInt()];
            if (recipesStates.containsKey(recipe)) {
                recipesStates.put(recipe, state);
            }
        }
    }

    public void sendRecipeStateToServer(AssemblyInstruction instruction, EnumAssemblyRecipeState state) {
        IMessage message = createMessage(NET_RECIPE_STATE, (buffer) ->
        {
            buffer.writeUtf(instruction.recipe.getId().toString());
            buffer.writeItemStack(instruction.output, false);
            buffer.writeInt(state.ordinal());
        });
        MessageManager.sendToServer(message);
    }

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
        super.getDebugInfo(left, right, side);
//        left.add("recipes - " + recipesStates.size());
        left.add(Component.literal("recipes - " + recipesStates.size()));
//        left.add("target - " + LocaleUtil.localizeMj(getTarget()));
        left.add(Component.literal("target - ").append(LocaleUtil.localizeMjComponent(getTarget())));
    }

    @Nullable
    private AssemblyInstruction lookupRecipe(String name, ItemStack output) {
//        AssemblyRecipe recipe = AssemblyRecipeRegistry.REGISTRY.get(new ResourceLocation(name));
        Optional<IAssemblyRecipe> recipe = AssemblyRecipeRegistry.getAll(level).stream().filter(r -> r.getId().equals(new ResourceLocation(name))).findFirst();
//        return recipe != null ? new AssemblyInstruction(recipe, output) : null;
        return recipe.map(assemblyRecipe -> new AssemblyInstruction(assemblyRecipe, output)).orElse(null);
    }

    public class AssemblyInstruction implements Comparable<AssemblyInstruction> {
        public final IAssemblyRecipe recipe;
        public final ItemStack output;

        private AssemblyInstruction(IAssemblyRecipe recipe, ItemStack output) {
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
            return recipe.getId().equals(instruction.recipe.getId()) && ItemStack.matches(output, instruction.output);
        }
    }

    // MenuProvider

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerAssemblyTable(BCSiliconMenuTypes.ASSEMBLY_TABLE, id, player, this);
    }

    // Calen: to show current recipe
    @Override
    public ItemStack getAssemblyResult() {
        AssemblyInstruction recipe = this.getActiveRecipe();
        return recipe == null ? StackUtil.EMPTY : recipe.output;
    }
}

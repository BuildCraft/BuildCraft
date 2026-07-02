/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.lib.gui.ItemProvider;
import ct.buildcraft.lib.misc.AdvancementUtil;
import ct.buildcraft.lib.misc.InventoryUtil;
import ct.buildcraft.lib.misc.LocaleUtil;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.net.MessageManager;
import ct.buildcraft.lib.net.MessageUpdateTile;
import ct.buildcraft.lib.recipe.AssemblyRecipeBasic;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.ItemHandlerManager;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.silicon.BCSiliconBlocks;
import ct.buildcraft.silicon.BCSiliconRecipes;
import ct.buildcraft.silicon.EnumAssemblyRecipeState;
import ct.buildcraft.silicon.container.ContainerAssemblyTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class TileAssemblyTable extends TileLaserTableBase implements MenuProvider{
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("assembly_table");
    public static final int NET_RECIPE_STATE = IDS.allocId("RECIPE_STATE");

    public final ItemHandlerSimple inv = itemManager.addInvHandler(
        "inv",
        3 * 4,
        ItemHandlerManager.EnumAccess.BOTH,
        EnumPipePart.VALUES
    );
    public SortedMap<AssemblyInstruction, EnumAssemblyRecipeState> recipesStates = new TreeMap<>();
    public final ItemProvider display = new ItemProvider((i) -> {
    	return i < recipesStates.size() ? new ArrayList<>(recipesStates.keySet()).get(i).output : ItemStack.EMPTY;}, 3 * 4);

    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftsilicon:precision_crafting");
    
    protected boolean isDirty = true;

    public TileAssemblyTable(BlockPos pos, BlockState state) {
    	super(BCSiliconBlocks.ASSEMBLY_TABLE_TILE.get(), pos, state);
    	inv.setCallback((a,b,c,d) -> isDirty = true);
    }
    
    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    private void updateRecipes() {
        int count = recipesStates.size();
        if(isDirty) {
	        for(AssemblyRecipeBasic recipe: level.getRecipeManager().getAllRecipesFor(BCSiliconRecipes.ASSEMBLY_TYPE.get())) {
	            Set<ItemStack> outputs = recipe.getOutputs(inv);
	            for (ItemStack out: outputs) {
	            	if(out.isEmpty())
	            		break;
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
	                }
	            }
	        }
	        isDirty = false;
        }

        boolean findActive = false;
        for (Iterator<Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState>> iterator = recipesStates.entrySet().iterator(); iterator.hasNext();) {
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
            	AssemblyRecipeBasic recipe = entry.getKey().recipe;
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
    	
 //        if(isDirty)
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
	        recipesStates.forEach((instruction, state) -> {
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
            CompoundTag entryTag = recipesStatesTag.getCompound(i);
            String name = entryTag.getString("recipe");
            if (entryTag.contains("output")) {
            	loadingrecipe = ()->{
            		AssemblyInstruction instruction = lookupRecipe(name, ItemStack.of(entryTag.getCompound("output")));//TODO CHECK!
                	if (instruction != null)
                		recipesStates.put(instruction, EnumAssemblyRecipeState.values()[entryTag.getInt("state")]);
            	};
            }
        }
	}
	Runnable loadingrecipe = null;
	
	@Override
	public void onLoad() {
		super.onLoad();
		if(loadingrecipe != null)
		loadingrecipe.run();
	}

	@Override
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);

        if (id == NET_GUI_DATA) {
            buffer.writeInt(recipesStates.size());
            recipesStates.forEach((instruction, state) -> {
                buffer.writeUtf(instruction.recipe.getId().toString());
                buffer.writeItem(instruction.output);
                buffer.writeInt(state.ordinal());
            });
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);

        if (id == NET_GUI_DATA) {
            recipesStates.clear();
            int count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                AssemblyInstruction instruction = lookupRecipe(buffer.readUtf(), buffer.readItem());
                recipesStates.put(instruction, EnumAssemblyRecipeState.values()[buffer.readInt()]);
            }
        }

        if (id == NET_RECIPE_STATE) {
            AssemblyInstruction recipe = lookupRecipe(buffer.readUtf(), buffer.readItem());
            EnumAssemblyRecipeState state = EnumAssemblyRecipeState.values()[buffer.readInt()];
            if (recipesStates.containsKey(recipe)) {
                recipesStates.put(recipe, state);
            }
        }
    }

    public void sendRecipeStateToServer(AssemblyInstruction instruction, EnumAssemblyRecipeState state) {
    	MessageUpdateTile message = createMessage(NET_RECIPE_STATE, (buffer) -> {
            buffer.writeUtf(instruction.recipe.getId().toString());
            buffer.writeItem(instruction.output);
            buffer.writeInt(state.ordinal());
        });
        MessageManager.sendToServer(message);
    }
    
	@Override
	public InteractionResult onActivated(Player player, InteractionHand hand, BlockHitResult hit) {
		if(player instanceof ServerPlayer splayer&&!player.level.isClientSide) {
			NetworkHooks.openScreen(splayer, this, worldPosition);
		}
		return super.onActivated(player, hand, hit);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p_39956_) {
		return new ContainerAssemblyTable(id, inventory, inv, display, ContainerLevelAccess.create(level, worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
	}

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("recipes - " + recipesStates.size());
        left.add("target - " + LocaleUtil.localizeMj(getTarget()));
    }
    
    @Nullable
    private AssemblyInstruction lookupRecipe(String name, ItemStack output) {
        Optional<? extends Recipe<?>> recipe = level.getRecipeManager().byKey(new ResourceLocation(name));
        return (recipe.isPresent() && recipe.get() instanceof AssemblyRecipeBasic assemblyRecipeBasic)
        		? new AssemblyInstruction(assemblyRecipeBasic, output) : null;
    }

    public class AssemblyInstruction implements Comparable<AssemblyInstruction> {
        public final AssemblyRecipeBasic recipe;
        public final ItemStack output;

        private AssemblyInstruction(AssemblyRecipeBasic recipe, ItemStack output) {
            this.recipe = recipe;
            this.output = output;
        }

        @Override
        public int compareTo(AssemblyInstruction o) {
            return recipe.getId().compareTo(o.recipe.getId()) + output.serializeNBT().toString().compareTo(o.output.serializeNBT().toString());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AssemblyInstruction)) return false;
            AssemblyInstruction instruction = (AssemblyInstruction) obj;
            return recipe.getId().equals(instruction.recipe.getId()) && ItemStack.isSame(output, instruction.output);
        }
    }

}

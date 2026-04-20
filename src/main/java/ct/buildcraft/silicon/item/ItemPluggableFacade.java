/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.item;

import java.util.List;
import javax.annotation.Nonnull;

import ct.buildcraft.api.facades.FacadeType;
import ct.buildcraft.api.facades.IFacade;
import ct.buildcraft.api.facades.IFacadeItem;
import ct.buildcraft.api.transport.IItemPluggable;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.lib.misc.BlockUtil;
import ct.buildcraft.lib.misc.LocaleUtil;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.SoundUtil;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.silicon.BCSilicon;
import ct.buildcraft.silicon.BCSiliconPlugs;
import ct.buildcraft.silicon.plug.FacadeBlockStateInfo;
import ct.buildcraft.silicon.plug.FacadeInstance;
import ct.buildcraft.silicon.plug.FacadePhasedState;
import ct.buildcraft.silicon.plug.FacadeStateManager;
import ct.buildcraft.silicon.plug.PluggableFacade;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemPluggableFacade extends Item implements IItemPluggable, IFacadeItem {
    public ItemPluggableFacade() {
        super(new Item.Properties().durability(0).tab(BCSilicon.tabFacades));
//        setHasSubtypes(true);
    }

    @Nonnull
    public ItemStack createItemStack(FacadeInstance state) {
        ItemStack item = new ItemStack(this);
        CompoundTag nbt = NBTUtilBC.getItemData(item);
        nbt.put("facade", state.writeToNbt());
        return item;
    }

    public static FacadeInstance getStates(@Nonnull ItemStack item) {
        CompoundTag nbt = NBTUtilBC.getItemData(item);

        String strPreview = nbt.getString("preview");
        if ("basic".equalsIgnoreCase(strPreview)) {
            return FacadeInstance.createSingle(FacadeStateManager.previewState, false);
        }

        if (!nbt.contains("facade") && nbt.contains("states")) {
            ListTag states = nbt.getList("states", Tag.TAG_COMPOUND);
            if (states.size() > 0) {
                // Only migrate if we actually have a facade to migrate.
                boolean isHollow = states.getCompound(0).getBoolean("isHollow");
                CompoundTag tagFacade = new CompoundTag();
                tagFacade.putBoolean("isHollow", isHollow);
                tagFacade.put("states", states);
                nbt.put("facade", tagFacade);
            }
        }

        return FacadeInstance.readFromNbt(nbt.getCompound("facade"));
    }

    @Nonnull
    @Override
    public ItemStack getFacadeForBlock(BlockState state) {
        FacadeBlockStateInfo info = FacadeStateManager.validFacadeStates.get(state);
        if (info == null) {
            return StackUtil.EMPTY;
        } else {
            return createItemStack(FacadeInstance.createSingle(info, false));
        }
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, Player player,
        InteractionHand hand) {
        FacadeInstance fullState = getStates(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), fullState.phasedStates[0].stateInfo.state);
        return new PluggableFacade(BCSiliconPlugs.facade, holder, side, fullState);
    }
    
    

    @Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems) {
    	if(!this.allowedIn(tab))
    		return;
		// Add a single phased facade as a default
        // check if the data is present as we only process in post-init
        FacadeBlockStateInfo stone = FacadeStateManager.getInfoForBlock(Blocks.STONE);
        if (stone != null) {
            FacadePhasedState[] states = { //
                FacadeStateManager.getInfoForBlock(Blocks.STONE).createPhased(null), //
                FacadeStateManager.getInfoForBlock(Blocks.OAK_PLANKS).createPhased(DyeColor.RED), //
                FacadeStateManager.getInfoForBlock(Blocks.OAK_LOG).createPhased(DyeColor.CYAN),//
            };
            FacadeInstance inst = new FacadeInstance(states, false);
            subItems.add(createItemStack(inst));

            for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
                if (!ForgeRegistries.BLOCKS.containsValue(info.state.getBlock())) {
                    // Forge can de-register blocks if the server a client is connected to
                    // doesn't have the mods that created them.
                    continue;
                }
                if (info.isVisible) {
                    subItems.add(createItemStack(FacadeInstance.createSingle(info, false)));
                    subItems.add(createItemStack(FacadeInstance.createSingle(info, true)));
                }
            }
        }
	}

	@Override
	public Component getName(ItemStack stack) {
        FacadeInstance fullState = getStates(stack);
        if (fullState.type == FacadeType.Basic) {
            String displayName = getFacadeStateDisplayName(fullState.phasedStates[0]);
            return Component.translatable(super.getDescriptionId(stack) + ": " + displayName);
        } else {
            return Component.translatable("item.FacadePhased.name");
        }
	}

    public static String getFacadeStateDisplayName(FacadePhasedState state) {
        ItemStack assumedStack = state.stateInfo.requiredStack;
        return assumedStack.getDisplayName().getString();
    }
    
    

    @Override
    @OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
    	 FacadeInstance states = getStates(stack);
         if (states.type == FacadeType.Phased) {
             String stateString = LocaleUtil.localize("item.FacadePhased.state");
             FacadePhasedState defaultState = null;
             for (FacadePhasedState state : states.phasedStates) {
                 if (state.activeColour == null) {
                     defaultState = state;
                     continue;
                 }
                 tooltip.add(Component.translatable(stateString, state.activeColour,
                     getFacadeStateDisplayName(state)));
             }
             if (defaultState != null) {
                 tooltip.add(1, Component.translatable(("item.FacadePhased.state_default"),
                     getFacadeStateDisplayName(defaultState)));
             }
         } else {
             if (flag.isAdvanced()) {
                 tooltip.add(Component.literal(ForgeRegistries.BLOCKS.getKey(states.phasedStates[0].stateInfo.state.getBlock()).toString()));
             }
             String propertiesStart = ChatFormatting.GRAY + "" + ChatFormatting.ITALIC;
             FacadeBlockStateInfo info = states.phasedStates[0].stateInfo;
             BlockUtil.getPropertiesStringMap(info.state, info.varyingProperties)
                 .forEach((name, value) -> tooltip.add(Component.literal(propertiesStart + name + " = " + value)));
         }
	}

    // IFacadeItem

    @Override
    public ItemStack createFacadeStack(IFacade facade) {
        return createItemStack((FacadeInstance) facade);
    }

    @Override
    public IFacade getFacade(ItemStack facade) {
        return getStates(facade);
    }
}

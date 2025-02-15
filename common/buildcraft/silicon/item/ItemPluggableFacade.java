/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.item;

import buildcraft.api.core.BCLog;
import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.*;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.plug.*;
import com.google.common.base.Stopwatch;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ItemPluggableFacade extends ItemBC_Neptune implements IItemPluggable, IFacadeItem {
    public ItemPluggableFacade(String idBC, Item.Properties properties) {
        super(idBC, properties);
//        setMaxDamage(0);
//        setHasSubtypes(true);
    }

    @Nonnull
    @Override
    public ItemStack createItemStack(IFacade state) {
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
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
        FacadeInstance fullState = getStates(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), fullState.phasedStates[0].stateInfo.state);
        return new PluggableFacade(BCSiliconPlugs.facade, holder, side, fullState);
    }

    @Override
    public void addSubItems(NonNullList<ItemStack> subItems) {
        Stopwatch watch = Stopwatch.createStarted();
        try {
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
        } finally {
            watch.stop();
            long time = watch.elapsed(TimeUnit.MICROSECONDS);
            if (FacadeStateManager.DEBUG) {
                BCLog.logger.info("[silicon.facade] Created ItemStacks for CreativeModeTab. (" + time / 1000 + " ms)");
            }
        }
    }

    @Override
//    public String getItemStackDisplayName(ItemStack stack)
    public Component getName(ItemStack stack) {
        FacadeInstance fullState = getStates(stack);
        if (fullState.type == FacadeType.Basic) {
//            String displayName = getFacadeStateDisplayName(fullState.phasedStates[0]);
            Component displayName = getFacadeStateDisplayName(fullState.phasedStates[0]);
//            return super.getItemStackDisplayName(stack) + ": " + displayName;
            return ((MutableComponent) super.getName(stack)).append(": ").append(displayName);
        } else {
//            return LocaleUtil.localize("item.FacadePhased.name");
            return Component.translatable("item.FacadePhased.name");
        }
    }

    // public static String getFacadeStateDisplayName(FacadePhasedState state)
    public static Component getFacadeStateDisplayName(FacadePhasedState state) {
        ItemStack assumedStack = state.stateInfo.requiredStack;
        return assumedStack.getDisplayName();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
//    public void addInformation(ItemStack stack, Level world, List<String> tooltip, ITooltipFlag flag)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        FacadeInstance states = getStates(stack);
        if (states.type == FacadeType.Phased) {
//            String stateString = LocaleUtil.localize("item.FacadePhased.state");
            FacadePhasedState defaultState = null;
            for (FacadePhasedState state : states.phasedStates) {
                if (state.activeColour == null) {
                    defaultState = state;
                    continue;
                }
//                tooltip.add(String.format(stateString, LocaleUtil.localizeColour(state.activeColour), getFacadeStateDisplayName(state)));
                tooltip.add(Component.translatable("item.FacadePhased.state", LocaleUtil.localizeColour(state.activeColour), getFacadeStateDisplayName(state)));
            }
            if (defaultState != null) {
//                tooltip.add(1, String.format(LocaleUtil.localize("item.FacadePhased.state_default"), getFacadeStateDisplayName(defaultState)));
                tooltip.add(1, Component.translatable("item.FacadePhased.state_default", getFacadeStateDisplayName(defaultState)));
            }
        } else {
            if (flag.isAdvanced()) {
                tooltip.add(Component.literal(BlockUtil.getRegistryName(states.phasedStates[0].stateInfo.state.getBlock()).toString()));
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

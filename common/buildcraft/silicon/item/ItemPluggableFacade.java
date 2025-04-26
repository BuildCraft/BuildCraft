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
import buildcraft.silicon.BCSiliconConfig;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.plug.*;
import com.google.common.base.Stopwatch;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
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
        CompoundNBT nbt = NBTUtilBC.getItemData(item);
        nbt.put("facade", state.writeToNbt());
        return item;
    }

    public static FacadeInstance getStates(@Nonnull ItemStack item) {
        CompoundNBT nbt = NBTUtilBC.getItemData(item);

        String strPreview = nbt.getString("preview");
        if ("basic".equalsIgnoreCase(strPreview)) {
            return FacadeInstance.createSingle(FacadeStateManager.previewState, false);
        }

        if (!nbt.contains("facade") && nbt.contains("states")) {
            ListNBT states = nbt.getList("states", Constants.NBT.TAG_COMPOUND);
            if (states.size() > 0) {
                // Only migrate if we actually have a facade to migrate.
                boolean isHollow = states.getCompound(0).getBoolean("isHollow");
                CompoundNBT tagFacade = new CompoundNBT();
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
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, PlayerEntity player, Hand hand) {
        FacadeInstance fullState = getStates(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), fullState.phasedStates[0].stateInfo.state);
        return new PluggableFacade(BCSiliconPlugs.facade, holder, side, fullState);
    }

    @Override
    public void addSubItems(ItemGroup tab, NonNullList<ItemStack> subItems) {
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
                    // Calen
                    if (!BCSiliconConfig.isFacadeBlockIdAllowedInCreativeModTabByConfig(info.state.getBlock())) {
                        continue;
                    }
                    // 1.12.2
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
                BCLog.logger.info("[silicon.facade] Created ItemStacks for ItemGroup. (" + time / 1000 + " ms)");
            }
        }
    }

    @Override
//    public String getItemStackDisplayName(ItemStack stack)
    public ITextComponent getName(ItemStack stack) {
        FacadeInstance fullState = getStates(stack);
        if (fullState.type == FacadeType.Basic) {
//            String displayName = getFacadeStateDisplayName(fullState.phasedStates[0]);
            ITextComponent displayName = getFacadeStateDisplayName(fullState.phasedStates[0]);
//            return super.getItemStackDisplayName(stack) + ": " + displayName;
            return ((IFormattableTextComponent) super.getName(stack)).append(": ").append(displayName);
        } else {
//            return LocaleUtil.localize("item.FacadePhased.name");
            return new TranslationTextComponent("item.FacadePhased.name");
        }
    }

    // public static String getFacadeStateDisplayName(FacadePhasedState state)
    public static ITextComponent getFacadeStateDisplayName(FacadePhasedState state) {
        ItemStack assumedStack = state.stateInfo.requiredStack;
        return assumedStack.getDisplayName();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
//    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag)
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
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
                tooltip.add(new TranslationTextComponent("item.FacadePhased.state", LocaleUtil.localizeColour(state.activeColour), getFacadeStateDisplayName(state)));
            }
            if (defaultState != null) {
//                tooltip.add(1, String.format(LocaleUtil.localize("item.FacadePhased.state_default"), getFacadeStateDisplayName(defaultState)));
                tooltip.add(1, new TranslationTextComponent("item.FacadePhased.state_default", getFacadeStateDisplayName(defaultState)));
            }
        } else {
            if (flag.isAdvanced()) {
                tooltip.add(new StringTextComponent(states.phasedStates[0].stateInfo.state.getBlock().getRegistryName().toString()));
            }
            String propertiesStart = TextFormatting.GRAY + "" + TextFormatting.ITALIC;
            FacadeBlockStateInfo info = states.phasedStates[0].stateInfo;
            BlockUtil.getPropertiesStringMap(info.state, info.varyingProperties)
                    .forEach((name, value) -> tooltip.add(new StringTextComponent(propertiesStart + name + " = " + value)));
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

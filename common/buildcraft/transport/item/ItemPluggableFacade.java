/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.plug.FacadeStateManager;
import buildcraft.transport.plug.FacadeStateManager.FacadeBlockStateInfo;
import buildcraft.transport.plug.FacadeStateManager.FacadePhasedState;
import buildcraft.transport.plug.FacadeStateManager.FullFacadeInstance;
import buildcraft.transport.plug.PluggableFacade;

public class ItemPluggableFacade extends ItemBC_Neptune implements IItemPluggable, IFacadeItem {
    public ItemPluggableFacade(String id) {
        super(id);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Nonnull
    public ItemStack createItemStack(FullFacadeInstance instance) {
        ItemStack item = new ItemStack(this);
        NBTTagCompound nbt = NBTUtilBC.getItemData(item);
        instance.writeToNbt(nbt, "states");
        return item;
    }

    public static FullFacadeInstance getInstance(@Nonnull ItemStack item) {
        return FullFacadeInstance.readFromNbt(NBTUtilBC.getItemData(item), "states");
    }

    @Override
    public FacadeType getFacadeType(@Nonnull ItemStack facade) {
        return getInstance(facade).type;
    }

    @Nonnull
    @Override
    public ItemStack getFacadeForBlock(IBlockState state) {
        FacadeBlockStateInfo info = FacadeStateManager.getStateInfo(state);
        if (info == null) {
            return StackUtil.EMPTY;
        } else {
            return createItemStack(FullFacadeInstance.createSingle(info, false));
        }
    }

    @Override
    public IBlockState[] getBlockStatesForFacade(@Nonnull ItemStack facade) {
        FullFacadeInstance info = getInstance(facade);
        IBlockState[] states = new IBlockState[info.phasedStates.length];
        for (int i = 0; i < states.length; i++) {
            states[i] = info.phasedStates[i].stateInfo.state;
        }
        return states;
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, EnumFacing side, EntityPlayer player, EnumHand hand) {
        FullFacadeInstance fullState = getInstance(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), fullState.phasedStates[0].stateInfo.state);
        return new PluggableFacade(BCTransportPlugs.facade, holder, side, fullState);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        // Add a single phased facade as a default
        subItems.add(createItemStack(new FullFacadeInstance(new FacadePhasedState[] {
            new FacadePhasedState(
                new FacadeBlockStateInfo(
                    Blocks.STONE.getDefaultState(),
                    new ItemStack(Blocks.STONE)
                ),
                false,
                null
            ),
            new FacadePhasedState(
                new FacadeBlockStateInfo(
                    Blocks.PLANKS.getDefaultState(),
                    new ItemStack(Blocks.PLANKS)
                ),
                false,
                EnumDyeColor.RED
            ),
            new FacadePhasedState(
                new FacadeBlockStateInfo(
                    Blocks.LOG.getDefaultState(),
                    new ItemStack(Blocks.LOG)
                ),
                false,
                EnumDyeColor.CYAN
            ),
        })));
        for (FacadeBlockStateInfo stateInfo : FacadeStateManager.PREVIEW_STATE_INFOS) {
            subItems.add(createItemStack(FullFacadeInstance.createSingle(stateInfo, false)));
            subItems.add(createItemStack(FullFacadeInstance.createSingle(stateInfo, true)));
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FullFacadeInstance fullState = getInstance(stack);
        if (fullState.type == FacadeType.Basic) {
            String displayName = getFacadeStateDisplayName(fullState.phasedStates[0]);
            return super.getItemStackDisplayName(stack) + ": " + displayName;
        } else {
            return LocaleUtil.localize("item.FacadePhased.name");
        }
    }

    public static String getFacadeStateDisplayName(FacadePhasedState state) {
        ItemStack assumedStack = state.stateInfo.requiredStack;
        String s = assumedStack.getDisplayName();
        if (state.isHollow) {
            s += " (" + LocaleUtil.localize("item.Facade.state_hollow") + ")";
        }
        return s;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        FullFacadeInstance states = getInstance(stack);
        // for (FacadePhasedState state : states.phasedStates) {
        // ItemStack requiredStack = state.stateInfo.requiredStack;
        // requiredStack.getItem().addInformation(requiredStack, player, tooltip, advanced);
        // }
        if (states.type == FacadeType.Phased) {
            String stateString = LocaleUtil.localize("item.FacadePhased.state");
            FacadePhasedState defaultState = null;
            for (FacadePhasedState state : states.phasedStates) {
                if (state.activeColour == null) {
                    defaultState = state;
                    continue;
                }
                tooltip.add(String.format(stateString, LocaleUtil.localizeColour(state.activeColour), getFacadeStateDisplayName(state)));
            }
            if (defaultState != null) {
                tooltip.add(1, String.format(LocaleUtil.localize("item.FacadePhased.state_default"), getFacadeStateDisplayName(defaultState)));
            }
        }
    }
}

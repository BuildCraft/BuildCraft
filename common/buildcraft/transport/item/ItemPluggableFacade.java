/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.plug.FacadeBlockStateInfo;
import buildcraft.transport.plug.FacadeInstance;
import buildcraft.transport.plug.FacadePhasedState;
import buildcraft.transport.plug.FacadeStateManager;
import buildcraft.transport.plug.PluggableFacade;

public class ItemPluggableFacade extends ItemBC_Neptune implements IItemPluggable, IFacadeItem {
    public ItemPluggableFacade(String id) {
        super(id);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Nonnull
    public ItemStack createItemStack(FacadeInstance state) {
        ItemStack item = new ItemStack(this);
        NBTTagCompound nbt = NBTUtilBC.getItemData(item);
        nbt.setTag("facade", state.writeToNbt());
        return item;
    }

    public static FacadeInstance getStates(@Nonnull ItemStack item) {
        NBTTagCompound nbt = NBTUtilBC.getItemData(item);

        String strPreview = nbt.getString("preview");
        if ("basic".equalsIgnoreCase(strPreview)) {
            return FacadeInstance.createSingle(FacadeStateManager.previewState, false);
        }

        if (!nbt.hasKey("facade") && nbt.hasKey("states")) {
            NBTTagList states = nbt.getTagList("states", Constants.NBT.TAG_COMPOUND);
            if (states.tagCount() > 0) {
                // Only migrate if we actually have a facade to migrate.
                boolean isHollow = states.getCompoundTagAt(0).getBoolean("isHollow");
                NBTTagCompound tagFacade = new NBTTagCompound();
                tagFacade.setBoolean("isHollow", isHollow);
                tagFacade.setTag("states", states);
                nbt.setTag("facade", tagFacade);
            }
        }

        return FacadeInstance.readFromNbt(nbt.getCompoundTag("facade"));
    }

    @Nonnull
    @Override
    public ItemStack getFacadeForBlock(IBlockState state) {
        FacadeBlockStateInfo info = FacadeStateManager.validFacadeStates.get(state);
        if (info == null) {
            return StackUtil.EMPTY;
        } else {
            return createItemStack(FacadeInstance.createSingle(info, false));
        }
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, EnumFacing side, EntityPlayer player,
        EnumHand hand) {
        FacadeInstance fullState = getStates(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), fullState.phasedStates[0].stateInfo.state);
        return new PluggableFacade(BCTransportPlugs.facade, holder, side, fullState);
    }

    @Override
    public void addSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        // Add a single phased facade as a default
        // check if the data is present as we only process in post-init
        FacadeBlockStateInfo stone = FacadeStateManager.getInfoForBlock(Blocks.STONE);
        if (stone != null) {
            FacadePhasedState[] states = { //
                FacadeStateManager.getInfoForBlock(Blocks.STONE).createPhased(null), //
                FacadeStateManager.getInfoForBlock(Blocks.PLANKS).createPhased(EnumDyeColor.RED), //
                FacadeStateManager.getInfoForBlock(Blocks.LOG).createPhased(EnumDyeColor.CYAN),//
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
    public String getItemStackDisplayName(ItemStack stack) {
        FacadeInstance fullState = getStates(stack);
        if (fullState.type == FacadeType.Basic) {
            String displayName = getFacadeStateDisplayName(fullState.phasedStates[0]);
            return super.getItemStackDisplayName(stack) + ": " + displayName;
        } else {
            return LocaleUtil.localize("item.FacadePhased.name");
        }
    }

    public static String getFacadeStateDisplayName(FacadePhasedState state) {
        ItemStack assumedStack = state.stateInfo.requiredStack;
        return assumedStack.getDisplayName();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        FacadeInstance states = getStates(stack);
        if (states.type == FacadeType.Phased) {
            String stateString = LocaleUtil.localize("item.FacadePhased.state");
            FacadePhasedState defaultState = null;
            for (FacadePhasedState state : states.phasedStates) {
                if (state.activeColour == null) {
                    defaultState = state;
                    continue;
                }
                tooltip.add(String.format(stateString, LocaleUtil.localizeColour(state.activeColour),
                    getFacadeStateDisplayName(state)));
            }
            if (defaultState != null) {
                tooltip.add(1, String.format(LocaleUtil.localize("item.FacadePhased.state_default"),
                    getFacadeStateDisplayName(defaultState)));
            }
        } else {
            String propertiesStart = TextFormatting.GRAY + "" + TextFormatting.ITALIC;
            FacadeBlockStateInfo info = states.phasedStates[0].stateInfo;
            BlockUtil.getPropertiesStringMap(info.state, info.varyingProperties)
                .forEach((name, value) -> tooltip.add(propertiesStart + name + " = " + value));
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

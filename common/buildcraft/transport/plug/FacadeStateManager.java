/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.plug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.facades.IFacadeRegistry;
import buildcraft.api.facades.IFacadeState;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;

import buildcraft.transport.recipe.FacadeSwapRecipe;

public enum FacadeStateManager implements IFacadeRegistry {
    INSTANCE;

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("transport.facade");
    public static final SortedMap<IBlockState, FacadeBlockStateInfo> validFacadeStates;
    public static final Map<ItemStackKey, List<FacadeBlockStateInfo>> stackFacades;
    public static FacadeBlockStateInfo defaultState, previewState;

    private static final String STR_SUCCESS = "success";
    private static final String STR_PASS = "pass";

    private static final Map<Block, String> disabledBlocks = new HashMap<>();
    private static final Map<IBlockState, ItemStack> customBlocks = new HashMap<>();

    static {
        validFacadeStates = new TreeMap<>(BlockUtil.blockStateComparator());
        stackFacades = new HashMap<>();
    }

    public static FacadeBlockStateInfo getInfoForBlock(Block block) {
        return getInfoForState(block.getDefaultState());
    }

    private static FacadeBlockStateInfo getInfoForState(IBlockState state) {
        return validFacadeStates.get(state);
    }

    public static void receiveInterModComms(IMCMessage message) {
        String id = message.key;
        if (FacadeAPI.IMC_FACADE_DISABLE.equals(id)) {
            if (!message.isResourceLocationMessage()) {
                BCLog.logger.warn("[facade.imc] Received an invalid IMC message from " + message.getSender() + " - "
                    + id + " should have a resourcelocation value, not a " + message);
                return;
            }
            ResourceLocation loc = message.getResourceLocationValue();
            Block block = Block.REGISTRY.getObject(loc);
            if (block == Blocks.AIR) {
                BCLog.logger.warn("[facade.imc] Received an invalid IMC message from " + message.getSender() + " - "
                    + id + " should have a valid block target, not " + block + " (" + message + ")");
                return;
            }
            disabledBlocks.put(block, message.getSender());
        } else if (FacadeAPI.IMC_FACADE_CUSTOM.equals(id)) {
            if (!message.isNBTMessage()) {
                BCLog.logger.warn("[facade.imc] Received an invalid IMC message from " + message.getSender() + " - "
                    + id + " should have an nbt value, not a " + message);
                return;
            }
            NBTTagCompound nbt = message.getNBTValue();
            String regName = nbt.getString(FacadeAPI.NBT_CUSTOM_BLOCK_REG_KEY);
            int meta = nbt.getInteger(FacadeAPI.NBT_CUSTOM_BLOCK_META);
            ItemStack stack = new ItemStack(nbt.getCompoundTag(FacadeAPI.NBT_CUSTOM_ITEM_STACK));
            if (regName.isEmpty()) {
                BCLog.logger.warn("[facade.imc] Received an invalid IMC message from " + message.getSender() + " - "
                    + id + " should have a registry name for the block, stored as "
                    + FacadeAPI.NBT_CUSTOM_BLOCK_REG_KEY);
                return;
            }
            if (stack.isEmpty()) {
                BCLog.logger.warn("[facade.imc] Received an invalid IMC message from " + message.getSender() + " - "
                    + id + " should have a valid ItemStack stored in " + FacadeAPI.NBT_CUSTOM_ITEM_STACK);
                return;
            }
            Block block = Block.REGISTRY.getObject(new ResourceLocation(regName));
            if (block == Blocks.AIR) {
                BCLog.logger.warn("[facade.imc] Received an invalid IMC message from " + message.getSender() + " - "
                    + id + " should have a valid block target, not " + block + " (" + message + ")");
                return;
            }
            IBlockState state = block.getStateFromMeta(meta);
            customBlocks.put(state, stack);
        }
    }

    /** @return One of:
     *         <ul>
     *         <li>A string describing the problem with this block (if it is not valid for a facade)</li>
     *         <li>OR {@link #STR_PASS} if every metadata needs to be checked by
     *         {@link #isValidFacadeState(IBlockState)}</li>
     *         <li>OR {@link #STR_SUCCESS} if every state of the block is valid for a facade.
     *         </ul>
    */
    private static String isValidFacadeBlock(Block block) {
        String disablingMod = disabledBlocks.get(block);
        if (disablingMod != null) {
            return "it has been disabled by " + disablingMod;
        }
        if (block instanceof IFluidBlock || block instanceof BlockLiquid) {
            return "it is a fluid block";
        }
        // if (block instanceof BlockSlime) {
        // return "it is a slime block";
        // }
        if (block instanceof BlockGlass || block instanceof BlockStainedGlass) {
            return STR_SUCCESS;
        }
        return STR_PASS;
    }

    /** @return Any of:
     *         <ul>
     *         <li>A string describing the problem with this state (if it is not valid for a facade)</li>
     *         <li>OR {@link #STR_SUCCESS} if this state is valid for a facade.
     *         </ul>
    */
    private static String isValidFacadeState(IBlockState state) {
        if (state.getBlock().hasTileEntity(state)) {
            return "it has a tile entity";
        }
        if (state.getRenderType() != EnumBlockRenderType.MODEL) {
            return "it doesn't have a normal model";
        }
        if (!state.isFullCube()) {
            return "it isn't a full cube";
        }
        return STR_SUCCESS;
    }

    @Nonnull
    private static ItemStack getRequiredStack(IBlockState state) {
        ItemStack stack = customBlocks.get(state);
        if (stack != null) {
            return stack;
        }
        Block block = state.getBlock();
        // Inlined block.getPickStack(), but this doesn't require a world
        return new ItemStack(block, 1, block.damageDropped(state));
    }

    public static void postInit() {
        defaultState = new FacadeBlockStateInfo(Blocks.AIR.getDefaultState(), StackUtil.EMPTY, ImmutableSet.of());
        for (Block block : ForgeRegistries.BLOCKS) {
            String result = isValidFacadeBlock(block);
            // These strings are hardcoded, so we can get away with not needing the .equals check
            if (!result.equals(STR_PASS) && !result.equals(STR_SUCCESS)) {
                if (DEBUG) {
                    BCLog.logger
                        .info("[transport.facade] Disallowed block " + block.getRegistryName() + " because " + result);
                }
                continue;
            } else if (DEBUG) {
                if (result.equals(STR_SUCCESS)) {
                    BCLog.logger.info("[transport.facade] Allowed block " + block.getRegistryName());
                }
            }
            Set<IBlockState> checkedStates = new HashSet<>();
            Map<IBlockState, ItemStack> usedStates = new HashMap<>();
            Map<ItemStackKey, Map<IProperty<?>, Comparable<?>>> varyingProperties = new HashMap<>();
            for (IBlockState state : block.getBlockState().getValidStates()) {
                state = block.getStateFromMeta(block.getMetaFromState(state));
                if (!checkedStates.add(state)) {
                    continue;
                }
                if (!result.equals(STR_SUCCESS)) {
                    result = isValidFacadeState(state);
                    if (result.equals(STR_SUCCESS)) {
                        if (DEBUG) {
                            BCLog.logger.info("[transport.facade] Allowed state " + state);
                        }
                    } else {
                        if (DEBUG) {
                            BCLog.logger.info("[transport.facade] Disallowed state " + state + " because " + result);
                        }
                        continue;
                    }
                }
                ItemStack stack = getRequiredStack(state);
                usedStates.put(state, stack);
                ItemStackKey stackKey = new ItemStackKey(stack);
                Map<IProperty<?>, Comparable<?>> vars = varyingProperties.get(stackKey);
                if (vars == null) {
                    vars = new HashMap<>();
                    vars.putAll(state.getProperties());
                    varyingProperties.put(stackKey, vars);
                } else {
                    for (Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
                        IProperty<?> prop = entry.getKey();
                        Comparable<?> value = entry.getValue();
                        if (vars.get(prop) != value) {
                            vars.put(prop, null);
                        }
                    }
                }
            }
            for (Entry<IBlockState, ItemStack> entry : usedStates.entrySet()) {
                IBlockState state = entry.getKey();
                ItemStack stack = entry.getValue();
                Map<IProperty<?>, Comparable<?>> vars = varyingProperties.get(new ItemStackKey(stack));
                vars.values().removeIf(Objects::nonNull);
                FacadeBlockStateInfo info = new FacadeBlockStateInfo(state, stack, ImmutableSet.copyOf(vars.keySet()));
                validFacadeStates.put(state, info);
                if (!info.requiredStack.isEmpty()) {
                    ItemStackKey stackKey = new ItemStackKey(info.requiredStack);
                    stackFacades.computeIfAbsent(stackKey, k -> new ArrayList<>()).add(info);
                }
            }
        }
        previewState = validFacadeStates.get(Blocks.BRICK_BLOCK.getDefaultState());
        FacadeSwapRecipe.genRecipes();
    }

    // IFacadeRegistry

    @Override
    public Collection<? extends IFacadeState> getValidFacades() {
        return validFacadeStates.values();
    }

    @Override
    public IFacadePhasedState createPhasedState(IFacadeState state, boolean isHollow, EnumDyeColor activeColor) {
        return new FacadePhasedState((FacadeBlockStateInfo) state, isHollow, activeColor);
    }

    @Override
    public IFacade createPhasedFacade(IFacadePhasedState[] states) {
        FacadePhasedState[] realStates = new FacadePhasedState[states.length];
        for (int i = 0; i < states.length; i++) {
            realStates[i] = (FacadePhasedState) states[i];
        }
        return new FacadeInstance(realStates);
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.plug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import io.netty.buffer.Unpooled;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
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

import buildcraft.lib.BCLib;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.recipe.FacadeSwapRecipe;

public enum FacadeStateManager implements IFacadeRegistry {
    INSTANCE;

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("transport.facade");
    public static final SortedMap<IBlockState, FacadeBlockStateInfo> validFacadeStates;
    public static final Map<ItemStackKey, List<FacadeBlockStateInfo>> stackFacades;
    public static FacadeBlockStateInfo defaultState, previewState;

    private static final Map<Block, String> disabledBlocks = new HashMap<>();
    private static final Map<IBlockState, ItemStack> customBlocks = new HashMap<>();

    /** An array containing all mods that fail the {@link #doesPropertyConform(IProperty)} check, and any others.
     * <p>
     * Note: Mods should ONLY be added to this list AFTER it has been reported to them, and taken off the list once a
     * version has been released with the fix. */
    private static final List<String> KNOWN_INVALID_REPORTED_MODS = Arrays.asList(new String[] { //
    });

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
     *         <li>{@link EnumActionResult#SUCCESS} if every state of the block is valid for a facade.
     *         <li>{@link EnumActionResult#PASS} if every metadata needs to be checked by
     *         {@link #isValidFacadeState(IBlockState)}</li>
     *         <li>{@link EnumActionResult#FAIL} with string describing the problem with this block (if it is not valid
     *         for a facade)</li>
     *         </ul>
     */
    private static ActionResult<String> isValidFacadeBlock(Block block) {
        String disablingMod = disabledBlocks.get(block);
        if (disablingMod != null) {
            return new ActionResult<>(EnumActionResult.FAIL, "it has been disabled by " + disablingMod);
        }
        if (block instanceof IFluidBlock || block instanceof BlockLiquid) {
            return new ActionResult<>(EnumActionResult.FAIL, "it is a fluid block");
        }
        // if (block instanceof BlockSlime) {
        // return "it is a slime block";
        // }
        if (block instanceof BlockGlass || block instanceof BlockStainedGlass) {
            return new ActionResult<>(EnumActionResult.SUCCESS, "");
        }
        return new ActionResult<>(EnumActionResult.PASS, "");
    }

    /** @return Any of:
     *         <ul>
     *         <li>{@link EnumActionResult#SUCCESS} if this state is valid for a facade.
     *         <li>{@link EnumActionResult#FAIL} with string describing the problem with this state (if it is not valid
     *         for a facade)</li>
     *         </ul>
     */
    private static ActionResult<String> isValidFacadeState(IBlockState state) {
        if (state.getBlock().hasTileEntity(state)) {
            return new ActionResult<>(EnumActionResult.FAIL, "it has a tile entity");
        }
        if (state.getRenderType() != EnumBlockRenderType.MODEL) {
            return new ActionResult<>(EnumActionResult.FAIL, "it doesn't have a normal model");
        }
        if (!state.isFullCube()) {
            return new ActionResult<>(EnumActionResult.FAIL, "it isn't a full cube");
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, "");
    }

    @Nonnull
    private static ItemStack getRequiredStack(IBlockState state) {
        ItemStack stack = customBlocks.get(state);
        if (stack != null) {
            return stack;
        }
        Block block = state.getBlock();
        Item item = Item.getItemFromBlock(block);
        if (item == Items.AIR) {
            item = block.getItemDropped(state, new Random(0), 0);
        }
        return new ItemStack(item, 1, block.damageDropped(state));
    }

    public static void init() {
        defaultState = new FacadeBlockStateInfo(Blocks.AIR.getDefaultState(), StackUtil.EMPTY, ImmutableSet.of());
        for (Block block : ForgeRegistries.BLOCKS) {
            if (!DEBUG && KNOWN_INVALID_REPORTED_MODS.contains(block.getRegistryName().getResourceDomain())) {
                if (BCLib.VERSION.startsWith("7.99")) {
                    BCLog.logger.warn(
                        "[transport.facade] Skipping " + block + " as it has been added to the list of broken mods!");
                    continue;
                }
            }

            // Check to make sure that all the properties work properly
            // Fixes a bug in extra utilities who doesn't serialise and deserialise properties properly

            boolean allPropertiesOk = true;
            for (IProperty<?> property : block.getBlockState().getProperties()) {
                allPropertiesOk &= doesPropertyConform(property);
            }
            if (!allPropertiesOk) {
                continue;
            }

            ActionResult<String> result = isValidFacadeBlock(block);
            // These strings are hardcoded, so we can get away with not needing the .equals check
            if (result.getType() != EnumActionResult.PASS && result.getType() != EnumActionResult.SUCCESS) {
                if (DEBUG) {
                    BCLog.logger
                        .info("[transport.facade] Disallowed block " + block.getRegistryName() + " because " + result);
                }
                continue;
            } else if (DEBUG) {
                if (result.getType() == EnumActionResult.SUCCESS) {
                    BCLog.logger.info("[transport.facade] Allowed block " + block.getRegistryName());
                }
            }
            Set<IBlockState> checkedStates = new HashSet<>();
            Map<IBlockState, ItemStack> usedStates = new HashMap<>();
            Map<ItemStackKey, Map<IProperty<?>, Comparable<?>>> varyingProperties = new HashMap<>();
            for (IBlockState state : block.getBlockState().getValidStates()) {
                // state = block.getStateFromMeta(block.getMetaFromState(state));
                // if (!checkedStates.add(state)) {
                // continue;
                // }
                if (result.getType() != EnumActionResult.SUCCESS) {
                    result = isValidFacadeState(state);
                    if (result.getType() == EnumActionResult.SUCCESS) {
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
                    vars = new HashMap<>(state.getProperties());
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
            PacketBufferBC testingBuffer = PacketBufferBC.asPacketBufferBc(Unpooled.buffer());
            varyingProperties.forEach((key, vars) -> {
                if (DEBUG) {
                    BCLog.logger.info("[transport.facade]   pre-" + key + ":");
                    vars.keySet().forEach(p -> BCLog.logger.info("[transport.facade]       " + p));
                }
                vars.values().removeIf(Objects::nonNull);
                if (DEBUG && !vars.isEmpty()) {
                    BCLog.logger.info("[transport.facade]   " + key + ":");
                    vars.keySet().forEach(p -> BCLog.logger.info("[transport.facade]       " + p));
                }
            });
            for (Entry<IBlockState, ItemStack> entry : usedStates.entrySet()) {
                IBlockState state = entry.getKey();
                ItemStack stack = entry.getValue();
                Map<IProperty<?>, Comparable<?>> vars = varyingProperties.get(new ItemStackKey(stack));
                try {
                    ImmutableSet<IProperty<?>> varSet = ImmutableSet.copyOf(vars.keySet());
                    FacadeBlockStateInfo info = new FacadeBlockStateInfo(state, stack, varSet);
                    validFacadeStates.put(state, info);
                    if (!info.requiredStack.isEmpty()) {
                        ItemStackKey stackKey = new ItemStackKey(info.requiredStack);
                        stackFacades.computeIfAbsent(stackKey, k -> new ArrayList<>()).add(info);
                    }

                    // Test to make sure that we can read + write it
                    FacadePhasedState phasedState = info.createPhased(false, null);
                    NBTTagCompound nbt = phasedState.writeToNbt();
                    FacadePhasedState read = FacadePhasedState.readFromNbt(nbt);
                    if (read.stateInfo != info) {
                        throw new IllegalStateException("Read (from NBT) state was different! (\n\t" + read.stateInfo
                            + "\n !=\n\t" + info + "\n\tNBT = " + nbt + "\n)");
                    }
                    phasedState.writeToBuffer(testingBuffer);
                    read = FacadePhasedState.readFromBuffer(testingBuffer);
                    if (read.stateInfo != info) {
                        throw new IllegalStateException("Read (from buffer) state was different! (\n\t" + read.stateInfo
                            + "\n !=\n\t" + info + "\n)");
                    }
                    testingBuffer.clear();
                    if (DEBUG) {
                        BCLog.logger.info("[transport.facade]   Added " + info);
                    }
                } catch (Throwable t) {
                    String msg = "Scanning facade states";
                    msg += "\n\tState = " + state;
                    msg += "\n\tBlock = " + safeToString(() -> state.getBlock().getRegistryName());
                    msg += "\n\tStack = " + stack;
                    msg += "\n\tvarying-properties: {";
                    for (Entry<IProperty<?>, Comparable<?>> varEntry : vars.entrySet()) {
                        msg += "\n\t\t" + varEntry.getKey() + " = " + varEntry.getValue();
                    }
                    msg += "\n\t}";
                    throw new IllegalStateException(msg.replace("\t", "    "), t);
                }
            }
        }
        previewState = validFacadeStates.get(Blocks.BRICK_BLOCK.getDefaultState());
        FacadeSwapRecipe.genRecipes();
    }

    private static <V extends Comparable<V>> boolean doesPropertyConform(IProperty<V> property) {
        try {
            property.parseValue("");
        } catch (AbstractMethodError error) {
            String message = "Invalid IProperty object detected!";
            message += "\n  Class = " + property.getClass();
            message += "\n  Method not overriden: IProperty.parseValue(String)";
            RuntimeException exception = new RuntimeException(message, error);
            if (BCLib.DEV || !BCLib.MC_VERSION.equals("1.12.2")) {
                throw exception;
            } else {
                BCLog.logger.error("[transport.facade] Invalid property!", exception);
            }
            return false;
        }

        boolean allFine = true;
        for (V value : property.getAllowedValues()) {
            String name = property.getName(value);
            Optional<V> optional = property.parseValue(name);
            V parsed = optional == null ? null : optional.orNull();
            if (!Objects.equals(value, parsed)) {
                allFine = false;
                // A property is *wrong*
                // this is a big problem
                String message = "Invalid property value detected!";
                message += "\n  Property class = " + property.getClass();
                message += "\n  Property = " + property;
                message += "\n  Possible Values = " + property.getAllowedValues();
                message += "\n  Value Name = " + name;
                message += "\n  Value (original) = " + value;
                message += "\n  Value (parsed) = " + parsed;
                message += "\n  Value class (original) = " + (value == null ? null : value.getClass());
                message += "\n  Value class (parsed) = " + (parsed == null ? null : parsed.getClass());
                if (optional == null) {
                    // Massive issue
                    message += "\n  IProperty.parseValue() -> Null com.google.common.base.Optional!!";
                }
                message += "\n";
                // This check *intentionally* crashes on a new MC version
                // or in a dev environment
                // as this really needs to be fixed
                RuntimeException exception = new RuntimeException(message);
                if (BCLib.DEV || !BCLib.MC_VERSION.equals("1.12.2")) {
                    throw exception;
                } else {
                    BCLog.logger.error("[transport.facade] Invalid property!", exception);
                }
            }
        }
        return allFine;
    }

    private static String safeToString(Callable<Object> callable) {
        try {
            return Objects.toString(callable.call());
        } catch (Throwable t) {
            return "~~ERROR~~" + t.getMessage();
        }
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

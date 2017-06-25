/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.plug;

import java.util.ArrayList;
import java.util.Arrays;
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
import javax.annotation.Nullable;

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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.facades.FacadeType;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.world.SingleBlockAccess;

public class FacadeStateManager {
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
     *         <li>A string describing the problem with this state (if it is not valid for a facade)
     *         </li>
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
            if (!Objects.equals(result, STR_PASS) && !Objects.equals(result, STR_SUCCESS)) {
                if (DEBUG) {
                    BCLog.logger
                        .info("[transport.facade] Disallowed block " + block.getRegistryName() + " because " + result);
                }
                continue;
            } else if (DEBUG) {
                if (Objects.equals(result, STR_SUCCESS)) {
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
                if (!Objects.equals(result, STR_SUCCESS)) {
                    result = isValidFacadeState(state);
                    if (Objects.equals(result, STR_SUCCESS)) {
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
    }

    public static class FacadeBlockStateInfo {
        public final IBlockState state;
        public final ItemStack requiredStack;
        public final ImmutableSet<IProperty<?>> varyingProperties;
        public final boolean isTransparent;
        public final boolean isVisible;
        public final boolean[] isSideSolid = new boolean[6];

        public FacadeBlockStateInfo(
            IBlockState state,
            ItemStack requiredStack,
            ImmutableSet<IProperty<?>> varyingProperties) {
            this.state = state;
            this.requiredStack = requiredStack;
            this.varyingProperties = varyingProperties;
            this.isTransparent = !state.isOpaqueCube();
            this.isVisible = !requiredStack.isEmpty();
            IBlockAccess access = new SingleBlockAccess(state);
            for (EnumFacing side : EnumFacing.VALUES) {
                isSideSolid[side.ordinal()] = state.isSideSolid(access, BlockPos.ORIGIN, side);
            }
        }

        // Helper methods

        public FacadePhasedState createPhased(boolean isHollow, EnumDyeColor activeColour) {
            return new FacadePhasedState(this, isHollow, activeColour);
        }
    }

    public static class FacadePhasedState {
        public final FacadeBlockStateInfo stateInfo;
        public final boolean isHollow;
        @Nullable
        public final EnumDyeColor activeColour;

        public FacadePhasedState(FacadeBlockStateInfo stateInfo, boolean isHollow, EnumDyeColor activeColour) {
            this.stateInfo = stateInfo;
            this.isHollow = isHollow;
            this.activeColour = activeColour;
        }

        public static FacadePhasedState readFromNbt(NBTTagCompound nbt) {
            FacadeBlockStateInfo stateInfo = defaultState;
            if (nbt.hasKey("state")) {
                try {
                    IBlockState blockState = NBTUtil.readBlockState(nbt.getCompoundTag("state"));
                    stateInfo = validFacadeStates.get(blockState);
                    if (stateInfo == null) {
                        stateInfo = defaultState;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            boolean isHollow = nbt.getBoolean("isHollow");
            EnumDyeColor colour = NBTUtilBC.readEnum(nbt.getTag("activeColour"), EnumDyeColor.class);
            return new FacadePhasedState(stateInfo, isHollow, colour);
        }

        public NBTTagCompound writeToNbt() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("state", NBTUtil.writeBlockState(new NBTTagCompound(), stateInfo.state));
            nbt.setBoolean("isHollow", isHollow);
            if (activeColour != null) {
                nbt.setTag("activeColour", NBTUtilBC.writeEnum(activeColour));
            }
            return nbt;
        }

        public static FacadePhasedState readFromBuffer(PacketBufferBC buf) {
            IBlockState state = MessageUtil.readBlockState(buf);
            boolean isHollow = buf.readBoolean();
            EnumDyeColor colour = MessageUtil.readEnumOrNull(buf, EnumDyeColor.class);
            FacadeBlockStateInfo info = validFacadeStates.get(state);
            if (info == null) {
                info = defaultState;
            }
            return new FacadePhasedState(info, isHollow, colour);
        }

        public void writeToBuffer(PacketBufferBC buf) {
            MessageUtil.writeBlockState(buf, stateInfo.state);
            buf.writeBoolean(isHollow);
            MessageUtil.writeEnumOrNull(buf, activeColour);
        }

        public FacadePhasedState withSwappedIsHollow() {
            return new FacadePhasedState(stateInfo, !isHollow, activeColour);
        }

        public FacadePhasedState withColour(EnumDyeColor colour) {
            return new FacadePhasedState(stateInfo, isHollow, colour);
        }

        public boolean isSideSolid(EnumFacing side) {
            return stateInfo.isSideSolid[side.ordinal()];
        }
    }

    public static class FullFacadeInstance {
        public final FacadePhasedState[] phasedStates;
        public final FacadeType type;

        public FullFacadeInstance(FacadePhasedState[] phasedStates) {
            if (phasedStates == null) throw new NullPointerException("phasedStates");
            if (phasedStates.length == 0) throw new IllegalArgumentException("phasedStates.length was 0");
            // Maximum of 17 states - 16 for each colour, 1 for no colour
            if (phasedStates.length > 17) throw new IllegalArgumentException("phasedStates.length was > 17");
            this.phasedStates = phasedStates;
            if (phasedStates.length == 1) {
                type = FacadeType.Basic;
            } else {
                type = FacadeType.Phased;
            }
        }

        public static FullFacadeInstance createSingle(FacadeBlockStateInfo info, boolean isHollow) {
            return new FullFacadeInstance(new FacadePhasedState[] { new FacadePhasedState(info, isHollow, null) });
        }

        public static FullFacadeInstance readFromNbt(NBTTagCompound nbt, String subTag) {
            NBTTagList list = nbt.getTagList(subTag, Constants.NBT.TAG_COMPOUND);
            if (list.hasNoTags()) {
                return new FullFacadeInstance(
                    new FacadePhasedState[] { new FacadePhasedState(defaultState, false, null) });
            }
            FacadePhasedState[] states = new FacadePhasedState[list.tagCount()];
            for (int i = 0; i < list.tagCount(); i++) {
                states[i] = FacadePhasedState.readFromNbt(list.getCompoundTagAt(i));
            }
            return new FullFacadeInstance(states);
        }

        public void writeToNbt(NBTTagCompound nbt, String subTag) {
            NBTTagList list = new NBTTagList();
            for (FacadePhasedState state : phasedStates) {
                list.appendTag(state.writeToNbt());
            }
            nbt.setTag(subTag, list);
        }

        public static FullFacadeInstance readFromBuffer(PacketBufferBC buf) {
            int count = buf.readFixedBits(5);
            FacadePhasedState[] states = new FacadePhasedState[count];
            for (int i = 0; i < count; i++) {
                states[i] = FacadePhasedState.readFromBuffer(buf);
            }
            return new FullFacadeInstance(states);
        }

        public void writeToBuffer(PacketBufferBC buf) {
            buf.writeFixedBits(phasedStates.length, 5);
            for (FacadePhasedState phasedState : phasedStates) {
                phasedState.writeToBuffer(buf);
            }
        }

        public boolean canAddColour(EnumDyeColor colour) {
            for (FacadePhasedState state : phasedStates) {
                if (state.activeColour == colour) {
                    return false;
                }
            }
            return true;
        }

        @Nullable
        public FullFacadeInstance withState(FacadePhasedState state) {
            if (canAddColour(state.activeColour)) {
                FacadePhasedState[] newStates = Arrays.copyOf(phasedStates, phasedStates.length + 1);
                newStates[newStates.length - 1] = state;
                return new FullFacadeInstance(newStates);
            } else {
                return null;
            }
        }

        public FacadePhasedState getCurrentStateForStack() {
            int count = phasedStates.length;
            if (count == 1) {
                return phasedStates[0];
            } else {
                int now = (int) (System.currentTimeMillis() % 100_000);
                return phasedStates[(now / 500) % count];
            }
        }

        public FullFacadeInstance withSwappedIsHollow() {
            FacadePhasedState[] newStates = Arrays.copyOf(phasedStates, phasedStates.length);
            for (int i = 0; i < newStates.length; i++) {
                newStates[i] = newStates[i].withSwappedIsHollow();
            }
            return new FullFacadeInstance(newStates);
        }

        public boolean areAllStatesSolid(EnumFacing side) {
            for (FacadePhasedState state : phasedStates) {
                if (!state.isSideSolid(side)) {
                    return false;
                }
            }
            return true;
        }
    }
}

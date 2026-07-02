/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.plug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import ct.buildcraft.api.core.BCDebugging;
import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.facades.FacadeAPI;
import ct.buildcraft.api.facades.IFacade;
import ct.buildcraft.api.facades.IFacadePhasedState;
import ct.buildcraft.api.facades.IFacadeRegistry;
import ct.buildcraft.api.facades.IFacadeState;
import ct.buildcraft.lib.BCLib;
import ct.buildcraft.lib.misc.BlockUtil;
import ct.buildcraft.lib.misc.ItemStackKey;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.lib.world.SingleBlockAccess;
import ct.buildcraft.silicon.recipe.FacadeSwapRecipe;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.InterModComms.IMCMessage;
import net.minecraftforge.registries.ForgeRegistries;

public enum FacadeStateManager implements IFacadeRegistry {
    INSTANCE;

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("silicon.facade");
    public static final SortedMap<BlockState, FacadeBlockStateInfo> validFacadeStates;
    public static final Map<ItemStackKey, List<FacadeBlockStateInfo>> stackFacades;
    public static FacadeBlockStateInfo defaultState, previewState;

    private static final Map<Block, String> disabledBlocks = new HashMap<>();
    private static final Map<BlockState, ItemStack> customBlocks = new HashMap<>();

    /** An array containing all mods that fail the {@link #doesPropertyConform(Property)} check, and any others.
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
        return getInfoForState(block.defaultBlockState());
    }

    private static FacadeBlockStateInfo getInfoForState(BlockState state) {
        return validFacadeStates.get(state);
    }

    public static void receiveInterModComms(IMCMessage message) {
        String id = getImcMethod(message);
        String sender = getImcSender(message);
        if (FacadeAPI.IMC_FACADE_DISABLE.equals(id)) {
            ResourceLocation loc = getImcResourceLocation(message);
            if (loc == null) {
                return;
            }
            Block block = ForgeRegistries.BLOCKS.getValue(loc);
            if (block == null || block == Blocks.AIR) {
                return;
            }
            disabledBlocks.put(block, sender);
        } else if (FacadeAPI.IMC_FACADE_CUSTOM.equals(id)) {
            CompoundTag nbt = getImcNbt(message);
            if (nbt == null) {
                return;
            }
            String regName = nbt.getString(FacadeAPI.NBT_CUSTOM_BLOCK_REG_KEY);
            ItemStack stack = ItemStack.of(nbt.getCompound(FacadeAPI.NBT_CUSTOM_ITEM_STACK));
            if (regName.isEmpty() || stack.isEmpty()) {
                return;
            }
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(regName));
            if (block == null || block == Blocks.AIR) {
                return;
            }
            BlockState state = block.defaultBlockState();
            if (nbt.contains(FacadeAPI.NBT_CUSTOM_BLOCK_META)) {
                int legacyMeta = nbt.getInt(FacadeAPI.NBT_CUSTOM_BLOCK_META);
                BlockState legacy = Block.stateById(legacyMeta);
                if (legacy.getBlock() == block) {
                    state = legacy;
                }
            }
            customBlocks.put(state, stack.copy());
        }
    }

    private static String getImcMethod(IMCMessage message) {
        Object value = invokeNoArg(message, "method");
        if (value instanceof String s) return s;
        value = invokeNoArg(message, "key");
        if (value instanceof String s) return s;
        return "";
    }

    private static String getImcSender(IMCMessage message) {
        Object value = invokeNoArg(message, "senderModId");
        if (value instanceof String s) return s;
        value = invokeNoArg(message, "modId");
        if (value instanceof String s) return s;
        value = invokeNoArg(message, "getSender");
        if (value instanceof String s) return s;
        return "unknown";
    }

    private static ResourceLocation getImcResourceLocation(IMCMessage message) {
        Object value = invokeNoArg(message, "getResourceLocationValue");
        if (value instanceof ResourceLocation rl) return rl;
        value = invokeNoArg(message, "resourceLocationValue");
        if (value instanceof ResourceLocation rl) return rl;
        return null;
    }

    private static CompoundTag getImcNbt(IMCMessage message) {
        Object value = invokeNoArg(message, "getNBTValue");
        if (value instanceof CompoundTag tag) return tag;
        value = invokeNoArg(message, "getNbtValue");
        if (value instanceof CompoundTag tag) return tag;
        value = invokeNoArg(message, "getMessageSupplier");
        if (value instanceof java.util.function.Supplier<?> supplier) {
            Object supplied = supplier.get();
            if (supplied instanceof CompoundTag tag) return tag;
        }
        return null;
    }

    private static Object invokeNoArg(Object target, String name) {
        try {
            Method m = target.getClass().getMethod(name);
            m.setAccessible(true);
            return m.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    /** @return One of:
     *         <ul>
     *         <li>{@link InteractionResult#SUCCESS} if every state of the block is valid for a facade.
     *         <li>{@link InteractionResult#PASS} if every metadata needs to be checked by
     *         {@link #isValidFacadeState(BlockState)}</li>
     *         <li>{@link InteractionResult#FAIL} with string describing the problem with this block (if it is not valid
     *         for a facade)</li>
     *         </ul>
     */
    private static InteractionResultHolder<String> isValidFacadeBlock(Block block) {
        String disablingMod = disabledBlocks.get(block);
        if (disablingMod != null) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, "it has been disabled by " + disablingMod);
        }
        if (isUserBlacklistedFacadeBlock(block)) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, "it is blacklisted for facade deduplication");
        }
        if (block instanceof IFluidBlock || block instanceof LiquidBlock) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, "it is a fluid block");
        }
        // if (block instanceof BlockSlime) {
        // return "it is a slime block";
        // }
        if (block instanceof GlassBlock || block instanceof StainedGlassBlock) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, "");
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, "");
    }

    /** @return Any of:
     *         <ul>
     *         <li>{@link InteractionResult#SUCCESS} if this state is valid for a facade.
     *         <li>{@link InteractionResult#FAIL} with string describing the problem with this state (if it is not valid
     *         for a facade)</li>
     *         </ul>
     */
    private static InteractionResultHolder<String> isValidFacadeState(BlockState state) {
        if (state.hasBlockEntity()) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, "it has a tile entity");
        }
        if (state.getRenderShape() != RenderShape.MODEL) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, "it doesn't have a normal model");
        }
        if (!Block.isShapeFullBlock(state.getShape(new SingleBlockAccess(state), BlockPos.ZERO))) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, "it isn't a full cube");
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, "");
    }

    @Nonnull
    private static ItemStack getRequiredStack(BlockState state) {
        ItemStack stack = customBlocks.get(state);
        if (stack != null) {
            return stack.copy();
        }
        Block block = state.getBlock();
        Item item = block.asItem();
        if (item != Items.AIR) {
            return new ItemStack(item, 1);
        }
        ItemStack clone = block.getCloneItemStack(state, (BlockHitResult) null, new SingleBlockAccess(state), BlockPos.ZERO, null);
        if (!clone.isEmpty()) {
            clone.setCount(1);
            return clone;
        }
        return StackUtil.EMPTY;
    }

    public static void init() {
        FacadeAPI.registry = INSTANCE;
        defaultState = new FacadeBlockStateInfo(Blocks.AIR.defaultBlockState(), StackUtil.EMPTY, ImmutableSet.of());
        if (FacadeAPI.facadeItem == null) {
            previewState = defaultState;
            return;
        }

        for (Block block : ForgeRegistries.BLOCKS) {
            scanBlock(block);
        }

        previewState = validFacadeStates.get(Blocks.BRICKS.defaultBlockState());
        FacadeSwapRecipe.genRecipes();
    }

    private static void scanBlock(Block block) {
        try {
            if (!DEBUG && KNOWN_INVALID_REPORTED_MODS.contains(ForgeRegistries.BLOCKS.getKey(block).getNamespace())) {
                if (BCLib.VERSION.startsWith("7.99")) {
                    BCLog.logger.warn(
                        "[silicon.facade] Skipping " + block + " as it has been added to the list of broken mods!");
                    return;
                }
            }

            // Check to make sure that all the properties work properly
            // Fixes a bug in extra utilities who doesn't serialise and deserialise properties properly

            boolean allPropertiesOk = true;
            for (Property<?> property : block.getStateDefinition().getProperties()) {
                allPropertiesOk &= doesPropertyConform(property);
            }
            if (!allPropertiesOk) {
                return;
            }

            InteractionResultHolder<String> result = isValidFacadeBlock(block);
            // These strings are hardcoded, so we can get away with not needing the .equals check
            if (result.getResult() != InteractionResult.PASS && result.getResult() != InteractionResult.SUCCESS) {
                if (DEBUG) {
                    BCLog.logger.info("[silicon.facade] Disallowed block " + ForgeRegistries.BLOCKS.getKey(block) + " because "
                        + result.getResult());
                }
                return;
            } else if (DEBUG) {
                if (result.getResult() == InteractionResult.SUCCESS) {
                    BCLog.logger.info("[silicon.facade] Allowed block " + ForgeRegistries.BLOCKS.getKey(block));
                }
            }
            Map<BlockState, ItemStack> usedStates = new HashMap<>();
            Map<ItemStackKey, Map<Property<?>, Comparable<?>>> varyingProperties = new HashMap<>();
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                // state = block.getStateFromMeta(block.getMetaFromState(state));
                // if (!checkedStates.add(state)) {
                // continue;
                // }
                if (result.getResult() != InteractionResult.SUCCESS) {
                    result = isValidFacadeState(state);
                    if (result.getResult() == InteractionResult.SUCCESS) {
                        if (DEBUG) {
                            BCLog.logger.info("[silicon.facade] Allowed state " + state);
                        }
                    } else {
                        if (DEBUG) {
                            BCLog.logger
                                .info("[silicon.facade] Disallowed state " + state + " because " + result.getResult());
                        }
                        continue;
                    }
                }
                if (shouldSkipFacadeState(block, state)) {
                    continue;
                }
                final ItemStack requiredStack;
                try {
                    requiredStack = getRequiredStack(state);
                } catch (RuntimeException e) {
                    BCLog.logger.warn(
                        "[silicon.facade] Disallowed state " + state
                            + " after getRequiredStack(state) threw an exception!", e
                    );
                    continue;
                }
                usedStates.put(state, requiredStack);
                ItemStackKey stackKey = new ItemStackKey(requiredStack);
                Map<Property<?>, Comparable<?>> vars = varyingProperties.get(stackKey);
                if (vars == null) {
                    vars = new HashMap<>(state.getValues());
                    varyingProperties.put(stackKey, vars);
                } else {
                    for (Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
                        Property<?> prop = entry.getKey();
                        Comparable<?> value = entry.getValue();
                        if (vars.get(prop) != value) {
                            vars.put(prop, null);
                        }
                    }
                }
            }
            FriendlyByteBuf testingBuffer = new FriendlyByteBuf(Unpooled.buffer());
            varyingProperties.forEach((key, vars) -> {
                if (DEBUG) {
                    BCLog.logger.info("[silicon.facade]   pre-" + key + ":");
                    vars.keySet().forEach(p -> BCLog.logger.info("[silicon.facade]       " + p));
                }
                vars.entrySet().removeIf(entry -> Objects.nonNull(entry.getValue())
                    || shouldIgnoreFacadeProperty(block, entry.getKey()));
                if (DEBUG && !vars.isEmpty()) {
                    BCLog.logger.info("[silicon.facade]   " + key + ":");
                    vars.keySet().forEach(p -> BCLog.logger.info("[silicon.facade]       " + p));
                }
            });
            Set<String> addedSignatures = new HashSet<>();
            for (Entry<BlockState, ItemStack> entry : usedStates.entrySet()) {
                BlockState state = entry.getKey();
                ItemStack stack = entry.getValue();
                Map<Property<?>, Comparable<?>> vars = varyingProperties.get(new ItemStackKey(stack));
                if (stack.isEmpty()) {
                    continue;
                }
                String signature = createFacadeSignature(state, stack, vars);
                if (!addedSignatures.add(signature)) {
                    continue;
                }
                try {
                    ImmutableSet<Property<?>> varSet = ImmutableSet.copyOf(vars.keySet());
                    FacadeBlockStateInfo info = new FacadeBlockStateInfo(state, stack, varSet);
                    validFacadeStates.put(state, info);
                    if (!info.requiredStack.isEmpty()) {
                        ItemStackKey stackKey = new ItemStackKey(info.requiredStack);
                        stackFacades.computeIfAbsent(stackKey, k -> new ArrayList<>()).add(info);
                    }

                    // Test to make sure that we can read + write it
                    FacadePhasedState phasedState = info.createPhased(null);
                    CompoundTag nbt = phasedState.writeToNbt();
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
                        BCLog.logger.info("[silicon.facade]   Added " + info);
                    }
                } catch (Throwable t) {
                    String msg = "Scanning facade states";
                    msg += "\n\tState = " + state;
                    msg += "\n\tBlock = " + safeToString(() -> ForgeRegistries.BLOCKS.getKey(block).toString());
                    msg += "\n\tStack = " + stack;
                    msg += "\n\tvarying-properties: {";
                    for (Entry<Property<?>, Comparable<?>> varEntry : vars.entrySet()) {
                        msg += "\n\t\t" + varEntry.getKey() + " = " + varEntry.getValue();
                    }
                    msg += "\n\t}";
                    throw new IllegalStateException(msg.replace("\t", "    "), t);
                }
            }
        } catch (RuntimeException e) {
            if (e instanceof IllegalStateException) {
                // This one needs to exit properly
                throw e;
            }
            BCLog.logger.warn("[silicon.facade] Skipping " + block + " as something about it threw an exception! ", e);
        }
    }

    private static boolean isUserBlacklistedFacadeBlock(Block block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        if (key == null) {
            return false;
        }
        String path = key.getPath();
        if (path.startsWith("waxed_")) {
            return true;
        }
        if (path.contains("chute") || path.contains("vine") || path.contains("sculk_vein") || path.contains("glow_lichen")
            || path.contains("chorus_flower") || path.contains("sunflower") || path.contains("rose_bush")
            || path.contains("lilac") || path.contains("peony") || path.equals("cobweb")
            || path.equals("wheat") || path.equals("kelp") || path.equals("kelp_plant")) {
            return true;
        }
        if (block instanceof net.minecraft.world.level.block.SlabBlock) {
            return true;
        }
        BlockState state = block.defaultBlockState();
        if (state.is(BlockTags.FLOWERS)) {
            return true;
        }
        if (block == Blocks.GRASS || block == Blocks.TALL_GRASS || block == Blocks.FERN || block == Blocks.LARGE_FERN) {
            return true;
        }
        return path.contains("berry");
    }

    private static boolean shouldSkipFacadeState(Block block, BlockState state) {
        if (block == Blocks.TARGET) {
            for (Property<?> property : state.getProperties()) {
                if ("power".equals(property.getName())) {
                    Object value = state.getValue((Property) property);
                    if (value instanceof Integer integer && integer.intValue() != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean shouldIgnoreFacadeProperty(Block block, Property<?> property) {
        String name = property.getName();
        if ("waterlogged".equals(name) || "persistent".equals(name) || "distance".equals(name)
            || "facing".equals(name) || "horizontal_facing".equals(name) || "powered".equals(name)
            || "note".equals(name) || "instrument".equals(name) || "type".equals(name)) {
            return true;
        }
        return false;
    }

    private static String createFacadeSignature(BlockState state, ItemStack stack, Map<Property<?>, Comparable<?>> vars) {
        StringBuilder sb = new StringBuilder();
        sb.append(new ItemStackKey(stack));
        vars.keySet().stream()
            .sorted((a, b) -> a.getName().compareTo(b.getName()))
            .forEach(prop -> sb.append('|').append(prop.getName()).append('=')
                .append(safeToString(() -> getPropertyValueName(state, prop))));
        return sb.toString();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static String getPropertyValueName(BlockState state, Property<?> property) {
        Property raw = (Property) property;
        Comparable value = state.getValue(raw);
        return raw.getName(value);
    }

    private static <V extends Comparable<V>> boolean doesPropertyConform(Property<V> property) {
        try {
            property.getValue("");
        } catch (AbstractMethodError error) {
            String message = "Invalid Property object detected!";
            message += "\n  Class = " + property.getClass();
            message += "\n  Method not overriden: Property.parseValue(String)";
            RuntimeException exception = new RuntimeException(message, error);
            if (BCLib.DEV || !BCLib.MC_VERSION.equals("1.12.2")) {
                throw exception;
            } else {
                BCLog.logger.error("[silicon.facade] Invalid property!", exception);
            }
            return false;
        }

        boolean[] allFine = {true};
        property.getAllValues().forEach((a)-> {
        	V value = a.value();
            String name = property.getName(value);
            Optional<V> optional = property.getValue(name);
            V parsed = optional == null ? null : optional.orElse(null);
            if (!Objects.equals(value, parsed)) {
                allFine[0] = false;
                // A property is *wrong*
                // this is a big problem
                String message = "Invalid property value detected!";
                message += "\n  Property class = " + property.getClass();
                message += "\n  Property = " + property;
                message += "\n  Possible Values = " + property.getAllValues();
                message += "\n  Value Name = " + name;
                message += "\n  Value (original) = " + value;
                message += "\n  Value (parsed) = " + parsed;
                message += "\n  Value class (original) = " + (value == null ? null : value.getClass());
                message += "\n  Value class (parsed) = " + (parsed == null ? null : parsed.getClass());
                if (optional == null) {
                    // Massive issue
                    message += "\n  Property.parseValue() -> Null com.google.common.base.Optional!!";
                }
                message += "\n";
                // This check *intentionally* crashes on a new MC version
                // or in a dev environment
                // as this really needs to be fixed
                RuntimeException exception = new RuntimeException(message);
                if (BCLib.DEV || !BCLib.MC_VERSION.equals("1.19.2")) {
                    throw exception;
                } else {
                    BCLog.logger.error("[silicon.facade] Invalid property!", exception);
                }
            }
        });
        return allFine[0];
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
    public IFacadePhasedState createPhasedState(IFacadeState state, DyeColor activeColor) {
        return new FacadePhasedState((FacadeBlockStateInfo) state, activeColor);
    }

    @Override
    public IFacade createPhasedFacade(IFacadePhasedState[] states, boolean isHollow) {
        FacadePhasedState[] realStates = new FacadePhasedState[states.length];
        for (int i = 0; i < states.length; i++) {
            realStates[i] = (FacadePhasedState) states[i];
        }
        return new FacadeInstance(realStates, isHollow);
    }

    @Override
    public void disableBlock(Block block) {
        disabledBlocks.put(block, "direct_api");
    }

    @Override
    public void mapStateToStack(BlockState state, ItemStack stack) {
        if (state != null && !stack.isEmpty()) {
            customBlocks.put(state, stack.copy());
        }
    }
}

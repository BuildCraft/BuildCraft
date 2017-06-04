/* Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/ */

package buildcraft.transport.plug;

import java.util.*;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import buildcraft.api.core.BCLog;
import buildcraft.api.facades.FacadeType;

import buildcraft.lib.misc.*;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.builders.snapshot.FakeWorld;

public class FacadeStateManager {
    private static final SortedMap<IBlockState, FacadeBlockStateInfo> STATE_INFOS = new TreeMap<>(
        BlockUtil.blockStateComparator()
    );
    public static final List<FacadeBlockStateInfo> PREVIEW_STATE_INFOS = new ArrayList<>();
    public static FacadeBlockStateInfo defaultState, previewState;

    public static boolean isValidFacadeState(IBlockState state) {
        Block block = state.getBlock();
        return !(block instanceof IFluidBlock || block instanceof BlockSlime) &&
            (block instanceof BlockGlass || block instanceof BlockStainedGlass || state.isFullCube());
    }

    @Nonnull
    private static ItemStack getRequiredStack(IBlockState state) {
        FakeWorld world = FakeWorld.INSTANCE;
        world.clear();
        world.setBlockState(BlockPos.ORIGIN, state);
        ItemStack stack;
        Block block = state.getBlock();
        try {
            stack = block.getPickBlock(
                state,
                new RayTraceResult(VecUtil.VEC_HALF, EnumFacing.UP, BlockPos.ORIGIN),
                world,
                BlockPos.ORIGIN,
                null
            );
        } catch (Exception ignored) {
            /* Some mods require a non-null player, but we don't have one to give. If a mod's block does require a
             * player entity then we won't support it, as it may require a different stack depending on the player. */
            BCLog.logger.info(
                "[transport.facade] Couldn't get item for " + block + ": " + ExceptionUtils.getStackTrace(ignored)
            );
            return StackUtil.EMPTY;
        }
        world.clear();
        return stack;
    }

    public static FacadeBlockStateInfo getStateInfo(IBlockState state) {
        return STATE_INFOS.computeIfAbsent(
            state,
            stateLocal ->
                isValidFacadeState(state)
                    ? new FacadeBlockStateInfo(state, getRequiredStack(state))
                    : null
        );
    }

    public static FacadeBlockStateInfo getStateInfoWithoutChecks(IBlockState state) {
        return FakeWorld.isInited
            ? getStateInfo(state)
            : new FacadeBlockStateInfo(state, new ItemStack(state.getBlock()));
    }

    public static FacadeBlockStateInfo getStateInfo(ItemStack stack) {
        FakeWorld world = FakeWorld.INSTANCE;
        world.clear();
        EntityPlayer player = new EntityPlayer(world, FakePlayerUtil.INSTANCE.gameProfile) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        player.setHeldItem(EnumHand.MAIN_HAND, stack);
        stack.onItemUse(
            player,
            world,
            BlockPos.ORIGIN,
            EnumHand.MAIN_HAND,
            EnumFacing.UP,
            0.5F,
            0.0F,
            0.5F
        );
        IBlockState state = world.getBlockState(BlockPos.ORIGIN);
        world.clear();
        return getStateInfo(state);
    }

    public static void postInit() {
        SortedSet<IBlockState> states = new TreeSet<>(BlockUtil.blockStateComparatorMetaEqual());
        StreamSupport.stream(ForgeRegistries.BLOCKS.spliterator(), false)
            .flatMap(block ->
                block.getBlockState().getValidStates().stream()
                    .filter(FacadeStateManager::isValidFacadeState)
            )
            .forEach(states::add);
        states.stream()
            .map(state ->
                new FacadeBlockStateInfo(
                    state,
                    new ItemStack(state.getBlock(), 1, state.getBlock().damageDropped(state))
                )
            )
            .forEach(PREVIEW_STATE_INFOS::add);
        defaultState = new FacadeBlockStateInfo(Blocks.AIR.getDefaultState(), StackUtil.EMPTY);
        previewState = new FacadeBlockStateInfo(
            Blocks.BRICK_BLOCK.getDefaultState(),
            new ItemStack(Blocks.BRICK_BLOCK)
        );
    }

    public static class FacadeBlockStateInfo {
        public final IBlockState state;
        public final ItemStack requiredStack;
        public final Map<EnumFacing, Boolean> isSideSolid = new EnumMap<>(EnumFacing.class);

        public FacadeBlockStateInfo(IBlockState state, ItemStack requiredStack) {
            this.state = state;
            this.requiredStack = requiredStack;
            for (EnumFacing side : EnumFacing.VALUES) {
                isSideSolid.put(
                    side,
                    state.isSideSolid(
                        new IBlockAccess() {
                            @Nullable
                            @Override
                            public TileEntity getTileEntity(BlockPos pos) {
                                return null;
                            }

                            @Override
                            public int getCombinedLight(BlockPos pos, int lightValue) {
                                return 0;
                            }

                            @Override
                            public IBlockState getBlockState(BlockPos pos) {
                                return pos.equals(BlockPos.ORIGIN) ? state : Blocks.AIR.getDefaultState();
                            }

                            @Override
                            public boolean isAirBlock(BlockPos pos) {
                                return !pos.equals(BlockPos.ORIGIN);
                            }

                            @Override
                            public Biome getBiome(BlockPos pos) {
                                return null;
                            }

                            @Override
                            public int getStrongPower(BlockPos pos, EnumFacing direction) {
                                return 0;
                            }

                            @Override
                            public WorldType getWorldType() {
                                return WorldType.DEFAULT;
                            }

                            @Override
                            public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
                                return pos.equals(BlockPos.ORIGIN);
                            }
                        },
                        BlockPos.ORIGIN,
                        side
                    )
                );
            }
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
                    stateInfo = getStateInfoWithoutChecks(blockState);
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
            FacadeBlockStateInfo info = getStateInfoWithoutChecks(state);
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
    }

    public static class FullFacadeInstance {
        public final FacadePhasedState[] phasedStates;
        public final FacadeType type;

        public FullFacadeInstance(FacadePhasedState[] phasedStates) {
            if (phasedStates == null) {
                throw new NullPointerException("phasedStates");
            }
            if (phasedStates.length == 0) {
                throw new IllegalArgumentException("phasedStates.length was 0");
            }
            // Maximum of 17 states - 16 for each colour, 1 for no colour
            if (phasedStates.length > 17) {
                throw new IllegalArgumentException("phasedStates.length was > 17");
            }
            this.phasedStates = phasedStates;
            if (phasedStates.length == 1) {
                type = FacadeType.Basic;
            } else {
                type = FacadeType.Phased;
            }
        }

        public static FullFacadeInstance createSingle(FacadeBlockStateInfo info, boolean isHollow) {
            return new FullFacadeInstance(new FacadePhasedState[] {new FacadePhasedState(info, isHollow, null)});
        }

        public static FullFacadeInstance readFromNbt(NBTTagCompound nbt, String subTag) {
            NBTTagList list = nbt.getTagList(subTag, Constants.NBT.TAG_COMPOUND);
            if (list.hasNoTags()) {
                return new FullFacadeInstance(new FacadePhasedState[] {
                    new FacadePhasedState(defaultState, false, null)
                });
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
                if (!state.stateInfo.isSideSolid.get(side)) {
                    return false;
                }
            }
            return true;
        }
    }
}

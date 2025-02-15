/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCoreConfig;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.compat.CompatManager;
import buildcraft.lib.inventory.TransactorEntityItem;
import buildcraft.lib.inventory.filter.StackFilter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.DoubleBlockCombiner.BlockType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.BucketPickupHandlerWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class BlockUtil {

    /**
     * The {@link LootContext.Builder} is created like in {@link Block#getDrops(BlockState, ServerLevel, BlockPos, BlockEntity)},
     * with an additional loot parameter {@link FakePlayer}.
     *
     * @return A list of itemstacks that are dropped from the block, or null if the block is air
     */
    @Nullable
    public static NonNullList<ItemStack> getItemStackFromBlock(ServerLevel world, BlockPos pos, GameProfile owner) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (world.isEmptyBlock(pos)) {
            return null;
        }

        // Use the (old) method as not all mods have converted to the new one
        // (and the old method calls the new one internally)
//        List<ItemStack> drops = block.getDrops(world, pos, state, 0);
        Player fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner, pos);
//        float dropChance = ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, 0, 1.0F, false, fakePlayer);

        LootParams.Builder lootparams$builder = (new LootParams.Builder(world))
//                .withRandom(world.random)
                .withLuck(fakePlayer.getLuck())
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, fakePlayer.getMainHandItem())
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, world.getBlockEntity(pos))
                .withParameter(LootContextParams.THIS_ENTITY, fakePlayer)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, fakePlayer);
        List<ItemStack> drops = state.getDrops(lootparams$builder);

        NonNullList<ItemStack> returnList = NonNullList.create();
//        for (ItemStack s : drops) {
//            if (world.rand.nextFloat() <= dropChance) {
//                returnList.add(s);
//            }
//        }
        returnList.addAll(drops);
        return returnList;
    }

    public static boolean breakBlock(ServerLevel world, BlockPos pos, BlockPos ownerPos, GameProfile owner) {
        return breakBlock(world, pos, BCLibConfig.itemLifespan * 20, ownerPos, owner);
    }

    public static boolean breakBlock(ServerLevel world, BlockPos pos, int forcedLifespan, BlockPos ownerPos, GameProfile owner) {
        NonNullList<ItemStack> items = NonNullList.create();

        if (breakBlock(world, pos, items, ownerPos, owner)) {
            for (ItemStack item : items) {
                dropItem(world, pos, forcedLifespan, item);
            }
            return true;
        }
        return false;
    }

    public static boolean harvestBlock(ServerLevel world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
        FakePlayer fakePlayer = getFakePlayerWithTool(world, tool, owner);
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }

        BlockState state = world.getBlockState(pos);

        if (!state.getBlock().canHarvestBlock(state, world, pos, fakePlayer)) {
            return false;
        }

        // set blockState
//        state.getBlock().onBlockHarvested(world, pos, state, fakePlayer);
        state.getBlock().onDestroyedByPlayer(state, world, pos, fakePlayer, true, world.getFluidState(pos));
        // drop
//        state.getBlock().harvestBlock(world, fakePlayer, pos, state, world.getBlockEntity(pos), tool);
        state.getBlock().playerDestroy(world, fakePlayer, pos, state, world.getBlockEntity(pos), tool);
        // Don't drop items as we do that ourselves
        world.destroyBlock(pos, /* dropBlock = */ false);

        return true;
    }

    public static boolean destroyBlock(ServerLevel world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
        FakePlayer fakePlayer = getFakePlayerWithTool(world, tool, owner);
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }
        world.destroyBlock(pos, true);
        return true;
    }

    public static FakePlayer getFakePlayerWithTool(ServerLevel world, @Nonnull ItemStack tool, GameProfile owner) {
        FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner);
        int i = 0;

        while (player.getItemInHand(InteractionHand.MAIN_HAND) != tool && i < 9) {
            if (i > 0) {
                player.getInventory().setItem(i - 1, StackUtil.EMPTY);
            }

            player.getInventory().setItem(i, tool);
            i++;
        }

        return player;
    }

    public static boolean breakBlock(ServerLevel world, BlockPos pos, NonNullList<ItemStack> drops, BlockPos ownerPos, GameProfile owner) {
        FakePlayer fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner, ownerPos);
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }

        if (!world.isEmptyBlock(pos) && !world.isClientSide && world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            drops.addAll(getItemStackFromBlock(world, pos, owner));
        }
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        return true;
    }

    public static void dropItem(ServerLevel world, BlockPos pos, int forcedLifespan, ItemStack stack) {
        float var = 0.7F;
        double dx = world.random.nextFloat() * var + (1.0F - var) * 0.5D;
        double dy = world.random.nextFloat() * var + (1.0F - var) * 0.5D;
        double dz = world.random.nextFloat() * var + (1.0F - var) * 0.5D;
        ItemEntity entityitem = new ItemEntity(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, stack);

        entityitem.lifespan = forcedLifespan;
        entityitem.setDefaultPickUpDelay();

        world.addFreshEntity(entityitem);
    }

    public static Optional<List<ItemStack>> breakBlockAndGetDrops(ServerLevel world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
        return breakBlockAndGetDrops(world, pos, tool, owner, false);
    }

    /**
     * @param grabAll If true then this will pickup every item in range of the position, false to only get the items
     * that the dropped while breaking the block.
     */
    public static Optional<List<ItemStack>> breakBlockAndGetDrops(ServerLevel world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner, boolean grabAll) {
        AABB aabb = new AABB(pos).inflate(1);
        Set<Entity> entities;
        if (grabAll) {
            entities = Collections.emptySet();
        } else {
            entities = Sets.newIdentityHashSet();
            entities.addAll(world.getEntitiesOfClass(ItemEntity.class, aabb));
        }
        if (!harvestBlock(world, pos, tool, owner)) {
            if (!destroyBlock(world, pos, tool, owner)) {
                return Optional.empty();
            }
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemEntity entity : world.getEntitiesOfClass(ItemEntity.class, aabb)) {
            if (entities.contains(entity)) {
                continue;
            }
            TransactorEntityItem transactor = new TransactorEntityItem(entity);
            ItemStack stack;
            while (!(stack = transactor.extract(StackFilter.ALL, 0, Integer.MAX_VALUE, false)).isEmpty()) {
                stacks.add(stack);
            }
        }
        return Optional.of(stacks);
    }

    public static boolean canChangeBlock(Level world, BlockPos pos, GameProfile owner) {
        return canChangeBlock(world.getBlockState(pos), world, pos, owner);
    }

    public static boolean canChangeBlock(BlockState state, Level world, BlockPos pos, GameProfile owner) {
        if (state == null) return true;

        Block block = state.getBlock();
        if (world.isEmptyBlock(pos)) {
            return true;
        }

        if (isUnbreakableBlock(world, pos, state, owner)) {
            return false;
        }

        // Calen: still/flow -> 1.12.2 same fluid different block / 1.18.2 different fluid same block
//        if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)
        if (block == Blocks.LAVA) {
            return false;
        }
//        else if (block instanceof IFluidBlock && ((IFluidBlock) block).getFluid() != null)
        else if (block instanceof IFluidBlock fluidBlock && fluidBlock.getFluid() != null) {
//            Fluid f = ((IFluidBlock) block).getFluid();
            Fluid f = fluidBlock.getFluid();
//            if (f.getDensity(world, pos) >= 3000)
            if (f.getFluidType().getDensity(state.getFluidState(), world, pos) >= 3000) {
                return false;
            }
        }

        return true;
    }

    public static float getBlockHardnessMining(Level world, BlockPos pos, BlockState state, GameProfile owner) {
        if (world instanceof ServerLevel) {
            Player fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer((ServerLevel) world, owner);
//            float relativeHardness = state.getPlayerRelativeBlockHardness(fakePlayer, world, pos);
//            float relativeHardness = state.getDestroySpeed(world, pos);
            boolean canDestroy = state.canEntityDestroy(world, pos, fakePlayer);
//            if (relativeHardness <= 0.0F)
//            if (relativeHardness < 0.0F)
            if (!canDestroy) {
                // Forge's getPlayerRelativeBlockHardness hook returns 0.0F if the hardness is < 0.0F.
                return -1.0F;
            }
        }
        return state.getDestroySpeed(world, pos);
    }

    public static boolean isUnbreakableBlock(Level world, BlockPos pos, BlockState state, GameProfile owner) {
        return getBlockHardnessMining(world, pos, state, owner) < 0;
    }

    public static boolean isUnbreakableBlock(Level world, BlockPos pos, GameProfile owner) {
        return isUnbreakableBlock(world, pos, world.getBlockState(pos), owner);
    }

    /** Returns true if a block cannot be harvested without a tool. */
    public static boolean isToughBlock(Level world, BlockPos pos) {
//        return !world.getBlockState(pos).getMaterial().isToolNotRequired();
        return world.getBlockState(pos).requiresCorrectToolForDrops();
    }

    public static boolean isFullFluidBlock(Level world, BlockPos pos) {
        return isFullFluidBlock(world.getBlockState(pos), world, pos);
    }

    public static boolean isFullFluidBlock(BlockState state, Level world, BlockPos pos) {
        Block block = state.getBlock();
//        if (block instanceof IFluidBlock)
        if (block instanceof IFluidBlock) {
            FluidStack fluid = ((IFluidBlock) block).drain(world, pos, IFluidHandler.FluidAction.SIMULATE);
//            return fluid == null || fluid.getAmount() > 0;
            return !fluid.isEmpty() || fluid.getAmount() > 0;
        } else if (block instanceof LiquidBlock) {
            int level = state.getValue(LiquidBlock.LEVEL);
//            return level == 0;
            return level == 8;
        }
        return false;
    }

    public static boolean isFluidBlock(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            return true;
        } else if (block instanceof LiquidBlock) {
            int level = state.getValue(LiquidBlock.LEVEL);
            return level > 0;
        }
        return false;
    }

    public static Fluid getFluid(Level world, BlockPos pos) {
//        FluidStack fluid = drainBlock(world, pos, false);
        FluidStack fluid = drainBlock(world, pos, IFluidHandler.FluidAction.SIMULATE);
//        return fluid != null ? fluid.getFluid() : null;
        return (fluid == null || fluid.isEmpty()) ? null : fluid.getFluid();
    }

    public static Fluid getFluidWithFlowing(Level world, BlockPos pos) {
//        IBlockState blockState = world.getBlockState(pos);
//        Block block = blockState.getBlock();
//        if (block == Blocks.FLOWING_WATER) {
//            return FluidRegistry.WATER;
//        }
//        if (block == Blocks.FLOWING_LAVA) {
//            return FluidRegistry.LAVA;
//        }
//        return getFluid(block);
        // Calen: this may be better
        Fluid ret = world.getFluidState(pos).getType();
        return (ret == null || ret instanceof EmptyFluid) ? null : ret;
    }

    public static Fluid getFluid(Block block) {
        if (block instanceof IFluidBlock fluidBlock) {
            return fluidBlock.getFluid();
        }
//        return FluidRegistry.lookupFluidForBlock(block);
        return null;
    }

    public static Fluid getFluidWithoutFlowing(BlockState state) {
        Block block = state.getBlock();
//        if (block instanceof BlockFluidClassic) {
//            if (((BlockFluidClassic) block).isSourceBlock(new SingleBlockAccess(state), SingleBlockAccess.POS)) {
//                return getFluid(block);
//            }
//        }
        if (block instanceof LiquidBlock) {
//            if (state.getValue(BlockLiquid.LEVEL) != 0) {
//                return null;
//            }
//            if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
//                return FluidRegistry.WATER;
//            }
//            if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
//                return FluidRegistry.LAVA;
//            }
//            return FluidRegistry.lookupFluidForBlock(block);
            FluidState fluidState = state.getFluidState();
            Fluid fluid = fluidState.getType();
            if (fluid != null && !(fluid instanceof EmptyFluid) && fluid.isSource(fluidState)) {
                return state.getFluidState().getType();
            }
        }
        return null;
    }

    public static Fluid getFluidWithFlowing(Block block) {
        Fluid fluid = null;
//        if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)
        if (block == Blocks.LAVA) {
            fluid = Fluids.LAVA;
        }
//        else if (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
        else if (block == Blocks.WATER) {
            fluid = Fluids.WATER;
        } else if (block instanceof IFluidBlock) {
            fluid = ((IFluidBlock) block).getFluid();
        }
        return fluid;
    }

    /**
     * Drain the fluid in the block if the fluid is Source.
     * {@link FluidUtil#getFluidHandler(Level, BlockPos, Direction)} can only get the handler of BlockEntity, not the fluid of FluidBlock.
     * {@link FluidUtil#tryPickUpFluid(ItemStack, Player, Level, BlockPos, Direction)} can get fluid from both BlockEntity & FluidBlock,
     * and this method is similar rto it.
     *
     * @param world
     * @param pos
     * @param doDrain
     * @return
     */
    public static FluidStack drainBlock(Level world, BlockPos pos, IFluidHandler.FluidAction doDrain) {
        BlockState state = world.getBlockState(pos);
        if (!state.getFluidState().getType().isSource(state.getFluidState())) {
            return StackUtil.EMPTY_FLUID;
        }
        IFluidHandler handler;
        Block block = state.getBlock();
        if (block instanceof IFluidBlock fluidBlock) {
            handler = new FluidBlockWrapper(fluidBlock, world, pos);
        } else if (block instanceof BucketPickup bucketPickup) {
            handler = new BucketPickupHandlerWrapper(bucketPickup, world, pos);
        } else {
            handler = FluidUtil.getFluidHandler(world, pos, null).orElse(null);
        }
        if (handler != null) {
            return handler.drain(FluidType.BUCKET_VOLUME, doDrain);
        } else {
            return StackUtil.EMPTY_FLUID;
        }
    }

    /** Create an explosion which only affects a single block. */
    public static void explodeBlock(Level world, BlockPos pos) {
//        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        if (world.isClientSide) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

//        Explosion explosion = new Explosion(world, null, x, y, z, 3f, false, false);
        Explosion explosion = new Explosion(world, null, x, y, z, 3f, false, BlockInteraction.KEEP);
        explosion.getToBlow().add(pos);
        explosion.finalizeExplosion(true); // 不破坏方块

        for (Player player : world.players()) {
            if (!(player instanceof ServerPlayer)) {
                continue;
            }

            if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 4096) {
                ((ServerPlayer) player).connection
                        .send(new ClientboundExplodePacket(x, y, z, 3f, explosion.getToBlow(), null));
            }
        }
    }

    public static long computeBlockBreakPower(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        float hardness = state.getDestroySpeed(world, pos);
        return (long) Math.floor(16 * MjAPI.MJ * ((hardness + 1) * 2) * BCCoreConfig.miningMultiplier);
    }

    /** The following functions let you avoid unnecessary chunk loads, which is nice. */
    public static BlockEntity getTileEntity(Level world, BlockPos pos) {
        return getTileEntity(world, pos, false);
    }

    public static BlockEntity getTileEntity(Level world, BlockPos pos, boolean force) {
        return CompatManager.getTile(world, pos, force);
    }

    public static BlockState getBlockState(Level world, BlockPos pos) {
        return getBlockState(world, pos, false);
    }

    public static BlockState getBlockState(Level world, BlockPos pos, boolean force) {
        return CompatManager.getState(world, pos, force);
    }

    public static boolean useItemOnBlock(Level world, Player player, ItemStack stack, BlockPos pos, Direction direction) {
//        boolean done = stack.getItem().onItemUseFirst(player, world, pos, direction, 0.5F, 0.5F, 0.5F, InteractionHand.MAIN_HAND) == InteractionResult.SUCCESS;
        UseOnContext ctx = new UseOnContext(
                world,
                player,
                InteractionHand.MAIN_HAND,
                stack,
                new BlockHitResult(
                        new Vec3(0.5F, 0.5F, 0.5F),
                        direction, pos, false
                )
        );
        boolean done = stack.getItem().onItemUseFirst(
                stack,
                ctx
        ) == InteractionResult.SUCCESS;

        if (!done) {
            done = stack.getItem().useOn(ctx) == InteractionResult.SUCCESS;
        }
        return done;
    }

    public static void onComparatorUpdate(Level world, BlockPos pos, Block block) {
//        world.updateComparatorOutputLevel(pos, block);
        world.updateNeighbourForOutputSignal(pos, block);
    }

    /**
     * Just like {@link ChestBlock#combine(BlockState, Level, BlockPos, boolean)} and
     * {@link DoubleBlockCombiner#combineWithNeigbour(BlockEntityType, Function, Function, DirectionProperty, BlockState, LevelAccessor, BlockPos, BiPredicate)}.
     *
     * @param inv
     * @return
     */
    // public static TileEntityChest getOtherDoubleChest(TileEntity inv)
    public static ChestBlockEntity getOtherDoubleChest(BlockEntity inv) {
        if (inv instanceof ChestBlockEntity) {
            ChestBlockEntity chest = (ChestBlockEntity) inv;
//
//            TileEntityChest adjacent = null;
//
//            chest.checkForAdjacentChests();
//
//            if (chest.adjacentChestXNeg != null) {
//                adjacent = chest.adjacentChestXNeg;
//            }
//
//            if (chest.adjacentChestXPos != null) {
//                adjacent = chest.adjacentChestXPos;
//            }
//
//            if (chest.adjacentChestZNeg != null) {
//                adjacent = chest.adjacentChestZNeg;
//            }
//
//            if (chest.adjacentChestZPos != null) {
//                adjacent = chest.adjacentChestZPos;
//            }
//
//            return adjacent;

            Level world = inv.getLevel();
            BlockState thisState = inv.getBlockState();
            BlockPos thisPos = inv.getBlockPos();

            DirectionProperty directionProperty = ChestBlock.FACING;
            Function<BlockState, BlockType> p_52824_ = ChestBlock::getBlockType;
            BlockType doubleblockcombiner$blocktype = p_52824_.apply(thisState);
            boolean flag = doubleblockcombiner$blocktype == BlockType.SINGLE;
            if (flag) {
                return null;
            } else {
                BlockPos otherPos = thisPos.relative(ChestBlock.getConnectedDirection(thisState));
                BlockState otherState = world.getBlockState(otherPos);
                if (otherState.is(thisState.getBlock())) {
                    BlockType doubleblockcombiner$blocktype1 = p_52824_.apply(otherState);
                    if (doubleblockcombiner$blocktype1 != BlockType.SINGLE
                            && doubleblockcombiner$blocktype != doubleblockcombiner$blocktype1
                            && otherState.getValue(directionProperty) == thisState.getValue(directionProperty)) {
                        return (ChestBlockEntity) chest.getType().getBlockEntity(world, otherPos);
                    }
                }
                return null;
            }
        }
        return null;
    }

    public static <T extends Comparable<T>> BlockState copyProperty(Property<T> property, BlockState dst, BlockState src) {
        return dst.getProperties().contains(property) ? dst.setValue(property, src.getValue(property)) : dst;
    }

    public static <T extends Comparable<T>> int compareProperty(Property<T> property, BlockState a, BlockState b) {
        return a.getValue(property).compareTo(b.getValue(property));
    }

    public static <T extends Comparable<T>> String getPropertyStringValue(BlockState blockState, Property<T> property) {
        return property.getName(blockState.getValue(property));
    }

    public static Map<String, String> getPropertiesStringMap(BlockState blockState, Collection<Property<?>> properties) {
        ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<>();
        for (Property<?> property : properties) {
            mapBuilder.put(property.getName(), getPropertyStringValue(blockState, property));
        }
        return mapBuilder.build();
    }

    public static Map<String, String> getPropertiesStringMap(BlockState blockState) {
        return getPropertiesStringMap(blockState, blockState.getProperties());
    }

    public static Comparator<BlockState> blockStateComparator() {
        return (blockStateA, blockStateB) ->
        {
            Block blockA = blockStateA.getBlock();
            Block blockB = blockStateB.getBlock();
            if (blockA != blockB) {
                return getRegistryName(blockA).toString().compareTo(getRegistryName(blockB).toString());
            }
            for (Property<?> property : Sets.intersection(new HashSet<>(blockStateA.getProperties()),
                    new HashSet<>(blockStateB.getProperties()))) {
                int compareResult = BlockUtil.compareProperty(property, blockStateA, blockStateB);
                if (compareResult != 0) {
                    return compareResult;
                }
            }
            return 0;
        };
    }

    public static boolean blockStatesWithoutBlockEqual(BlockState a, BlockState b, Collection<Property<?>> ignoredProperties) {
        return Sets.intersection(new HashSet<>(a.getProperties()), new HashSet<>(b.getProperties())).stream()
                .filter(property -> !ignoredProperties.contains(property))
                .allMatch(property -> Objects.equals(a.getValue(property), b.getValue(property)));
    }

    public static boolean blockStatesWithoutBlockEqual(BlockState a, BlockState b) {
        return Sets.intersection(new HashSet<>(a.getProperties()), new HashSet<>(b.getProperties())).stream()
                .allMatch(property -> Objects.equals(a.getValue(property), b.getValue(property)));
    }

    public static boolean blockStatesEqual(BlockState a, BlockState b, Collection<Property<?>> ignoredProperties) {
        return a.getBlock() == b.getBlock()
                && Sets.intersection(new HashSet<>(a.getProperties()), new HashSet<>(b.getProperties())).stream()
                .filter(property -> !ignoredProperties.contains(property))
                .allMatch(property -> Objects.equals(a.getValue(property), b.getValue(property)));
    }

    public static boolean blockStatesEqual(BlockState a, BlockState b) {
        return a.getBlock() == b.getBlock()
                && Sets.intersection(new HashSet<>(a.getProperties()), new HashSet<>(b.getProperties())).stream()
                .allMatch(property -> Objects.equals(a.getValue(property), b.getValue(property)));
    }

    public static Comparator<BlockPos> uniqueBlockPosComparator(Comparator<BlockPos> parent) {
        return (a, b) ->
        {
            int parentValue = parent.compare(a, b);
            if (parentValue != 0) {
                return parentValue;
            } else if (a.getX() != b.getX()) {
                return Integer.compare(a.getX(), b.getX());
            } else if (a.getY() != b.getY()) {
                return Integer.compare(a.getY(), b.getY());
            } else if (a.getZ() != b.getZ()) {
                return Integer.compare(a.getZ(), b.getZ());
            } else {
                return 0;
            }
        };
    }

    // Calen
    public static Block getBlockFromRegistryName(String name) {
        return getBlockFromRegistryName(new ResourceLocation(name));
    }

    public static Block getBlockFromRegistryName(ResourceLocation name) {
        return ForgeRegistries.BLOCKS.getValue(name);
    }

    public static ResourceLocation getRegistryName(Block block) {
        return block.builtInRegistryHolder().key().location();
    }
}

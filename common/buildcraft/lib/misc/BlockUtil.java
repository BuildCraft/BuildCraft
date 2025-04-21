/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.blocks.BlockConstants;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCoreConfig;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.compat.CompatManager;
import buildcraft.lib.inventory.TransactorEntityItem;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMerger;
import net.minecraft.tileentity.TileEntityMerger.Type;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
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

    /** The {@link LootContext.Builder} is created like in {@link Block#getDrops(BlockState, ServerWorld, BlockPos, TileEntity)},
     * with an additional loot parameter {@link FakePlayer}.
     *
     * @return A list of itemstacks that are dropped from the block, or null if the block is air */
    @Nullable
    public static NonNullList<ItemStack> getItemStackFromBlock(ServerWorld world, BlockPos pos, GameProfile owner) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (world.isEmptyBlock(pos)) {
            return null;
        }

        // Use the (old) method as not all mods have converted to the new one
        // (and the old method calls the new one internally)
//        List<ItemStack> drops = block.getDrops(world, pos, state, 0);
        PlayerEntity fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner, pos);
//        float dropChance = ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, 0, 1.0F, false, fakePlayer);

        LootContext.Builder lootcontext$builder = (new LootContext.Builder(world))
                .withRandom(world.random)
                .withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(pos))
                .withParameter(LootParameters.TOOL, ItemStack.EMPTY)
                .withOptionalParameter(LootParameters.BLOCK_ENTITY, world.getBlockEntity(pos))
                .withParameter(LootParameters.LAST_DAMAGE_PLAYER, fakePlayer);
        List<ItemStack> drops = state.getDrops(lootcontext$builder);

        NonNullList<ItemStack> returnList = NonNullList.create();
//        for (ItemStack s : drops) {
//            if (world.rand.nextFloat() <= dropChance) {
//                returnList.add(s);
//            }
//        }
        returnList.addAll(drops);
        return returnList;

    }

    public static boolean breakBlock(ServerWorld world, BlockPos pos, BlockPos ownerPos, GameProfile owner) {
        return breakBlock(world, pos, BCLibConfig.itemLifespan * 20, ownerPos, owner);
    }

    public static boolean breakBlock(ServerWorld world, BlockPos pos, int forcedLifespan, BlockPos ownerPos, GameProfile owner) {
        NonNullList<ItemStack> items = NonNullList.create();

        if (breakBlock(world, pos, items, ownerPos, owner)) {
            for (ItemStack item : items) {
                dropItem(world, pos, forcedLifespan, item);
            }
            return true;
        }
        return false;
    }

    public static boolean harvestBlock(ServerWorld world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
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
        state.getBlock().removedByPlayer(state, world, pos, fakePlayer, true, world.getFluidState(pos));
        // drop
//        state.getBlock().harvestBlock(world, fakePlayer, pos, state, world.getBlockEntity(pos), tool);
        state.getBlock().playerDestroy(world, fakePlayer, pos, state, world.getBlockEntity(pos), tool);
        // Don't drop items as we do that ourselves
        world.destroyBlock(pos, /* dropBlock = */ false);

        return true;
    }

    public static boolean destroyBlock(ServerWorld world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
        FakePlayer fakePlayer = getFakePlayerWithTool(world, tool, owner);
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }
        world.destroyBlock(pos, true);
        return true;
    }

    public static FakePlayer getFakePlayerWithTool(ServerWorld world, @Nonnull ItemStack tool, GameProfile owner) {
        FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner);
        int i = 0;

        while (player.getItemInHand(Hand.MAIN_HAND) != tool && i < 9) {
            if (i > 0) {
                player.inventory.setItem(i - 1, StackUtil.EMPTY);
            }

            player.inventory.setItem(i, tool);
            i++;
        }

        return player;
    }

    public static boolean breakBlock(ServerWorld world, BlockPos pos, NonNullList<ItemStack> drops, BlockPos ownerPos, GameProfile owner) {
        FakePlayer fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner, ownerPos);
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }

        if (!world.isEmptyBlock(pos) && !world.isClientSide && world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            drops.addAll(getItemStackFromBlock(world, pos, owner));
        }
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), BlockConstants.UPDATE_ALL);

        return true;
    }

    public static void dropItem(ServerWorld world, BlockPos pos, int forcedLifespan, ItemStack stack) {
        float var = 0.7F;
        double dx = world.random.nextFloat() * var + (1.0F - var) * 0.5D;
        double dy = world.random.nextFloat() * var + (1.0F - var) * 0.5D;
        double dz = world.random.nextFloat() * var + (1.0F - var) * 0.5D;
        ItemEntity entityitem = new ItemEntity(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, stack);

        entityitem.lifespan = forcedLifespan;
        entityitem.setDefaultPickUpDelay();

        world.addFreshEntity(entityitem);
    }

    public static Optional<List<ItemStack>> breakBlockAndGetDrops(ServerWorld world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
        return breakBlockAndGetDrops(world, pos, tool, owner, false);
    }

    /** @param grabAll If true then this will pickup every item in range of the position, false to only get the items
     *            that the dropped while breaking the block. */
    public static Optional<List<ItemStack>> breakBlockAndGetDrops(ServerWorld world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner, boolean grabAll) {
        AxisAlignedBB aabb = new AxisAlignedBB(pos).inflate(1);
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

    public static boolean canChangeBlock(World world, BlockPos pos, GameProfile owner) {
        return canChangeBlock(world.getBlockState(pos), world, pos, owner);
    }

    public static boolean canChangeBlock(BlockState state, World world, BlockPos pos, GameProfile owner) {
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
        else if (block instanceof IFluidBlock && ((IFluidBlock) block).getFluid() != null) {
//            Fluid f = ((IFluidBlock) block).getFluid();
            Fluid f = ((IFluidBlock) block).getFluid();
//            if (f.getDensity(world, pos) >= 3000)
            if (f.getAttributes().getDensity(world, pos) >= 3000) {
                return false;
            }
        }

        return true;
    }

    public static float getBlockHardnessMining(World world, BlockPos pos, BlockState state, GameProfile owner) {
        if (world instanceof ServerWorld) {
            PlayerEntity fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer((ServerWorld) world, owner);
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

    public static boolean isUnbreakableBlock(World world, BlockPos pos, BlockState state, GameProfile owner) {
        return getBlockHardnessMining(world, pos, state, owner) < 0;
    }

    public static boolean isUnbreakableBlock(World world, BlockPos pos, GameProfile owner) {
        return isUnbreakableBlock(world, pos, world.getBlockState(pos), owner);
    }

    /** Returns true if a block cannot be harvested without a tool. */
    public static boolean isToughBlock(World world, BlockPos pos) {
//        return !world.getBlockState(pos).getMaterial().isToolNotRequired();
        return world.getBlockState(pos).requiresCorrectToolForDrops();
    }

    public static boolean isFullFluidBlock(World world, BlockPos pos) {
        return isFullFluidBlock(world.getBlockState(pos), world, pos);
    }

    public static boolean isFullFluidBlock(BlockState state, World world, BlockPos pos) {
        Block block = state.getBlock();
//        if (block instanceof IFluidBlock)
        if (block instanceof IFluidBlock) {
            FluidStack fluid = ((IFluidBlock) block).drain(world, pos, IFluidHandler.FluidAction.SIMULATE);
//            return fluid == null || fluid.getAmount() > 0;
            return !fluid.isEmpty() || fluid.getAmount() > 0;
        } else if (block instanceof FlowingFluidBlock) {
            int level = state.getValue(FlowingFluidBlock.LEVEL);
//            return level == 0;
            return level == 8;
        }
        return false;
    }

    public static Fluid getFluid(World world, BlockPos pos) {
//        FluidStack fluid = drainBlock(world, pos, false);
        FluidStack fluid = drainBlock(world, pos, IFluidHandler.FluidAction.SIMULATE);
//        return fluid != null ? fluid.getFluid() : null;
        return (fluid == null || fluid.isEmpty()) ? null : fluid.getFluid();
    }

    public static Fluid getFluidWithFlowing(World world, BlockPos pos) {
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
        if (block instanceof IFluidBlock) {
            IFluidBlock fluidBlock = (IFluidBlock) block;
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
        if (block instanceof FlowingFluidBlock) {
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
     * {@link FluidUtil#getFluidHandler(World, BlockPos, Direction)} can only get the handler of TileEntity, not the fluid of FluidBlock.
     * {@link FluidUtil#tryPickUpFluid(ItemStack, PlayerEntity, World, BlockPos, Direction)} can get fluid from both TileEntity & FluidBlock,
     * and this method is similar rto it.
     * @param world
     * @param pos
     * @param doDrain
     * @return
     */
    public static FluidStack drainBlock(World world, BlockPos pos, IFluidHandler.FluidAction doDrain) {
        BlockState state = world.getBlockState(pos);
        if (!state.getFluidState().getType().isSource(state.getFluidState())) {
            return StackUtil.EMPTY_FLUID;
        }
        IFluidHandler handler;
        Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            IFluidBlock fluidBlock = (IFluidBlock) block;
            handler = new FluidBlockWrapper(fluidBlock, world, pos);
        } else if (block instanceof IBucketPickupHandler) {
            IBucketPickupHandler bucketPickup = (IBucketPickupHandler) block;
            handler = new BucketPickupHandlerWrapper(bucketPickup, world, pos);
        } else {
            handler = FluidUtil.getFluidHandler(world, pos, null).orElse(null);
        }
        if (handler != null) {
            return handler.drain(FluidAttributes.BUCKET_VOLUME, doDrain);
        } else {
            return StackUtil.EMPTY_FLUID;
        }
    }

    /** Create an explosion which only affects a single block. */
    public static void explodeBlock(World world, BlockPos pos) {
//        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        if (world.isClientSide) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

//        Explosion explosion = new Explosion(world, null, x, y, z, 3f, false, false);
        Explosion explosion = new Explosion(world, null, x, y, z, 3f, false, Explosion.Mode.NONE);
        explosion.getToBlow().add(pos);
        explosion.finalizeExplosion(true); // 不破坏方块

        for (PlayerEntity player : world.players()) {
            if (!(player instanceof ServerPlayerEntity)) {
                continue;
            }

            if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 4096) {
                ((ServerPlayerEntity) player).connection
                        .send(new SExplosionPacket(x, y, z, 3f, explosion.getToBlow(), null));
            }
        }
    }

    public static long computeBlockBreakPower(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        float hardness = state.getDestroySpeed(world, pos);
        return (long) Math.floor(16 * MjAPI.MJ * ((hardness + 1) * 2) * BCCoreConfig.miningMultiplier);
    }

    /** The following functions let you avoid unnecessary chunk loads, which is nice. */
    public static TileEntity getTileEntity(World world, BlockPos pos) {
        return getTileEntity(world, pos, false);
    }

    public static TileEntity getTileEntity(World world, BlockPos pos, boolean force) {
        return CompatManager.getTile(world, pos, force);
    }

    public static BlockState getBlockState(World world, BlockPos pos) {
        return getBlockState(world, pos, false);
    }

    public static BlockState getBlockState(World world, BlockPos pos, boolean force) {
        return CompatManager.getState(world, pos, force);
    }

    public static boolean useItemOnBlock(World world, PlayerEntity player, ItemStack stack, BlockPos pos, Direction direction) {
//        boolean done = stack.getItem().onItemUseFirst(player, world, pos, direction, 0.5F, 0.5F, 0.5F, Hand.MAIN_HAND) == ActionResultType.SUCCESS;
        ItemUseContext ctx = new ItemUseContext(
                world,
                player,
                Hand.MAIN_HAND,
                stack,
                new BlockRayTraceResult(
                        new Vector3d(0.5F, 0.5F, 0.5F),
                        direction, pos, false
                )
        );
        boolean done = stack.getItem().onItemUseFirst(
                stack,
                ctx
        ) == ActionResultType.SUCCESS;

        if (!done) {
            done = stack.getItem().useOn(ctx) == ActionResultType.SUCCESS;
        }
        return done;
    }

    public static void onComparatorUpdate(World world, BlockPos pos, Block block) {
//        world.updateComparatorOutputLevel(pos, block);
        world.updateNeighbourForOutputSignal(pos, block);
    }

    /**
     * Just like {@link ChestBlock#combine(BlockState, World, BlockPos, boolean)} and
     * {@link TileEntityMerger#combineWithNeigbour(TileEntityType, Function, Function, DirectionProperty, BlockState, IWorld, BlockPos, BiPredicate)}.
     * @param inv
     * @return
     */
    // public static TileEntityChest getOtherDoubleChest(TileEntity inv)
    public static ChestTileEntity getOtherDoubleChest(TileEntity inv) {
        if (inv instanceof ChestTileEntity) {
            ChestTileEntity chest = (ChestTileEntity) inv;
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

            World world = inv.getLevel();
            BlockState thisState = inv.getBlockState();
            BlockPos thisPos = inv.getBlockPos();

            DirectionProperty directionProperty = ChestBlock.FACING;
            Function<BlockState, Type> p_52824_ = ChestBlock::getBlockType;
            Type doubleblockcombiner$blocktype = p_52824_.apply(thisState);
            boolean flag = doubleblockcombiner$blocktype == Type.SINGLE;
            if (flag) {
                return null;
            } else {
                BlockPos otherPos = thisPos.relative(ChestBlock.getConnectedDirection(thisState));
                BlockState otherState = world.getBlockState(otherPos);
                if (otherState.is(thisState.getBlock())) {
                    Type doubleblockcombiner$blocktype1 = p_52824_.apply(otherState);
                    if (doubleblockcombiner$blocktype1 != Type.SINGLE
                            && doubleblockcombiner$blocktype != doubleblockcombiner$blocktype1
                            && otherState.getValue(directionProperty) == thisState.getValue(directionProperty)) {
                        return (ChestTileEntity) chest.getType().getBlockEntity(world, otherPos);
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
                return blockA.getRegistryName().toString().compareTo(blockB.getRegistryName().toString());
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
    public static Block getBlockFromName(String name) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
    }

    public static Block getBlockFromName(ResourceLocation name) {
        return ForgeRegistries.BLOCKS.getValue(name);
    }

    public static boolean isReplaceable(BlockState blockState) {
        return blockState.isAir() || blockState.getMaterial().isReplaceable();
    }

    public static boolean isSoftBlock(World world, BlockPos pos) {
        return isSoftBlock(world.getBlockState(pos));
    }

    public static boolean isSoftBlock(BlockState blockState) {
        return blockState.is(OreDictionaryTags.SOFT) ||
                blockState.getBlock() instanceof IFluidBlock ||
                blockState.getBlock() instanceof FlowingFluidBlock ||
                blockState.getBlock() instanceof IPlantable ||
                blockState.getMaterial().isReplaceable();
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ct.buildcraft.api.core.BuildCraftAPI;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.core.BCCoreConfig;
import ct.buildcraft.lib.BCLibConfig;
import ct.buildcraft.lib.compat.CompatManager;
import ct.buildcraft.lib.inventory.TransactorEntityItem;
import ct.buildcraft.lib.inventory.filter.StackFilter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.wrappers.BucketPickupHandlerWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;
import net.minecraftforge.fml.loading.FMLEnvironment;

public final class BlockUtil {

    /** @return A list of itemstacks that are dropped from the block, or null if the block is air */
    @Nullable
    public static NonNullList<ItemStack> getItemStackFromBlock(ServerLevel world, BlockPos pos, GameProfile owner) {
        BlockState state = world.getBlockState(pos);
//    w    Block block = state.getBlock();
        if (state.isAir()) {
            return null;
        }

        // Use the (old) method as not all mods have converted to the new one
        // (and the old method calls the new one internally)
        List<ItemStack> drops = Block.getDrops(state, world, pos, world.getBlockEntity(pos));
        NonNullList<ItemStack> returnList = NonNullList.create();
        returnList.addAll(drops);
        return returnList;
    }

    public static boolean breakBlock(ServerLevel world, BlockPos pos, BlockPos ownerPos, GameProfile owner) {
        return breakBlock(world, pos, BCLibConfig.itemLifespan * 20, ownerPos, owner);
    }

    public static boolean breakBlock(ServerLevel world, BlockPos pos, int forcedLifespan, BlockPos ownerPos,
        GameProfile owner) {
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

        state.getBlock().playerWillDestroy(world, pos, state, fakePlayer);
    //    state.getBlock().playerDestroy(world, fakePlayer, pos, state, world.getBlockEntity(pos), tool);
        // Don't drop items as we do that ourselves
        world.destroyBlock(pos, /* dropBlock = */ true);

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
                player.getInventory().setItem(i-1, ItemStack.EMPTY);;
            }

            player.getInventory().setItem(i, tool);
            i++;
        }

        return player;
    }

    public static boolean breakBlock(ServerLevel world, BlockPos pos, NonNullList<ItemStack> drops, BlockPos ownerPos,
        GameProfile owner) {
        FakePlayer fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner, ownerPos);
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }

        if (!world.getBlockState(pos).isAir() && !world.isClientSide && world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            drops.addAll(getItemStackFromBlock(world, pos, owner));
        }
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        fakePlayer.kill();
        return true;
    }

    public static void dropItem(ServerLevel world, BlockPos pos, int forcedLifespan, ItemStack stack) {
        float var = 0.7F;
        double dx = world.getRandom().nextFloat() * var + (1.0F - var) * 0.5D;
        double dy = world.getRandom().nextFloat() * var + (1.0F - var) * 0.5D;
        double dz = world.getRandom().nextFloat() * var + (1.0F - var) * 0.5D;
        ItemEntity entityitem = new ItemEntity(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, stack);

        entityitem.lifespan = forcedLifespan;
        entityitem.setDefaultPickUpDelay();;

        world.addFreshEntity(entityitem);
    }

    public static Optional<List<ItemStack>> breakBlockAndGetDrops(ServerLevel world, BlockPos pos,
        @Nonnull ItemStack tool, GameProfile owner) {
        return breakBlockAndGetDrops(world, pos, tool, owner, false);
    }

    /** @param grabAll If true then this will pickup every item in range of the position, false to only get the items
     *            that the dropped while breaking the block. */
    public static Optional<List<ItemStack>> breakBlockAndGetDrops(ServerLevel world, BlockPos pos,
        @Nonnull ItemStack tool, GameProfile owner, boolean grabAll) {
        AABB aabb = new AABB(pos).inflate(1);
        Set<Entity> entities;
        if (grabAll) {
            entities = Collections.emptySet();
        } else {
            entities = Sets.newIdentityHashSet();
            entities.addAll(world.getEntitiesOfClass(ItemEntity.class, aabb));
        }
        FakePlayer p = getFakePlayerWithTool(world, tool, owner);
        if (!harvestBlock(world, pos, tool, owner)) {
            if (!destroyBlock(world, pos, tool, owner)) {
            	p.kill();
                return Optional.empty();
            }
        }
        p.kill();
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
        if (state.isAir()) {
            return true;
        }

        if (isUnbreakableBlock(world, pos, state, owner)) {
            return false;
        }

        if (block == Blocks.LAVA) {
            return false;
        } else if (block instanceof IFluidBlock && ((IFluidBlock) block).getFluid() != null) {
            Fluid f = ((IFluidBlock) block).getFluid();
            if (f.getFluidType().getDensity() >= 3000) {
                return false;
            }
        }

        return true;
    }

    public static float getBlockHardnessMining(Level world, BlockPos pos, BlockState state, GameProfile owner) {
        if (world instanceof ServerLevel) {
            Player fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer((ServerLevel) world, owner);
            float relativeHardness = state.getDestroyProgress(fakePlayer, world, pos);
            if (relativeHardness <= 0.0F) {
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

/*    /** Returns true if a block cannot be harvested without a tool. */
  /*  public static boolean isToughBlock(Level world, BlockPos pos) {
        return !world.getBlockState(pos).getMaterial().;
    }*/

    public static boolean isFullFluidBlock(Level world, BlockPos pos) {
        return isFullFluidBlock(world.getBlockState(pos), world, pos);
    }

    public static boolean isFullFluidBlock(BlockState state, Level world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            FluidStack fluid = ((IFluidBlock) block).drain(world, pos, FluidAction.SIMULATE);
            return fluid == null || fluid.getAmount() > 0;
        } else if (block instanceof LiquidBlock) {
            int level = state.getValue(LiquidBlock.LEVEL);
            return level == 0;
        }
        return false;
    }

    //Only get Source or WaterLogged block
    public static Fluid getFluid(Level world, BlockPos pos) {
        FluidStack fluid = drainBlock(world, pos, false);
        return fluid != FluidStack.EMPTY ? fluid.getFluid() : Fluids.EMPTY;
    }

    //check for Source block
    public static Fluid getFluidWithFlowing(Level world, BlockPos pos) {
    	BlockState state = world.getBlockState(pos);
        FluidState fs = state.getFluidState();
        if(state.getBlock() instanceof LiquidBlock liquidBlock)
        	return liquidBlock.getFluid();
        if(!fs.isEmpty())
        	return Fluids.EMPTY;
        return fs.getType();
    }

    /**
     * check is block is FluidBlock
     * */
    public static Fluid getFluid(Block block) {
    	if(block instanceof IFluidBlock lb) {
    		return lb.getFluid();
    	}
    	if(block instanceof LiquidBlock lb) {
      		return lb.getFluid();
    	}
    	return Fluids.EMPTY;
    }
    
    public static Fluid getFluidWithoutFlowing(BlockState state) {
    	FluidState fs = state.getFluidState();
    	if(!fs.isEmpty()&&fs.isSource()) {
    		return fs.getType();
    	}
    	return Fluids.EMPTY;

    }

    public static Fluid getFluidWithFlowing(Block block) {
        if (block instanceof LiquidBlock) {
            return ((LiquidBlock)block).getFluid();
        }
        return Fluids.EMPTY;
    }

    public static FluidStack drainBlock(Level world, BlockPos pos, boolean doDrain) {
    	Block block = world.getBlockState(pos).getBlock();
    	if(!world.getFluidState(pos).isSource())
    		return FluidStack.EMPTY;
  //  	BCLog.logger.debug(world.getBlockState(pos).toString());
        IFluidHandler targetFluidHandler;
        if (block instanceof IFluidBlock)
        {
            targetFluidHandler = new FluidBlockWrapper((IFluidBlock) block, world, pos);
        }
        else if (block instanceof BucketPickup)
        {
            targetFluidHandler = new BucketPickupHandlerWrapper((BucketPickup) block, world, pos);
        }
        else return FluidStack.EMPTY;
        return targetFluidHandler.drain(FluidType.BUCKET_VOLUME, doDrain ? FluidAction.EXECUTE : FluidAction.SIMULATE);
/*        IFluidHandler handler = FluidUtil.getFluidHandler(world, pos, null).orElse(null);
        if (handler != null) {
            return handler.drain(1000, doDrain?FluidAction.EXECUTE:FluidAction.SIMULATE);
        } else {
            return null;
        }*/
    }

    /** Create an explosion which only affects a single block. */
    public static void explodeBlock(Level world, BlockPos pos) {
        if (FMLEnvironment.dist.isClient()) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        Explosion explosion = new Explosion(world, null, x, y, z, 3f, false, Explosion.BlockInteraction.NONE);
        explosion.getToBlow().add(pos);
        explosion.finalizeExplosion(true);

        for (Player player : world.players()) {
            if (!(player instanceof ServerPlayer)) {
                continue;
            }

            if (player.getOnPos().distSqr(pos) < 4096) {
                ((ServerPlayer) player).connection
                    .send(new ClientboundExplodePacket(x, y, z, 3f, explosion.getToBlow(), null));
            }
        }
    }

    public static long computeBlockBreakPower(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        float hardness = state.getDestroySpeed(world, pos);
        long a =  (long) Math.floor(16 * MjAPI.MJ * ((hardness + 1) * 2) * BCCoreConfig.miningMultiplier);
        return a;
    }

    /** The following functions let you avoid unnecessary chunk loads, which is nice. */
    public static BlockEntity getBlockEntity(Level world, BlockPos pos) {
        return getBlockEntity(world, pos, false);
    }

    public static BlockEntity getBlockEntity(Level world, BlockPos pos, boolean force) {
        return CompatManager.getTile(world, pos, force);
    }

    public static BlockState getBlockState(Level world, BlockPos pos) {
        return getBlockState(world, pos, false);
    }

    public static BlockState getBlockState(Level world, BlockPos pos, boolean force) {
        return CompatManager.getState(world, pos, force);
    }

    public static boolean useItemOnBlock(Level world, Player player, ItemStack stack, BlockPos pos,
        Direction direction) {
    	
        boolean done = stack.getItem().onItemUseFirst(stack, new UseOnContext(world, player, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.ZERO, direction, pos, false))) == InteractionResult.SUCCESS;

        if (!done) {
            done = stack.getItem().use(world, player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.SUCCESS;
        }
        return done;
    }

    public static void onComparatorUpdate(Level world, BlockPos pos, Block block) {
        world.updateNeighbourForOutputSignal(pos, block);
    }

    public static @Nullable ChestBlockEntity getOtherDoubleChest(BlockEntity inv) {
        if (inv instanceof ChestBlockEntity) {
            ChestBlockEntity chest = (ChestBlockEntity) inv;
            BlockState chestb = chest.getBlockState();
            if(chestb.getValue(BlockStateProperties.CHEST_TYPE) == ChestType.SINGLE)
            	return null;
            BlockEntity adjacent = inv.getLevel().getBlockEntity(inv.getBlockPos().offset(ChestBlock.getConnectedDirection(chestb).getNormal()));
            return adjacent instanceof ChestBlockEntity ? (ChestBlockEntity) adjacent : null ;
        }
        return null;
    }

    public static <T extends Comparable<T>> BlockState copyProperty(Property<T> property, BlockState dst,
        BlockState src) {
        return dst.getProperties().contains(property) ? dst.setValue(property, src.getValue(property)) : dst;
    }

    public static <T extends Comparable<T>> int compareProperty(Property<T> property, BlockState a, BlockState b) {
        return a.getValue(property).compareTo(b.getValue(property));
    }

    public static <T extends Comparable<T>> String getPropertyStringValue(BlockState blockState,
        Property<T> property) {
        return property.getName(blockState.getValue(property));
    }

    public static Map<String, String> getPropertiesStringMap(BlockState blockState,
        Collection<Property<?>> properties) {
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
        return (blockStateA, blockStateB) -> {
            Block blockA = blockStateA.getBlock();
            Block blockB = blockStateB.getBlock();
            if (blockA != blockB) {
                return blockA.getDescriptionId().toString().compareTo(blockB.getDescriptionId().toString());
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

    public static boolean blockStatesWithoutBlockEqual(BlockState a, BlockState b,
        Collection<Property<?>> ignoredProperties) {
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
        return (a, b) -> {
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

	
/*	public static @Nullable FlowingFluid getFlowingFluidWithState(BlockState state) {
        FluidState fluidState = state.getFluidState();
        if(fluidState.isEmpty()) 
        	return null;
        Fluid fluid = fluidState.getType();
        return fluidState.isSource() : fluid.isSource(fluidState)
	}*/
}

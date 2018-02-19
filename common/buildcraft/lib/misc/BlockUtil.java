/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.items.BCStackHelper;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.compat.CompatManager;
import buildcraft.lib.inventory.TransactorEntityItem;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.world.SingleBlockAccess;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class BlockUtil {

    /**
     * @return A list of itemstacks that are dropped from the block, or null if the block is air
     */
    @Nullable
    public static List<ItemStack> getItemStackFromBlock(WorldServer world, BlockPos pos, GameProfile owner) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.isAir(state, world, pos)) {
            return null;
        }

        List<ItemStack> dropsList = block.getDrops(world, pos, state, 0);
        EntityPlayer fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner, pos);
        float dropChance = ForgeEventFactory.fireBlockHarvesting(dropsList, world, pos, state, 0, 1.0F, false, fakePlayer);

        List<ItemStack> returnList = Lists.newArrayList();
        for (ItemStack s : dropsList) {
            if (world.rand.nextFloat() <= dropChance) {
                returnList.add(s);
            }
        }

        return returnList;
    }

    public static boolean breakBlock(WorldServer world, BlockPos pos, BlockPos ownerPos, GameProfile owner) {
        return breakBlock(world, pos, BCLibConfig.itemLifespan * 20, ownerPos, owner);
    }

    public static boolean breakBlock(WorldServer world, BlockPos pos, int forcedLifespan, BlockPos ownerPos, GameProfile owner) {
        List<ItemStack> items = Lists.newArrayList();

        if (breakBlock(world, pos, items, ownerPos, owner)) {
            for (ItemStack item : items) {
                dropItem(world, pos, forcedLifespan, item);
            }
            return true;
        }
        return false;
    }

    public static boolean harvestBlock(WorldServer world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
        FakePlayer fakePlayer = getFakePlayerWithTool(world, tool, owner);
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);

        if (!state.getBlock().canHarvestBlock(world, pos, fakePlayer)) {
            return false;
        }

        state.getBlock().onBlockHarvested(world, pos, state, fakePlayer);
        state.getBlock().harvestBlock(world, fakePlayer, pos, state, world.getTileEntity(pos), tool);
        world.setBlockToAir(pos);

        return true;
    }

    public static FakePlayer getFakePlayerWithTool(WorldServer world, @Nonnull ItemStack tool, GameProfile owner) {
        FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner);
        int i = 0;

        while (player.getHeldItemMainhand() != tool && i < 9) {
            if (i > 0) {
                player.inventory.setInventorySlotContents(i - 1, null);
            }

            player.inventory.setInventorySlotContents(i, tool);
            i++;
        }

        return player;
    }

    public static boolean breakBlock(WorldServer world, BlockPos pos, List<ItemStack> drops, BlockPos ownerPos, GameProfile owner) {
        FakePlayer fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner, ownerPos);
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }

        if (!world.isAirBlock(pos) && !world.isRemote && world.getGameRules().getBoolean("doTileDrops")) {
            drops.addAll(getItemStackFromBlock(world, pos, owner));
        }
        world.setBlockToAir(pos);

        return true;
    }

    public static void dropItem(WorldServer world, BlockPos pos, int forcedLifespan, ItemStack stack) {
        float var = 0.7F;
        double dx = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
        double dy = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
        double dz = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
        EntityItem entityitem = new EntityItem(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, stack);

        entityitem.lifespan = forcedLifespan;
        entityitem.setDefaultPickupDelay();

        world.spawnEntity(entityitem);
    }

    public static List<ItemStack> breakBlockAndGetDrops(WorldServer world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
        if (!BlockUtil.harvestBlock(world, pos, tool, owner)) {
            world.destroyBlock(pos, true);
        }
        List<ItemStack> stacks = Lists.newArrayList();
        for (EntityItem entity : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).expandXyz(1))) {
            TransactorEntityItem transactor = new TransactorEntityItem(entity);
            ItemStack stack;
            while (!BCStackHelper.isEmpty((stack = transactor.extract(StackFilter.ALL, 0, Integer.MAX_VALUE, false)))) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    public static boolean canChangeBlock(World world, BlockPos pos, GameProfile owner) {
        return canChangeBlock(world.getBlockState(pos), world, pos, owner);
    }

    public static boolean canChangeBlock(IBlockState state, World world, BlockPos pos, GameProfile owner) {
        if (state == null) return true;

        Block block = state.getBlock();
        if (block.isAir(state, world, pos)) {
            return true;
        }

        if (isUnbreakableBlock(world, pos, state, owner)) {
            return false;
        }

        if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
            return false;
        } else if (block instanceof IFluidBlock && ((IFluidBlock) block).getFluid() != null) {
            Fluid f = ((IFluidBlock) block).getFluid();
            return f.getDensity(world, pos) < 3000;
        }

        return true;
    }

    public static float getBlockHardnessMining(World world, BlockPos pos, IBlockState state, GameProfile owner) {
        if (world instanceof WorldServer) {
            EntityPlayer fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer((WorldServer) world, owner);
            float relativeHardness = state.getPlayerRelativeBlockHardness(fakePlayer, world, pos);
            if (relativeHardness <= 0.0F) {
                // Forge's getPlayerRelativeBlockHardness hook returns 0.0F if the hardness is < 0.0F.
                return -1.0F;
            }
        }
        return state.getBlockHardness(world, pos);
    }

    public static boolean isUnbreakableBlock(World world, BlockPos pos, IBlockState state, GameProfile owner) {
        return getBlockHardnessMining(world, pos, state, owner) < 0;
    }

    public static boolean isUnbreakableBlock(World world, BlockPos pos, GameProfile owner) {
        return isUnbreakableBlock(world, pos, world.getBlockState(pos), owner);
    }

    /** Returns true if a block cannot be harvested without a tool. */
    public static boolean isToughBlock(World world, BlockPos pos) {
        return !world.getBlockState(pos).getMaterial().isToolNotRequired();
    }

    public static boolean isFullFluidBlock(World world, BlockPos pos) {
        return isFullFluidBlock(world.getBlockState(pos), world, pos);
    }

    public static boolean isFullFluidBlock(IBlockState state, World world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            FluidStack fluid = ((IFluidBlock) block).drain(world, pos, false);
            return fluid == null || fluid.amount > 0;
        } else if (block instanceof BlockLiquid) {
            int level = state.getValue(BlockLiquid.LEVEL);
            return level == 0;
        }
        return false;
    }

    public static Fluid getFluid(World world, BlockPos pos) {
        FluidStack fluid = drainBlock(world, pos, false);
        return fluid != null ? fluid.getFluid() : null;
    }

    public static Fluid getFluidWithFlowing(World world, BlockPos pos) {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (block == Blocks.FLOWING_WATER) {
            return FluidRegistry.WATER;
        }
        if (block == Blocks.FLOWING_LAVA) {
            return FluidRegistry.LAVA;
        }
        return getFluid(block);
    }

    public static Fluid getFluid(Block block) {
        if (block instanceof IFluidBlock) {
            return FluidRegistry.getFluid(((IFluidBlock) block).getFluid().getName());
        }
        return FluidRegistry.lookupFluidForBlock(block);
    }

    public static Fluid getFluidWithoutFlowing(IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockFluidClassic) {
            if (((BlockFluidClassic) block).isSourceBlock(new SingleBlockAccess(state), SingleBlockAccess.POS)) {
                return getFluid(block);
            }
        }
        if (block instanceof BlockLiquid) {
            if (state.getValue(BlockLiquid.LEVEL) != 0) {
                return null;
            }
            if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                return FluidRegistry.WATER;
            }
            if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
                return FluidRegistry.LAVA;
            }
            return FluidRegistry.lookupFluidForBlock(block);
        }
        return null;
    }

    public static Fluid getFluidWithFlowing(Block block) {
        Fluid fluid = null;
        if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
            fluid = FluidRegistry.LAVA;
        } else if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
            fluid = FluidRegistry.WATER;
        } else if (block instanceof BlockFluidBase) {
            fluid = ((BlockFluidBase) block).getFluid();
        }
        return fluid;
    }

    public static FluidStack drainBlock(World world, BlockPos pos, boolean doDrain) {
        IFluidHandler handler = FluidUtil.getFluidHandler(world, pos, null);
        if (handler != null) {
            return handler.drain(Fluid.BUCKET_VOLUME, doDrain);
        } else {
            return null;
        }
    }

    /** Create an explosion which only affects a single block. */
    public static void explodeBlock(World world, BlockPos pos) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        Explosion explosion = new Explosion(world, null, x, y, z, 3f, false, false);
        explosion.getAffectedBlockPositions().add(pos);
        explosion.doExplosionB(true);

        for (EntityPlayer player : world.playerEntities) {
            if (!(player instanceof EntityPlayerMP)) {
                continue;
            }

            if (player.getDistanceSq(pos) < 4096) {
                ((EntityPlayerMP) player).connection.sendPacket(new SPacketExplosion(x, y, z, 3f, explosion.getAffectedBlockPositions(), null));
            }
        }
    }

    public static long computeBlockBreakPower(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        float hardness = state.getBlockHardness(world, pos);
        return (long) Math.floor(16 * MjAPI.MJ * ((hardness + 1) * 2));
    }

    /** The following functions let you avoid unnecessary chunk loads, which is nice. */
    public static TileEntity getTileEntity(World world, BlockPos pos) {
        return getTileEntity(world, pos, false);
    }

    public static TileEntity getTileEntity(World world, BlockPos pos, boolean force) {
        return CompatManager.getTile(world, pos, force);
    }

    public static IBlockState getBlockState(World world, BlockPos pos) {
        return getBlockState(world, pos, false);
    }

    public static IBlockState getBlockState(World world, BlockPos pos, boolean force) {
        return CompatManager.getState(world, pos, force);
    }

    public static boolean useItemOnBlock(World world, EntityPlayer player, ItemStack stack, BlockPos pos, EnumFacing direction) {
        boolean done = stack.getItem().onItemUseFirst(stack, player, world, pos, direction, 0.5F, 0.5F, 0.5F, EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS;

        if (!done) {
            done = stack.getItem().onItemUse(stack, player, world, pos, EnumHand.MAIN_HAND, direction, 0.5F, 0.5F, 0.5F) == EnumActionResult.SUCCESS;
        }
        return done;
    }

    public static void onComparatorUpdate(World world, BlockPos pos, Block block) {
        world.updateComparatorOutputLevel(pos, block);
    }

    public static TileEntityChest getOtherDoubleChest(TileEntity inv) {
        if (inv instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) inv;

            TileEntityChest adjacent = null;

            chest.checkForAdjacentChests();

            if (chest.adjacentChestXNeg != null) {
                adjacent = chest.adjacentChestXNeg;
            }

            if (chest.adjacentChestXPos != null) {
                adjacent = chest.adjacentChestXPos;
            }

            if (chest.adjacentChestZNeg != null) {
                adjacent = chest.adjacentChestZNeg;
            }

            if (chest.adjacentChestZPos != null) {
                adjacent = chest.adjacentChestZPos;
            }

            return adjacent;
        }
        return null;
    }

    public static <T extends Comparable<T>> IBlockState copyProperty(IProperty<T> property, IBlockState dst, IBlockState src) {
        return dst.getPropertyKeys().contains(property) ? dst.withProperty(property, src.getValue(property)) : dst;
    }

    public static <T extends Comparable<T>> int compareProperty(IProperty<T> property, IBlockState a, IBlockState b) {
        return a.getValue(property).compareTo(b.getValue(property));
    }

    public static <T extends Comparable<T>> String getPropertyStringValue(IBlockState blockState, IProperty<T> property) {
        return property.getName(blockState.getValue(property));
    }

    public static Map<String, String> getPropertiesStringMap(IBlockState blockState, Collection<IProperty<?>> properties) {
        ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<>();
        for (IProperty<?> property : properties) {
            mapBuilder.put(property.getName(), getPropertyStringValue(blockState, property));
        }
        return mapBuilder.build();
    }

    public static Map<String, String> getPropertiesStringMap(IBlockState blockState) {
        return getPropertiesStringMap(blockState, blockState.getPropertyKeys());
    }

    public static Comparator<IBlockState> blockStateComparator() {
        return (blockStateA, blockStateB) -> {
            Block blockA = blockStateA.getBlock();
            Block blockB = blockStateB.getBlock();
            if (blockA != blockB) {
                return blockA.getRegistryName().toString().compareTo(blockB.getRegistryName().toString());
            }
            for (IProperty<?> property : Sets.intersection(
                    new HashSet<>(blockStateA.getPropertyKeys()),
                    new HashSet<>(blockStateB.getPropertyKeys())
            )) {
                int compareResult = BlockUtil.compareProperty(property, blockStateA, blockStateB);
                if (compareResult != 0) {
                    return compareResult;
                }
            }
            return 0;
        };
    }

    public static boolean blockStatesWithoutBlockEqual(IBlockState a, IBlockState b, Collection<IProperty<?>> ignoredProperties) {
        return Sets.intersection(new HashSet<>(a.getPropertyKeys()), new HashSet<>(b.getPropertyKeys())).stream()
                .filter(property -> !ignoredProperties.contains(property))
                .allMatch(property -> Objects.equals(a.getValue(property), b.getValue(property)));
    }

    public static boolean blockStatesWithoutBlockEqual(IBlockState a, IBlockState b) {
        return Sets.intersection(new HashSet<>(a.getPropertyKeys()), new HashSet<>(b.getPropertyKeys())).stream()
                .allMatch(property -> Objects.equals(a.getValue(property), b.getValue(property)));
    }

    public static boolean blockStatesEqual(IBlockState a, IBlockState b, Collection<IProperty<?>> ignoredProperties) {
        return a.getBlock() == b.getBlock() &&
                Sets.intersection(new HashSet<>(a.getPropertyKeys()), new HashSet<>(b.getPropertyKeys())).stream()
                .filter(property -> !ignoredProperties.contains(property))
                .allMatch(property -> Objects.equals(a.getValue(property), b.getValue(property)));
    }

    public static boolean blockStatesEqual(IBlockState a, IBlockState b) {
        return a.getBlock() == b.getBlock() &&
                Sets.intersection(new HashSet<>(a.getPropertyKeys()), new HashSet<>(b.getPropertyKeys())).stream()
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
}

/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.FMLCommonHandler;

import buildcraft.BuildCraftCore;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.lib.misc.FakePlayerUtil;

public final class BlockUtils {
    /** Deactivate constructor */
    private BlockUtils() {}

    public static List<ItemStack> getItemStackFromBlock(WorldServer world, BlockPos pos, BlockPos owner) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == null || block.isAir(state, world, pos)) {
            return null;
        }

        List<ItemStack> dropsList = block.getDrops(world, pos, state, 0);
        EntityPlayer fakePlayer = FakePlayerUtil.INSTANCE.getBuildCraftPlayer(world, pos).get();
        float dropChance = ForgeEventFactory.fireBlockHarvesting(dropsList, world, pos, state, 0, 1.0F, false, fakePlayer);

        ArrayList<ItemStack> returnList = new ArrayList<>();
        for (ItemStack s : dropsList) {
            if (world.rand.nextFloat() <= dropChance) {
                returnList.add(s);
            }
        }

        return returnList;
    }

    public static boolean breakBlock(WorldServer world, BlockPos pos, BlockPos owner) {
        return breakBlock(world, pos, BuildCraftCore.itemLifespan * 20, owner);
    }

    public static boolean breakBlock(WorldServer world, BlockPos pos, int forcedLifespan, BlockPos owner) {
        List<ItemStack> items = new ArrayList<>();

        if (breakBlock(world, pos, items, owner)) {
            for (ItemStack item : items) {
                dropItem(world, pos, forcedLifespan, item);
            }
            return true;
        }
        return false;
    }

    public static boolean harvestBlock(WorldServer world, BlockPos pos, ItemStack tool, BlockPos owner) {
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), CoreProxy.proxy.getBuildCraftPlayer(world, owner).get());
        MinecraftForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);

        EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer(world, pos).get();

        if (!state.getBlock().canHarvestBlock(world, pos, player)) {
            return false;
        }

        state.getBlock().onBlockHarvested(world, pos, state, player);
        state.getBlock().harvestBlock(world, player, pos, state, world.getTileEntity(pos), tool);
        world.setBlockToAir(pos);

        return true;
    }

    public static EntityPlayer getFakePlayerWithTool(WorldServer world, BlockPos pos, ItemStack tool) {
        EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer(world, pos).get();
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

    public static boolean breakBlock(WorldServer world, BlockPos pos, List<ItemStack> drops, BlockPos owner) {
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), CoreProxy.proxy.getBuildCraftPlayer(world, owner).get());
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

        world.spawnEntityInWorld(entityitem);
    }

    public static boolean canChangeBlock(World world, BlockPos pos) {
        return canChangeBlock(world.getBlockState(pos), world, pos);
    }

    public static boolean canChangeBlock(IBlockState state, World world, BlockPos pos) {
        if (state == null) return true;

        Block block = state.getBlock();
        if (block == null || block.isAir(state, world, pos)) {
            return true;
        }

        if (isUnbreakableBlock(world, pos, state)) {
            return false;
        }

        if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
            return false;
        } else if (block instanceof IFluidBlock && ((IFluidBlock) block).getFluid() != null) {
            Fluid f = ((IFluidBlock) block).getFluid();
            if (f.getDensity(world, pos) >= 3000) {
                return false;
            }
        }

        return true;
    }

    public static float getBlockHardnessMining(World world, BlockPos pos, IBlockState state) {
        if (world instanceof WorldServer /* && !BuildCraftCore.miningAllowPlayerProtectedBlocks */) {
            EntityPlayer fakePlayer = FakePlayerUtil.INSTANCE.getBuildCraftPlayer((WorldServer) world, pos).get();
            float relativeHardness = state.getPlayerRelativeBlockHardness(fakePlayer, world, pos);
            if (relativeHardness <= 0.0F) {
                // Forge's getPlayerRelativeBlockHardness hook returns 0.0F if the hardness is < 0.0F.
                return -1.0F;
            }
        }
        return state.getBlockHardness(world, pos);
    }

    public static boolean isUnbreakableBlock(World world, BlockPos pos, IBlockState state) {
        return getBlockHardnessMining(world, pos, state) < 0;
    }

    public static boolean isUnbreakableBlock(World world, BlockPos pos) {
        return isUnbreakableBlock(world, pos, world.getBlockState(pos));
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
            if (fluid == null || fluid.amount > 0) {
                return true;
            }
            return false;
        } else if (block instanceof BlockLiquid) {
            int level = state.getValue(BlockLiquid.LEVEL);
            return level == 0;
        }
        return false;
    }

    public static Fluid getFluid(Block block) {
        return FluidRegistry.lookupFluidForBlock(block);
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
        return drainBlock(world.getBlockState(pos), world, pos, doDrain);
    }

    public static FluidStack drainBlock(IBlockState state, World world, BlockPos pos, boolean doDrain) {
        Block block = state.getBlock();
        Fluid fluid = getFluidWithFlowing(block);

        if (fluid != null && FluidRegistry.isFluidRegistered(fluid)) {
            if (block instanceof IFluidBlock) {
                IFluidBlock fluidBlock = (IFluidBlock) block;
                if (!fluidBlock.canDrain(world, pos)) {
                    return null;
                }
                return fluidBlock.drain(world, pos, doDrain);
            } else {
                int level = state.getValue(BlockLiquid.LEVEL);
                // if (level != 0) {
                // return null;
                // }

                if (doDrain) {
                    world.setBlockToAir(pos);
                }

                return new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
            }
        } else {
            return null;
        }
    }

    /** Create an explosion which only affects a single block. */
    @SuppressWarnings("unchecked")
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

        for (EntityPlayer player : (List<EntityPlayer>) world.playerEntities) {
            if (!(player instanceof EntityPlayerMP)) {
                continue;
            }

            if (player.getDistanceSq(pos) < 4096) {
                ((EntityPlayerMP) player).connection.sendPacket(new SPacketExplosion(x, y, z, 3f, explosion.getAffectedBlockPositions(), null));
            }
        }
    }

    @Deprecated
    public static int computeBlockBreakEnergy(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        float hardness = state.getBlockHardness(world, pos);
        return (int) Math.floor(2 * BuildCraftCore.miningMultiplier * ((hardness + 1) * 2));
    }

    public static int computeBlockBreakPower(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        float hardness = state.getBlockHardness(world, pos);
        return (int) Math.floor(BuilderAPI.BREAK_POWER * ((hardness + 1) * 2));
    }

    /** The following functions let you avoid unnecessary chunk loads, which is nice. */
    public static TileEntity getTileEntity(World world, BlockPos pos) {
        return getTileEntity(world, pos, false);
    }

    public static TileEntity getTileEntity(World world, BlockPos pos, boolean force) {
        if (!force) {
            if (pos.getY() < 0 || pos.getY() > 255) {
                return null;
            }
            Chunk chunk = ThreadSafeUtils.getChunk(world, pos.getX() >> 4, pos.getZ() >> 4);
            return chunk != null ? chunk.getTileEntity(pos, EnumCreateEntityType.CHECK) : null;
        } else {
            return world.getTileEntity(pos);
        }
    }

    public static IBlockState getBlockState(World world, BlockPos pos) {
        return getBlockState(world, pos, false);
    }

    public static IBlockState getBlockState(World world, BlockPos pos, boolean force) {
        if (!force) {
            if (pos.getY() < 0 || pos.getY() >= world.getHeight()) {
                return Blocks.AIR.getDefaultState();
            }
            Chunk chunk = ThreadSafeUtils.getChunk(world, pos.getX() >> 4, pos.getZ() >> 4);
            return chunk != null ? chunk.getBlockState(pos) : Blocks.AIR.getDefaultState();
        } else {
            if (pos.getY() < 0 || pos.getY() > 255) {
                return Blocks.AIR.getDefaultState();
            }
            Chunk chunk = ThreadSafeUtils.getChunk(world, pos.getX() >> 4, pos.getZ() >> 4);
            return chunk != null ? chunk.getBlockState(pos) : Blocks.AIR.getDefaultState();
        }
    }

    // Meta is hidden internally, so we shouldn't even try
    // public static int getBlockMetadata(World world, BlockPos pos) {
    // return getBlockMetadata(world, pos, false);
    //
    // }

    // public static int getBlockMetadata(World world, BlockPos pos, boolean force) {
    // if (!force) {
    // if (y < 0 || y > 255) {
    // return 0;
    // }
    // Chunk chunk = getChunkUnforced(world, x >> 4, z >> 4);
    // return chunk != null ? chunk.getBlockMetadata(x & 15, y, z & 15) : 0;
    // } else {
    // return world.getBlockMetadata(pos);
    // }
    // }

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
}
